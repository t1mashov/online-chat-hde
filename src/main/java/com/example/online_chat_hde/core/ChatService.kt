package com.example.online_chat_hde.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.example.online_chat_hde.models.InitResponse
import com.example.online_chat_hde.models.InitWidgetData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.NewMessageResponse
import com.example.online_chat_hde.models.PrependMessagesResponse
import com.example.online_chat_hde.models.SetStaffResponse
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.UserData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.models.StartChatResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.json.JSONObject


class ChatService(
    context: Context,
    val serverOptions: ServerOptions,
    val chatOptions: ChatOptions,
    val ticketOptions: TicketOptions = TicketOptions(),
    @Volatile var userData: UserData? = null
) {

    private val _connection = MutableStateFlow<ConnectionState>(ConnectionState.NeverConnected)
    val connection: StateFlow<ConnectionState> = _connection.asStateFlow()

    private val appContext = context.applicationContext

    val sharedPrefs = SharedPrefs(appContext)
    private val queueService = MessagesQueueService(this, appContext)
    internal val uploadFileService = UploadFileService(this, appContext)
    private val reconnectManager = ReconnectManager(this)


    // События сокета + ответы сервера
    private val _events = MutableSharedFlow<ChatEvent>()
    val events: SharedFlow<ChatEvent> = _events

    @Volatile private var socket: Socket? = null

    // сообщения, отправленные клиентом (загружаются)
    private val _loadingMessageFlow = MutableSharedFlow<VisitorMessage>()
    internal val loadingMessageFlow: SharedFlow<VisitorMessage> = _loadingMessageFlow

    // сообщения пользователя (от сервера)
    private val _newUserMessageFlow = MutableSharedFlow<Message.User>()
    internal val newUserMessageFlow: SharedFlow<Message.User> = _newUserMessageFlow

    // сообщения сотрудников (от сервера)
    private val _newServerMessageFlow = MutableSharedFlow<Message.Server>()
    internal val newServerMessageFlow: SharedFlow<Message.Server> = _newServerMessageFlow

    // первое сообщение от сервера после подключения
    private val _initMessageFlow = MutableSharedFlow<List<Message>>()
    internal val initMessageFlow: SharedFlow<List<Message>> = _initMessageFlow

    // передача параметра totalTickets
    private val _totalTickets = MutableSharedFlow<Int>()
    internal val totalTickets: SharedFlow<Int> = _totalTickets

    // триггер загрузки контента
    private val _globalLoadingFlow = MutableSharedFlow<Boolean>()
    internal val globalLoading: SharedFlow<Boolean> = _globalLoadingFlow

    private val _setStaffFlow = MutableSharedFlow<SetStaffResponse>()
    internal val setStaffFlow: SharedFlow<SetStaffResponse> = _setStaffFlow

    private val _ticketDataFlow = MutableSharedFlow<TicketOptionsWithStatus>()
    internal val ticketDataFlow: SharedFlow<TicketOptionsWithStatus> = _ticketDataFlow

    // Поток состояний попыток переподключения
    private val _reconnectFlow = MutableSharedFlow<ReconnectState>()
    internal val reconnectFlow: SharedFlow<ReconnectState> = _reconnectFlow


    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val mutex = Mutex()

    // Флаг показывает, намеренно ли мы сбросили соединение (если true то это мы)
    @Volatile internal var manualDisconnect = false



    fun isConnected() = socket?.connected() == true

    // отслеживание обрыва и восстановления подключения
    private val connectivityManager by lazy {
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            println("[networkCallback onAvailable]")
            reconnectManager.triggerReconnect()
        }

        override fun onLost(network: Network) {
            // писать ошибку что нет соединения
            println("[networkCallback onLost]")
            scope.launch {
                _connection.emit(ConnectionState.Disconnected)
            }
            defuseSocket()
            queueService.disableQueue()
        }
    }

    private fun startNetworkMonitoring() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }
    private fun stopNetworkMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) { }
    }



    private fun prepareSocket() {
        if (userData != null) {
            val payload = Payload.Auth.fromVisitorData(userData!!)
            buildSocket(payload)
        }
        else if (chatOptions.saveUserAfterConnection) {
            val userData = sharedPrefs.getUser()
            val payload = if (userData != null) {
                // получаем юзера из shared prefs
                Payload.Auth.fromVisitorData(userData)
            } else {
                // Создаем нового юзера
                Payload.NewUser()
            }

            buildSocket(payload)
        }
        else {
            buildSocket(Payload.NewUser())
        }

    }


    internal fun buildSocket(payload: Payload) {
        // обезвреживаем предыдущий сокет (если был)
        defuseSocket()

        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("origin", serverOptions.originUrl)
                    .build()
                chain.proceed(request)
            }
            .build()

        when (payload) {
            is Payload.Auth -> {
                userData = UserData.fromPayloadAuthUser(payload)
            }
            else -> {}
        }

        val payloadString = payload.encode()
        println("[payloadString] >>> $payloadString")

        val options = IO.Options().apply {
            this.reconnection = false
            this.forceNew = true
            this.query = "type=web&payload=$payloadString"
            this.callFactory = httpClient
            this.webSocketFactory = httpClient
            this.transports = arrayOf(WebSocket.NAME)
        }

        socket = IO.socket(serverOptions.socketUrl, options)
    }


    private fun restoreSavedLoadingMessages() {
        if (isConnected()) {
            val loadingMessages = sharedPrefs.getMessagesQueue()
            scope.launch {
                for (item in loadingMessages) {
                    _loadingMessageFlow.emit(item)
                }
            }
        }
    }

    private fun restoreSavedStaff() {
        if (chatOptions.saveUserAfterConnection) {
            sharedPrefs.getStaff()?.let {
                scope.launch {
                    _setStaffFlow.emit(SetStaffResponse.fromStaff(it))
                }
            }
        }
    }


    fun setUser(data: UserData?) {
        userData = data
    }

    internal fun connect() {
        if ( !isConnected()) {
            println("[connect]")
            manualDisconnect = false
            socket?.connect()
            _connection.value = ConnectionState.Connecting
        }
    }


    fun resetReconnectionAttempts() {
        reconnectManager.resetAttempts()
    }


    internal fun attachListeners() {
        println("[attachListeners] socket = $socket")
        socket?.let {
            it.off()
            it.on(Socket.EVENT_CONNECT) {
                println("#[EVENT_CONNECT]")
                _connection.value = ConnectionState.Connected
                queueService.startSendMessageQueue()
                scope.launch { _events.emit(ChatEvent.Connected) }
                reconnectManager.resetAttempts()
            }
            it.on(SocketEvents.SERVER_RESPONSE) {args ->
                scope.launch {
                    val json = when (args.size) {
                        2 -> args[1] as JSONObject
                        1 -> args[0] as JSONObject
                        else -> JSONObject()
                    }
                    onServerResponse(json)
                }
            }
            it.on(Socket.EVENT_DISCONNECT) { args ->
                println("#[EVENT_DISCONNECT] >>> ${args.contentToString()}")
                queueService.disableQueue()
                _connection.value = ConnectionState.Disconnected
                if (!manualDisconnect) {
                    reconnectManager.triggerReconnect()
                } else {
                    manualDisconnect = false
                }
                scope.launch { _events.emit(ChatEvent.Disconnected(args.toList())) }
            }
            it.on(Socket.EVENT_CONNECT_ERROR) {args ->
                println("#[EVENT_CONNECT_ERROR] >>> ${args.contentToString()}")
                val err = (args.firstOrNull() as? Throwable)
                    ?: RuntimeException("CONNECT_ERROR: ${args.contentToString()}")
                _connection.value = ConnectionState.Error(err)
                scope.launch { _events.emit(ChatEvent.ConnectError(args.toList())) }
                queueService.disableQueue()
                reconnectManager.triggerReconnect()
            }
        }
    }



    fun initConnect() {
        if (isConnected()) return
        println("[init-connect]")

        prepareSocket()
        attachListeners()
        connect()

        // устанавливаем сохраненного сотрудника (если был)
        restoreSavedStaff()
        // Загрузка loading сообщений в VM
        restoreSavedLoadingMessages()
        // Запуск мониторинга доступности сети
        startNetworkMonitoring()

        scope.launch { _globalLoadingFlow.emit(true) }
    }


    fun disconnect() {
        defuseSocket()
        userData = null
        manualDisconnect = true
        stopNetworkMonitoring()
        _connection.value = ConnectionState.Disconnected
    }


    internal fun sendMessageToServer(message: VisitorMessage) {
        if (isConnected()) {
            socket?.emit(
                SocketEvents.VISITOR_MESSAGE,
                message.toJson()
            )
            scope.launch { _events.emit(ChatEvent.UserMessage(UserMessageEvent.Message(message))) }
        }
    }


    fun sendStartChatMessage(data: StartVisitorChatData) {
        sharedPrefs.setStartChatMessage(data)
        val message = VisitorMessage(text = data.message, files = listOf())
        scope.launch {
            _loadingMessageFlow.emit(message)
            if (message.files.isNotEmpty()) {
                _globalLoadingFlow.emit(true)
            }
        }
        queueService.startSendMessageQueue()
    }

    fun sendStartChatMessageToServer(data: StartVisitorChatData) {
        if (isConnected()) {
            socket?.emit(
                SocketEvents.START_VISITOR_CHAT,
                JSONObject(data.toJsonString())
            )
            scope.launch { _events.emit(ChatEvent.UserMessage(UserMessageEvent.StartVisitorChat(data))) }
        }
    }

    fun sendMessage(message: VisitorMessage, addToQueue: Boolean = true) {
        if (addToQueue) {
            sharedPrefs.addMessageToQueue(message)
        }
        scope.launch {
            _loadingMessageFlow.emit(message)
        }
        queueService.startSendMessageQueue()
    }



    private fun defuseSocket() {
        socket?.off()
        socket?.disconnect()
        socket?.close()
        socket = null
    }



    fun sendVirtualMessage(message: Message.Server) {
        scope.launch {
            _newServerMessageFlow.emit(message)
        }
    }

    fun showPrependMessages(ticket: Int) {
        socket?.emit(
            SocketEvents.LOAD_TICKET,
            ticket
        )
        scope.launch { _events.emit(ChatEvent.UserMessage(UserMessageEvent.LoadTicket(ticket))) }
    }

    fun visitorIsTyping(text: String) {
        socket?.emit(
            SocketEvents.VISITOR_IS_TYPING,
            text
        )
        scope.launch { _events.emit(ChatEvent.UserMessage(UserMessageEvent.VisitorIsTyping(text))) }
    }


    private suspend fun onServerResponse(json: JSONObject) {
        println("[server-message] >>> $json")
        val action = json.get("action").toString()
        when (action) {
            ActionTypes.INIT_WIDGET -> {
                val initWidget = InitResponse.fromJson(json)
                _events.emit(ChatEvent.ServerResponse(ServerResponseEvent.InitWidget(initWidget)))

                val disabledTicket = ticketOptions.consentLink==null && !ticketOptions.showEmailField && !ticketOptions.showNameField

                scope.launch {
                    _ticketDataFlow.emit(
                        TicketOptionsWithStatus(
                            options = ticketOptions,
                            status = if (initWidget.data.ticketForm) TicketStatus.STAFF_OFFLINE
                                     else if (sharedPrefs.getStartChatMessage() != null || disabledTicket) TicketStatus.DISABLED
                                     else if (initWidget.data is InitWidgetData.First) TicketStatus.FIRST_MESSAGE
                                     else TicketStatus.DISABLED
                        )
                    )
                }

                when (val data = initWidget.data) {
                    is InitWidgetData.First -> {
                        ChatHDE.onDetectUser?.invoke(data.userData)
                        userData = data.userData

                        // надо сохранять полученного пользователя
                        if (chatOptions.saveUserAfterConnection) {
                            sharedPrefs.saveUser(
                                data.userData
                            )
                        }

                        data.initialChatButtons?.let {
                            _initMessageFlow.emit(
                                listOf(Message.Server(name = chatOptions.botName).apply {
                                    chatButtons = it
                                    isVirtual = true
                                    text = chatOptions.welcomeMessage
                                })
                            )
                        }
                        _totalTickets.emit(1)
                        _globalLoadingFlow.emit(false)
                    }
                    is InitWidgetData.Progress -> {

                        // Сохраняем VisitorData
                        if (chatOptions.saveUserAfterConnection) {
                            sharedPrefs.saveUser(data.visitorData)
                        }
                        userData = data.visitorData

                        // Отправляем кол-во тикетов в VM
                        _totalTickets.emit(data.widgetChat.totalTickets)

                        // Добавляем кнопки к последнему сообщению если есть
                        val savedButtons = sharedPrefs.getChatButtons()
                        if (savedButtons.isNotEmpty()) {
                            data.widgetChat.messages.lastOrNull()?.let {
                                it.chatButtons = savedButtons
                            }
                        }
                        // Отправляем во ViewModel
                        _initMessageFlow.emit(data.widgetChat.messages)
                    }

                }

                ChatHDE.onInitMessage?.invoke(initWidget.data)
            }
            ActionTypes.NEW_MESSAGE -> {
                _globalLoadingFlow.emit(false)
                val newMessage = NewMessageResponse.fromJson(json)
                _events.emit(ChatEvent.ServerResponse(ServerResponseEvent.NewMessage(newMessage)))

                when (val data = newMessage.data) {
                    is Message.User -> {
                        ChatHDE.onUserMessage?.invoke(data)
                        sharedPrefs.saveChatButtons(listOf())
                        _newUserMessageFlow.emit(data)
                        queueService.checkNextQueueMessage(newMessage)
                    }
                    is Message.Server -> {
                        ChatHDE.onServerMessage?.invoke(data)
                        if (data.chatButtons != null && data.chatButtons!!.isNotEmpty()) {
                            sharedPrefs.saveChatButtons(data.chatButtons!!)
                        }
                        else {
                            sharedPrefs.saveChatButtons(listOf())
                        }
                        _newServerMessageFlow.emit(data)
                    }
                }

            }
            ActionTypes.PREPEND_MESSAGES -> {
                val prependMessage = PrependMessagesResponse.fromJson(json)
                _events.emit(ChatEvent.ServerResponse(ServerResponseEvent.PrependMessages(prependMessage)))
                if (prependMessage.data != null) {
                    for (message in prependMessage.data.messages.reversed()) {
                        when (message) {
                            is Message.Server -> {
                                _newServerMessageFlow.emit(message.apply { isPrepend = true })
                            }
                            is Message.User -> {
                                _newUserMessageFlow.emit(message.apply { isPrepend = true })
                            }
                        }
                    }
                }
                _globalLoadingFlow.emit(false)
            }
            ActionTypes.SET_STAFF -> {
                val setStaff = SetStaffResponse.fromJson(json)
                _events.emit(ChatEvent.ServerResponse(ServerResponseEvent.SetStaff(setStaff)))
                scope.launch {
                    _setStaffFlow.emit(setStaff)
                }
                sharedPrefs.saveStaff(setStaff.data.staff)
            }
            ActionTypes.START_CHAT -> {
                val startChat = StartChatResponse.fromJson(json)
                _events.emit(ChatEvent.ServerResponse(ServerResponseEvent.StartChat(startChat)))
                _globalLoadingFlow.emit(false)
                ChatHDE.onUserMessage?.invoke(startChat.data)
                sharedPrefs.saveChatButtons(listOf())
                _newUserMessageFlow.emit(startChat.data)
                sharedPrefs.setStartChatMessage(null)
                queueService.checkNextQueueMessage(startChat.toNewMessageResponse())
            }
            ActionTypes.TICKET_CREATED -> {
                sharedPrefs.setStartChatMessage(null)
                _events.emit(ChatEvent.ServerResponse(ServerResponseEvent.TicketCreated))
            }
        }
    }


}
