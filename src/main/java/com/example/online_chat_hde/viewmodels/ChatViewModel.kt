package com.example.online_chat_hde.viewmodels

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.online_chat_hde.core.ChatClient
import com.example.online_chat_hde.core.UploadError
import com.example.online_chat_hde.models.ConnectionState
import com.example.online_chat_hde.models.InitWidgetData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.MessagingEvent
import com.example.online_chat_hde.models.Rate
import com.example.online_chat_hde.models.RateFormat
import com.example.online_chat_hde.models.Staff
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.TicketStatus
import com.example.online_chat_hde.models.UserRate
import com.example.online_chat_hde.models.VisitorMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ChatViewModelFactory(
    private val chatClient: ChatClient
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(chatClient) as T
    }
}





sealed interface UiEffect {
    data class ShowTopError(val error: UploadError) : UiEffect
}

@Immutable
data class ChatState(
    val isGlobalLoading: Boolean = true,
    val isRatingChat: Boolean = false,
    val rate: Rate? = null,
    val isConnected: Boolean = true,
    val staff: Staff? = null,
    val ticketStatus: TicketStatus = TicketStatus.CHAT_ACTIVE,
    val totalTickets: Int = 0,
    val loadedTicket: Int = 0,
    val messageText: String = "",
    val filePickerExpanded: Boolean = false
)

class ChatViewModel(
    val client: ChatClient
): ViewModel() {

    private val _messages = mutableStateListOf<UiMessage>()
    val messages: SnapshotStateList<UiMessage> = _messages

    private val _loadingMessages = mutableStateListOf<UiMessage>()
    val loadingMessages: SnapshotStateList<UiMessage> = _loadingMessages

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>(replay = 1)
    val effects = _effects.asSharedFlow()


    fun onMessageChange(txt: String) {
        _state.update { it.copy(messageText = txt) }
    }

    fun onFilePickerExpandedChange(expanded: Boolean) {
        _state.update { it.copy(filePickerExpanded = expanded) }
    }

    fun setGlobalLoading(value: Boolean) {
        _state.update { it.copy(isGlobalLoading = value) }
    }

    fun setConnected(value: Boolean) {
        _state.update { it.copy(isConnected = value) }
    }


    fun rateChat(rate: Int, comment: String) {
        client.rateChat(
            UserRate(rate, comment)
        )
    }


    fun deleteMessage(uuid: String) {
        _messages.removeIf { it.message.uuid == uuid }
    }


    fun startChat(data: StartVisitorChatData) {
        client.sendStartChatMessage(data)
    }


    fun sendMessage(text: String) {
        client.sendMessage(message = VisitorMessage(
            text = text,
            files = listOf()
        ))
    }

    fun connect() {
        if (!state.value.isConnected) {
            client.initConnect()
            _state.update { it.copy(isGlobalLoading = true) }
        }
    }


    fun uploadFile(uri: Uri, size: Long) {
        // Ограничиваем размер файла
        if (size / 1024 / 1024 > client.chatOptions.maxUploadFileSizeMB) {
            viewModelScope.launch {
                _effects.emit(UiEffect.ShowTopError(UploadError.FileTooLarge))
            }
        }
        else {
            _state.update { it.copy(isGlobalLoading = true) }
            client.uploadFileService.uploadFile(uri)
        }
    }

    fun showPrependMessages() {
        _state.update { it.copy(
            isGlobalLoading = true,
            loadedTicket = it.loadedTicket+1
        ) }
        client.showPrependMessages(state.value.loadedTicket)
    }

    fun visitorIsTyping(text: String) {
        client.visitorIsTyping(text)
    }

    fun getTicketOptions() = client.ticketOptions
    fun getUserData() = client.userData
    fun getServerOptions() = client.serverOptions



    init {

        // Обработка событий сообщений
        viewModelScope.launch {
            client.messagingEvents.collect { event ->
                when (event) {

                    is MessagingEvent.Server.NewMessage -> {
                        _state.update { it.copy(isGlobalLoading = false) }
                        handleMessage(event.response.data)
                    }

                    is MessagingEvent.Server.PrependMessages -> {
                        val prependMessage = event.response
                        if (prependMessage.data != null) {
                            for (message in prependMessage.data.messages.reversed()) {
                                handleMessage(
                                    message.apply { isPrepend = true }
                                )
                            }
                        }
                        _state.update { it.copy(isGlobalLoading = false) }
                    }

                    is MessagingEvent.Server.StartChat -> {
                        _state.update { it.copy(isGlobalLoading = false, ticketStatus = TicketStatus.CHAT_ACTIVE) }
                        _messages.clear()
                        handleMessage(event.response.data)
                    }

                    is MessagingEvent.Server.TicketCreated -> {
                        _state.update { it.copy(isGlobalLoading = false, ticketStatus = TicketStatus.WAIT_FOR_REPLY) }
                    }

                    is MessagingEvent.Server.CloseChat -> {
                        _state.update { it.copy(isRatingChat = true) }
                    }

                    is MessagingEvent.Server.RateSuccess -> {
                        _state.update { it.copy(isRatingChat = false) }
                    }

                    // обработка назначения сотрудника
                    is MessagingEvent.Server.SetStaff -> {
                        _state.update { it.copy(staff = event.response.data.staff) }
                    }

                    // Обработка инициирующего сообщения
                    is MessagingEvent.Server.InitWidget -> {
                        val authRequired = getTicketOptions().let { it.showEmailField || it.showNameField || !it.consentLink.isNullOrEmpty() }
                        // обработка события показывать ли экран тикета
                        val status = if (event.response.data.ticketForm) TicketStatus.STAFF_OFFLINE
                                     else if (event.response.data is InitWidgetData.First && authRequired) TicketStatus.FIRST_MESSAGE
                                     else TicketStatus.CHAT_ACTIVE

                        val initWidgetData = event.response.data

                        _state.update { it.copy(
                            ticketStatus = status,
                            rate = initWidgetData.rate
                        ) }

                        when (initWidgetData) {
                            is InitWidgetData.First -> {
                                initWidgetData.initialChatButtons?.let {
                                    handleInitMessages(listOf(Message.Server(name = client.chatOptions.botName).apply {
                                        chatButtons = it
                                        isVirtual = true
                                        text = client.chatOptions.welcomeMessage
                                    }))
                                }
                                _state.update { it.copy(
                                    isGlobalLoading = false,
                                    totalTickets = 1
                                )}
                            }
                            is InitWidgetData.Progress -> {
                                _state.update { it.copy(totalTickets = initWidgetData.widgetChat.totalTickets) }
                                // Добавляем кнопки к последнему сообщению если есть
                                val savedButtons = client.getChatButtons()
                                if (savedButtons.isNotEmpty()) {
                                    initWidgetData.widgetChat.messages.lastOrNull()?.let {
                                        it.chatButtons = savedButtons
                                    }
                                }
                                handleInitMessages(initWidgetData.widgetChat.messages)

                                val newStaff = initWidgetData.widgetChat.staff
                                newStaff?.let { sf ->
                                    _state.update { it.copy(staff = sf) }
                                }
                            }
                        }

                    }



                    // обработка загружаемых сообщений клиента
                    is MessagingEvent.User.LoadingMessage -> {
                        val message = event.data
                        if (message.files.isNotEmpty()) {
                            // дожидаемся загрузки и потом рисуем сразу из server-response
                            _state.update { it.copy(isGlobalLoading = true) }
                        }
                        else {
                            // добавляем сообшение с загрузкой
                            val ormes = UiMessage(
                                Message.User().apply {
                                    uuid = message.uuid
                                    text = message.text
                                    visitor = true
                                },
                                isLoading = true
                            )
                            _loadingMessages.add(ormes)
                        }

                        _messages.removeIf { it.message.isVirtual }
                    }

                    else -> {}

                }
            }
        }


        // Отслеживаем ошибки загрузки файла
        viewModelScope.launch {
            client.errorEvents.collect { err ->
                _state.update { it.copy(isGlobalLoading = false) }
                _effects.emit(UiEffect.ShowTopError(err))
            }
        }


        // Отслеживание состояния подключения
        viewModelScope.launch {
            client.connectionState.collect { conn ->
                _state.update { it.copy(isConnected = conn is ConnectionState.Connected) }
            }
        }
    }


    private fun handleMessage(message: Message) {
        when (message) {
            // пришедшие сообщения АГЕНТОВ
            is Message.Server -> {
                // Если сообщение содержит и текст и файлы, надо раздробить его на кусочки (для корректного определения размера)
                val messagesList = splitMessage(message)

                if (!message.isVirtual && !message.isPrepend) {
                    _messages.removeIf { it.message.isVirtual }
                }

                messagesList.forEach {
                    val ormes = UiMessage(it)
                    if (message.isPrepend) {
                        viewModelScope.launch {
                            _messages.add(0, ormes)
                        }
                    }
                    else {
                        _messages.add(ormes)
                    }
                }
            }

            // подтвержденные сообщения ПОЛЬЗОВАТЕЛЯ
            is Message.User -> {
                if (_loadingMessages.isNotEmpty() && _loadingMessages[0].message.text == message.text) {
                    _loadingMessages.removeAt(0)
                }

                val ormes = UiMessage(message)
                if (message.isPrepend) {
                    viewModelScope.launch {
                        _messages.add(0, ormes)
                    }
                }
                else {
                    _messages.add(ormes)
                }
            }
        }
    }


    private fun handleInitMessages(messages: List<Message>) {
        // Если сообщение содержит и текст и файлы, надо раздробить его на кусочки
        // Надо пройтись по всем сообщениям и раздробить при необходимости
        val messagesList = mutableListOf<Message>()
        for (message in messages) {
            messagesList.addAll(splitMessage(message))
        }

        // т.к. это init сообщение, надо очистить переписку и заполнить заново
        _messages.clear()
        _messages.addAll(
            messagesList.map {
                val ormes = UiMessage(it)
                return@map ormes
            }
        )

        _state.update { it.copy(isGlobalLoading = false) }
    }


    private fun splitMessage(message: Message): List<Message> {
        val messagesList = mutableListOf<Message>()
        val textPart = when (message) {
            is Message.Server -> Message.Server(message.name)
            is Message.User -> Message.User()
        }.apply {
            this.text = message.text
            this.time = message.time
            this.isVirtual = message.isVirtual
            this.chatButtons = message.chatButtons
            this.dates = message.dates
        }
        messagesList.add(textPart)

        if (!message.files.isNullOrEmpty()) {
            for (file in message.files!!) {
                val fileMessage = when (message) {
                    is Message.Server -> Message.Server(message.name)
                    is Message.User -> Message.User()
                }.apply {
                    this.text = ""
                    this.time = message.time
                    this.isVirtual = message.isVirtual
                    this.dates = message.dates
                    this.files = listOf(file)
                }
                messagesList.add(fileMessage)
            }
        }

        return messagesList
    }

}


data class UiMessage(
    val message: Message,
    var isLoading: Boolean = false,
    var showButtons: MutableState<Boolean> = mutableStateOf(true)
)
