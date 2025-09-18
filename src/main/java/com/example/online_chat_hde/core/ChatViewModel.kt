package com.example.online_chat_hde.core

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.online_chat_hde.models.ConnectionState
import com.example.online_chat_hde.models.InitWidgetData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.MessagingEvent
import com.example.online_chat_hde.models.Staff
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.TicketOptions
import com.example.online_chat_hde.models.TicketOptionsWithStatus
import com.example.online_chat_hde.models.TicketStatus
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.ui.ChatUIConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt





class ChatViewModelFactory(
    private val chatClient: ChatClient
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(chatClient) as T
    }
}






class ChatViewModel(
    val client: ChatClient
): ViewModel() {

    private val _messages = mutableStateListOf<OrientedMessage>()
    val messages: SnapshotStateList<OrientedMessage> = _messages

    private val _loadingMessages = mutableStateListOf<OrientedMessage>()
    val loadingMessages: SnapshotStateList<OrientedMessage> = _loadingMessages

    val isGlobalLoading = mutableStateOf(true)

    val staff = mutableStateOf<Staff?>(null)

    val ticketStatus = mutableStateOf(TicketOptionsWithStatus(TicketOptions(), TicketStatus.CHAT_ACTIVE))

    val totalTickets = mutableIntStateOf(0)
    val loadedTicket = mutableIntStateOf(0)

    val errorKey = mutableStateOf<String?>(null)

    val firstVisible = mutableIntStateOf(0)
    val offset = mutableIntStateOf(0)

    private val _saveScroll = MutableSharedFlow<Unit>()
    val saveScroll = _saveScroll.asSharedFlow()

    private val _restoreScroll = MutableSharedFlow<Unit>(replay = 1)
    val restoreScroll = _restoreScroll.asSharedFlow()

    private val _scrollToBottom = MutableSharedFlow<Unit>(replay = 1)
    val scrollToBottom: SharedFlow<Unit> = _scrollToBottom.asSharedFlow()

    val isConnected = mutableStateOf(false)

    private fun triggerScroll() {
        viewModelScope.launch {
            _scrollToBottom.emit(Unit)
        }
    }

    fun attachTextEnv(
        density: Density,
        resolver: FontFamily.Resolver,
        bubbleMaxWidthPx: Int,
        uiConfig: ChatUIConfig
    ) {
        this.density = density
        this.sizer = TextSizer(density, resolver)
        this.uiConfig = uiConfig
        this.bubbleMaxWidthPx = bubbleMaxWidthPx
        this.timeWidthPx = sizer!!.measureMessageLine("00:00", TextStyle(fontSize = uiConfig.dimensions.timeFontSize))
    }

    @Volatile private var sizer: TextSizer? = null
    @Volatile private lateinit var uiConfig: ChatUIConfig
    @Volatile private var timeWidthPx: Int = 0
    @Volatile private var bubbleMaxWidthPx: Int = 0
    @Volatile private lateinit var density: Density


    private fun checkHorizontalMode(
        orientedMessage: OrientedMessage
    ): Boolean {
        val innerSpacePx = with(density) {uiConfig.dimensions.innerIndent.roundToPx()}
        val boxPaddingPx = with(density) {uiConfig.dimensions.messagePadding.roundToPx()}
        val messageLength = orientedMessage.textWidth
        val minFreeSpace = with(density) {uiConfig.dimensions.messageMinEndIndent.roundToPx()}
        val contentPadding = with(density) {uiConfig.dimensions.contentHorizontalPadding.roundToPx()}

        if ( !orientedMessage.message.files.isNullOrEmpty()) {
            // если это файл
            val pyperclipWidth = uiConfig.dimensions.pyperclipSize.value.roundToInt()
            val result = (messageLength +
                    boxPaddingPx*2 +
                    innerSpacePx*2 +
                    pyperclipWidth +
                    timeWidthPx <= bubbleMaxWidthPx - minFreeSpace - contentPadding*2)
            return result
        }
        else {
            val result = (messageLength + boxPaddingPx*2 + innerSpacePx + timeWidthPx <= bubbleMaxWidthPx - minFreeSpace - contentPadding*2)
            return result
        }
    }


    private fun detectTextSize(orientedMessage: OrientedMessage) {
        if ('\n' in orientedMessage.message.text) {
            orientedMessage.textWidth = Int.MAX_VALUE
        }
        else {
            val s = sizer ?: return
            val style = TextStyle(
                fontSize = uiConfig.dimensions.messageFontSize
            )
            if (!orientedMessage.message.files.isNullOrEmpty()) {
                orientedMessage.textWidth = s.measureMessageLine(orientedMessage.message.files!![0].name, style)
            }
            else {
                orientedMessage.textWidth = s.measureMessageLine(orientedMessage.message.text, style)
            }
        }
    }


    fun recheckHorizontalMode() {
        for (item in _messages) {
            item.placeHorizontal.value = checkHorizontalMode(item)
        }
        for (item in _loadingMessages) {
            item.placeHorizontal.value = checkHorizontalMode(item)
        }
    }


    init {

        // Обработка событий сообщений
        viewModelScope.launch {
            client.messagingEvents.collect { event ->
                when (event) {
                    is MessagingEvent.Server -> when (event) {
                        is MessagingEvent.Server.NewMessage -> {
                            isGlobalLoading.value = false
                            handleMessage(event.data.data)
                        }

                        is MessagingEvent.Server.PrependMessages -> {
                            val prependMessage = event.data
                            if (prependMessage.data != null) {
                                for (message in prependMessage.data.messages.reversed()) {
                                    handleMessage(
                                        message.apply { isPrepend = true }
                                    )
                                }
                            }
                            isGlobalLoading.value = false
                        }

                        is MessagingEvent.Server.StartChat -> {
                            isGlobalLoading.value = false
                            ticketStatus.value = TicketOptionsWithStatus(options = getTicketOptions(), status = TicketStatus.CHAT_ACTIVE)
                            _messages.clear()
                            handleMessage(event.data.data)
                        }

                        is MessagingEvent.Server.TicketCreated -> {
                            ticketStatus.value = TicketOptionsWithStatus(
                                options = getTicketOptions(),
                                status = TicketStatus.WAIT_FOR_REPLY
                            )
                            isGlobalLoading.value = false
                        }

                        // обработка назначения сотрудника
                        is MessagingEvent.Server.SetStaff -> {
                            staff.value = event.data.data.staff
                        }

                        // Обработка инициирующего сообщения
                        is MessagingEvent.Server.InitWidget -> {
                            // обработка события показывать ли экран тикета
                            val ticketOptions = getTicketOptions()
                            val disabledTicket = ticketOptions.consentLink==null && !ticketOptions.showEmailField && !ticketOptions.showNameField
                            val status = TicketOptionsWithStatus(
                                options = ticketOptions,
                                status = if (event.data.data.ticketForm) TicketStatus.STAFF_OFFLINE
                                         else if (client.getStartChatMessage() != null || disabledTicket) TicketStatus.CHAT_ACTIVE
                                         else if (event.data.data is InitWidgetData.First) TicketStatus.FIRST_MESSAGE
                                         else TicketStatus.CHAT_ACTIVE
                            )
                            ticketStatus.value = status

                            when (val initWidgetData = event.data.data) {
                                is InitWidgetData.First -> {
                                    initWidgetData.initialChatButtons?.let {
                                        handleInitMessages(listOf(Message.Server(name = client.chatOptions.botName).apply {
                                            chatButtons = it
                                            isVirtual = true
                                            text = client.chatOptions.welcomeMessage
                                        }))
                                    }
                                    totalTickets.intValue = 1
                                    isGlobalLoading.value = false
                                }
                                is InitWidgetData.Progress -> {
                                    totalTickets.intValue = initWidgetData.widgetChat.totalTickets
                                    // Добавляем кнопки к последнему сообщению если есть
                                    val savedButtons = client.getChatButtons()
                                    if (savedButtons.isNotEmpty()) {
                                        initWidgetData.widgetChat.messages.lastOrNull()?.let {
                                            it.chatButtons = savedButtons
                                        }
                                    }
                                    handleInitMessages(initWidgetData.widgetChat.messages)

                                    val newStaff = initWidgetData.widgetChat.staff
                                    newStaff?.let {
                                        staff.value = it
                                    }
                                }
                            }

                        }
                    }

                    is MessagingEvent.User -> when (event) {
                        // обработка загружаемых сообщений клиента
                        is MessagingEvent.User.LoadingMessage -> {
                            val message = event.data
                            if (message.files.isNotEmpty()) {
                                // дожидаемся загрузки и потом рисуем сразу из server-response
                                isGlobalLoading.value = true
                            }
                            else {
                                // добавляем сообшение с загрузкой
                                val ormes = OrientedMessage(
                                    Message.User().apply {
                                        uuid = message.uuid
                                        text = message.text
                                        visitor = true
                                    },
                                    isLoading = true
                                )
                                detectTextSize(ormes)
                                val isHorizontal = checkHorizontalMode(ormes)
                                ormes.placeHorizontal.value = isHorizontal
                                _loadingMessages.add(ormes)
                            }

                            _messages.removeIf { it.message.isVirtual }

                            triggerScroll()
                        }

                        else -> {}
                    }
                }
            }
        }


        // Отслеживаем ошибки загрузки файла
        viewModelScope.launch {
            client.errorEvents.collect {
                isGlobalLoading.value = false
                showTopError(it)
            }
        }


        // Отслеживание состояния подключения
        viewModelScope.launch {
            client.connectionState.collect { conn ->
                isConnected.value = conn is ConnectionState.Connected
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
                    val ormes = OrientedMessage(it)
                    detectTextSize(ormes)
                    val isHorizontal = checkHorizontalMode(ormes)
                    ormes.placeHorizontal.value = isHorizontal
                    if (message.isPrepend) {
                        saveScroll()
                        viewModelScope.launch {
                            _messages.add(0, ormes)
                        }
                    }
                    else {
                        _messages.add(ormes)
                        triggerScroll()
                    }
                }
            }

            // подтвержденные сообщения ПОЛЬЗОВАТЕЛЯ
            is Message.User -> {
                if (_loadingMessages.isNotEmpty() && _loadingMessages[0].message.text == message.text) {
                    _loadingMessages.removeAt(0)
                }

                val ormes = OrientedMessage(message)
                detectTextSize(ormes)
                val isHorizontal = checkHorizontalMode(ormes)
                ormes.placeHorizontal.value = isHorizontal
                if (message.isPrepend) {
                    saveScroll()
                    viewModelScope.launch {
                        _messages.add(0, ormes)
                    }
                }
                else {
                    _messages.add(ormes)
                    triggerScroll()
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
                val ormes = OrientedMessage(it)
                detectTextSize(ormes)
                val isHorizontal = checkHorizontalMode(ormes)
                ormes.placeHorizontal.value = isHorizontal
                return@map ormes
            }
        )

        triggerScroll()
        isGlobalLoading.value = false
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


    private fun showTopError(key: String) {
        errorKey.value = key
        viewModelScope.launch {
            delay(5000)
            errorKey.value = null
        }
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
        if (!isConnected.value) {
            client.initConnect()
            isGlobalLoading.value = true
        }
    }

    fun closeChat() {
        client.disconnect()
    }


    fun uploadFile(uri: Uri, size: Long) {
        // Ограничиваем размер файла
        if (size / 1024 / 1024 > client.chatOptions.maxUploadFileSizeMB) {
            showTopError(ErrorKeys.FILE_MAX_SIZE)
        }
        else {
            isGlobalLoading.value = true
            client.uploadFileService.uploadFile(uri)
        }
    }

    fun showPrependMessages() {
        isGlobalLoading.value = true
        loadedTicket.intValue += 1
        client.showPrependMessages(loadedTicket.intValue)
    }

    fun visitorIsTyping(text: String) {
        client.visitorIsTyping(text)
    }

    fun getTicketOptions() = client.ticketOptions
    fun getUserData() = client.userData
    fun getServerOptions() = client.serverOptions

    fun saveScroll() {
        viewModelScope.launch { _saveScroll.emit(Unit) }
    }
    fun restoreScroll() {
        viewModelScope.launch { _restoreScroll.emit(Unit) }
    }

}


data class OrientedMessage(
    val message: Message,
    var textWidth: Int = 0,
    var isLoading: Boolean = false,
    var placeHorizontal: MutableState<Boolean> = mutableStateOf(false),
    var showButtons: MutableState<Boolean> = mutableStateOf(true)
)




internal class TextSizer(
    private val density: Density,
    private val resolver: FontFamily.Resolver
) {

    fun measureMessageLine(
        text: String,
        style: TextStyle
    ): Int {
        val intr = ParagraphIntrinsics(
            text = text,
            style = style,
            annotations = emptyList<AnnotatedString.Range<SpanStyle>>(),
            density = density,
            fontFamilyResolver = resolver,
            placeholders = emptyList()
        )
        return intr.maxIntrinsicWidth.toInt()
    }
}