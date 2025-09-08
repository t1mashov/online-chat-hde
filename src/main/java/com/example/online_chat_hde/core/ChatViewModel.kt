package com.example.online_chat_hde.core

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.Staff
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.ui.ChatUIConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt





class ChatViewModelFactory(
    private val chatService: ChatService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(chatService) as T
    }
}






class ChatViewModel(
    private val service: ChatService
): ViewModel() {

    private val _messages = mutableStateListOf<OrientedMessage>()
    val messages: SnapshotStateList<OrientedMessage> = _messages

    private val _loadingMessages = mutableStateListOf<OrientedMessage>()
    val loadingMessages: SnapshotStateList<OrientedMessage> = _loadingMessages

    val isGlobalLoading = mutableStateOf(true)

    val isInternetAvailable = mutableStateOf(false)

    val staff = mutableStateOf<Staff?>(null)

    val showTicket = mutableStateOf(TicketOptionsWithStatus(TicketOptions(), TicketStatus.DISABLED))

    val totalTickets = mutableIntStateOf(1)

    val errorKey = mutableStateOf<String?>(null)


    private val _saveScroll = MutableSharedFlow<Unit>()
    val saveScroll = _saveScroll.asSharedFlow()

    private val _scrollToBottom = MutableSharedFlow<Unit>()
    val scrollToBottom = _scrollToBottom.asSharedFlow()

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

        // подключение к интернету
        viewModelScope.launch {
            service.internetAvailable.collect { success ->
                isInternetAvailable.value = success
            }
        }

        // обработка назначения сотрудника
        viewModelScope.launch {
            service.setStaffFlow.collect { setStaff ->
                staff.value = setStaff.data.staff
            }
        }

        // обработка события есть ли агенты в сети
        viewModelScope.launch {
            service.ticketDataFlow.collect { isTicketVisible ->
                showTicket.value = isTicketVisible
            }
        }

        // обработка загружаемых сообщений клиента
        viewModelScope.launch {
            service.loadingMessageFlow.collect { message ->
                if (message.files.isNotEmpty()) {
                    // дожидаемся загрузки и потом рисуем сразу из server-response
                    isGlobalLoading.value = true
                }
                else {
                    // добавляем сообшение с загрузкой
                    val ormes = OrientedMessage(
                        Converter.visitorMessageToUserMessage(message),
                        isLoading = true
                    )
                    detectTextSize(ormes)
                    val isHorizontal = checkHorizontalMode(ormes)
                    ormes.placeHorizontal.value = isHorizontal
                    _loadingMessages.add(ormes)
                }

                _messages.removeIf { it.message.isVirtual }

            }
        }

        // пришедшие сообщения АГЕНТОВ
        viewModelScope.launch {
            service.newServerMessageFlow.collect { message ->

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
                        viewModelScope.launch {
                            _saveScroll.emit(Unit)
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

        // пришедшие сообщения КЛИЕНТА
        viewModelScope.launch {
            service.newUserMessageFlow.collect { message ->

                if (_loadingMessages.isNotEmpty() && _loadingMessages[0].message.text == message.text) {
                    _loadingMessages.removeAt(0)
                }

                val ormes = OrientedMessage(message)
                detectTextSize(ormes)
                val isHorizontal = checkHorizontalMode(ormes)
                ormes.placeHorizontal.value = isHorizontal
                if (message.isPrepend) {
                    viewModelScope.launch {
                        println("[SAVE EMIT CLIENT]")
                        _saveScroll.emit(Unit)
                        _messages.add(0, ormes)
                    }
                }
                else {
                    _messages.add(ormes)
                    triggerScroll()
                }

            }
        }


        // Обработка параметра totalTickets
        viewModelScope.launch {
            service.totalTickets.collect {
                totalTickets.intValue = it
            }
        }


        // Обработка INIT сообщения
        viewModelScope.launch {
            service.initMessageFlow.collect { response ->

                // Если сообщение содержит и текст и файлы, надо раздробить его на кусочки
                // Надо пройтись по всем сообщениям и раздробить при необходимости
                val messagesList = mutableListOf<Message>()
                for (message in response) {
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
        }

        // отслеживаем конец загрузки чата
        viewModelScope.launch {
            service.globalLoading.collect { response ->
                isGlobalLoading.value = response
            }
        }

        // Отслеживаем ошибки загрузки файла
        viewModelScope.launch {
            service.uploadFileService.errorFlow.collect {
                isGlobalLoading.value = false
                showTopError(it)
            }
        }
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



    fun clickStartChat(data: StartVisitorChatData) {
        service.sendStartChatMessage(data)
    }


    fun clickSendMessage(text: String) {
        service.sendMessage(message = VisitorMessage(
            text = text,
            files = listOf()
        ))
    }

    private val isConnected = mutableStateOf(false)
    fun connect() {
        if (!isConnected.value) {
            service.initConnect()
            service.startNetworkMonitoring()
            isConnected.value = true
            isGlobalLoading.value = true
        }
    }

    fun clickCloseChat() {
        service.disconnectForce()
        isConnected.value = false
    }


    fun uploadFile(uri: Uri, size: Long) {
        // Ограничиваем размер файла
        if (size / 1024 / 1024 > service.chatOptions.maxUploadFileSizeMB) {
            showTopError(ErrorKeys.FILE_MAX_SIZE)
        }
        else {
            isGlobalLoading.value = true
            service.uploadFileService.uploadFile(uri)
        }
    }

    fun showPrependMessages() {
        isGlobalLoading.value = true
        totalTickets.intValue -= 1
        service.showPrependMessages(totalTickets.intValue)
    }

    fun visitorIsTyping(text: String) {
        service.visitorIsTyping(text)
    }

    fun getTicketOptions() = service.ticketOptions
    fun getUserData() = service.userData
    fun getServerOptions() = service.serverOptions

}


data class OrientedMessage(
    val message: Message,
    var textWidth: Int = 0,
    var isLoading: Boolean = false,
    var placeHorizontal: MutableState<Boolean> = mutableStateOf(false)
)




class TextSizer(
    val density: Density,
    val resolver: FontFamily.Resolver
) {

    fun measureMessageLine(
        text: String,
        style: TextStyle
    ): Int {
        val intr = ParagraphIntrinsics(
            text = text,
            style = style,
            spanStyles = emptyList(),
            placeholders = emptyList(),
            density = density,
            fontFamilyResolver = resolver
        )
        return intr.maxIntrinsicWidth.toInt()
    }
}