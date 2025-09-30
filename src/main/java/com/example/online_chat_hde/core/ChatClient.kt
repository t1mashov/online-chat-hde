package com.example.online_chat_hde.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.example.online_chat_hde.models.ChatOptions
import com.example.online_chat_hde.models.ChatSavableData
import com.example.online_chat_hde.models.ConnectionState
import com.example.online_chat_hde.models.InitResponse
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.MessagingEvent
import com.example.online_chat_hde.models.NewMessageResponse
import com.example.online_chat_hde.models.PrependMessagesResponse
import com.example.online_chat_hde.models.RateResponse
import com.example.online_chat_hde.models.SetStaffResponse
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.UserData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.models.StartChatResponse
import com.example.online_chat_hde.models.TicketOptions
import com.example.online_chat_hde.models.UserRate
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
import org.json.JSONObject


class ChatClient(
    context: Context,
    var serverOptions: ServerOptions,
    var chatOptions: ChatOptions,
    var ticketOptions: TicketOptions = TicketOptions()
) {

    @Volatile var userData: UserData? = null

    // Состояние соединения
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.NeverConnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // События сокета
    private val _messagingEvents = MutableSharedFlow<MessagingEvent>()
    val messagingEvents: SharedFlow<MessagingEvent> = _messagingEvents

    private val appContext = context.applicationContext

    private val sharedPrefs = SharedPrefs(appContext)
    private val messageQueueService = MessagesQueueService(this, sharedPrefs)
    private val startChatQueueService = StartChatQueueService(this, sharedPrefs)
    internal val uploadFileService = UploadFileService(this, appContext)
    private val reconnectManager = ReconnectManager(this)

    // Ошибки при загрузке файла
    val errorEvents: SharedFlow<UploadError>
        get() = uploadFileService.errorFlow


    @Volatile private var socket: Socket? = null


    // Поток состояний попыток переподключения
    private val _reconnectFlow = MutableSharedFlow<ReconnectState>()
    internal val reconnectFlow: SharedFlow<ReconnectState> = _reconnectFlow

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    // Флаг показывает, намеренно ли мы сбросили соединение (если true то это мы)
    @Volatile internal var manualDisconnect = false


    fun isConnected() = socket?.connected() == true

    // отслеживание обрыва и восстановления подключения
    private val connectivityManager by lazy {
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            println("SDK[networkCallback onAvailable]")
            reconnectManager.triggerReconnect()
        }

        override fun onLost(network: Network) {
            // писать ошибку что нет соединения
            println("SDK[networkCallback onLost]")
            scope.launch {
                _connectionState.emit(ConnectionState.Disconnected)
            }
            defuseSocket()
            messageQueueService.disableQueue()
            startChatQueueService.freeQueue()
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
        println("SDK[prepareSocket userData] >>> ${userData?.toJson()}")
        if (userData != null) {
            val payload = Payload.Auth.fromVisitorData(userData!!)
            buildSocket(payload)
        }
        else if (chatOptions.saveUserAfterConnection) {
            val user = sharedPrefs.getUser()
            println("SDK[prepareSocket get user] >>> ${user?.toJson()}")
            val payload = if (user != null) {
                // получаем юзера из shared prefs
                Payload.Auth.fromVisitorData(user)
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
        println("SDK[payloadString] >>> $payloadString")

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
                    _messagingEvents.emit(MessagingEvent.User.LoadingMessage(item))
                }
            }
        }
    }

    private fun restoreSavedStaff() {
        if (chatOptions.saveUserAfterConnection) {
            sharedPrefs.getStaff()?.let {
                scope.launch {
                    _messagingEvents.emit(MessagingEvent.Server.SetStaff(SetStaffResponse.fromStaff(it)))
                }
            }
        }
    }


    fun setUser(data: UserData?) {
        userData = data
        sharedPrefs.saveUser(data)
    }

    internal fun connect() {
        if ( !isConnected()) {
            println("SDK[connect]")
            manualDisconnect = false
            socket?.connect()
            _connectionState.value = ConnectionState.Connecting
        }
    }


    fun resetReconnectionAttempts() {
        reconnectManager.resetAttempts()
    }


    internal fun attachListeners() {
        println("SDK[attachListeners] socket = $socket")
        socket?.let {
            it.off()
            it.on(Socket.EVENT_CONNECT) {
                println("SDK#[EVENT_CONNECT]")
                _connectionState.value = ConnectionState.Connected
                messageQueueService.startSendMessageQueue()
                startChatQueueService.startSend()
                reconnectManager.resetAttempts()
            }
            it.on(SocketEvents.SERVER_RESPONSE) {args ->
                println("SDK#[server-response] >>> ${args.contentToString()}")
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
                println("SDK#[EVENT_DISCONNECT] >>> ${args.contentToString()}")
                messageQueueService.disableQueue()
                startChatQueueService.freeQueue()
                _connectionState.value = ConnectionState.Disconnected
                if (!manualDisconnect) {
                    reconnectManager.triggerReconnect()
                } else {
                    manualDisconnect = false
                }
            }
            it.on(Socket.EVENT_CONNECT_ERROR) {args ->
                println("SDK#[EVENT_CONNECT_ERROR] >>> ${args.contentToString()}")
                val err = (args.firstOrNull() as? Throwable)
                    ?: RuntimeException("CONNECT_ERROR: ${args.contentToString()}")
                _connectionState.value = ConnectionState.Error(err)
                messageQueueService.disableQueue()
                startChatQueueService.freeQueue()
                reconnectManager.triggerReconnect()
            }
        }
    }



    fun initConnect() {
        disconnect()
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
    }


    fun disconnect() {
        defuseSocket()
        manualDisconnect = true
        stopNetworkMonitoring()
        _connectionState.value = ConnectionState.Disconnected
    }


    internal fun sendMessageToServer(message: VisitorMessage) {
        if (isConnected()) {
            socket?.emit(
                SocketEvents.VISITOR_MESSAGE,
                message.toJson()
            )
        }
    }


    fun sendStartChatMessage(data: StartVisitorChatData) {
        println("SDK[sendStartChatMessage] >>> ${data.toJson()}")
        sharedPrefs.setStartChatMessage(data)
        val message = VisitorMessage(text = data.message, files = listOf())
        scope.launch {
            _messagingEvents.emit(MessagingEvent.User.LoadingMessage(message))
        }
        startChatQueueService.startSend()
    }

    fun sendStartChatMessageToServer(data: StartVisitorChatData) {
        if (isConnected()) {
            println("SDK[visitor-message] >>> ['${SocketEvents.START_VISITOR_CHAT}', ${data.toJson()}]")
            userData?.name = data.name
            userData?.email = data.email
            if (chatOptions.saveUserAfterConnection) {
                sharedPrefs.saveUser(userData)
            }
            socket?.emit(
                SocketEvents.START_VISITOR_CHAT,
                data.toJson()
            )
            scope.launch { _messagingEvents.emit(MessagingEvent.User.StartVisitorChat(data)) }
        }
    }

    fun sendMessage(message: VisitorMessage, addToQueue: Boolean = true) {
        if (addToQueue) {
            sharedPrefs.addMessageToQueue(message)
        }
        scope.launch {
            _messagingEvents.emit(MessagingEvent.User.LoadingMessage(message))
        }
        messageQueueService.startSendMessageQueue()
    }

    fun rateChat(rate: UserRate) {
        socket?.emit(SocketEvents.RATE_CHAT, rate.toJson())
        scope.launch {
            _messagingEvents.emit(MessagingEvent.User.RateChat(rate))
        }
    }



    private fun defuseSocket() {
        socket?.off()
        socket?.disconnect()
        socket?.close()
        socket = null
    }



    fun sendVirtualMessage(message: Message.Server) {
        scope.launch {
            _messagingEvents.emit(MessagingEvent.Server.NewMessage(NewMessageResponse(data = message.apply { isVirtual = true })))
        }
    }

    fun showPrependMessages(ticket: Int) {
        println("SDK[load-ticket, $ticket]")
        socket?.emit(
            SocketEvents.LOAD_TICKET,
            ticket
        )
        scope.launch { _messagingEvents.emit(MessagingEvent.User.LoadTicket(ticket)) }
    }

    fun visitorIsTyping(text: String) {
        socket?.emit(
            SocketEvents.VISITOR_IS_TYPING,
            text
        )
        scope.launch { _messagingEvents.emit(MessagingEvent.User.VisitorIsTyping(text)) }
    }


    internal fun getStartChatMessage() = sharedPrefs.getStartChatMessage()
    internal fun getChatButtons() = sharedPrefs.getChatButtons()


    fun getSavedData(): ChatSavableData {
        return ChatSavableData(
            userData = userData,
            staffData = sharedPrefs.getStaff(),
            chatButtons = getChatButtons(),
            messagesQueue = sharedPrefs.getMessagesQueue(),
            startChatDatta = getStartChatMessage()
        )
    }

    fun setSavedData(data: ChatSavableData) {
        sharedPrefs.saveUser(data.userData)
        sharedPrefs.saveStaff(data.staffData)
        sharedPrefs.saveChatButtons(data.chatButtons)
        sharedPrefs.setMessagesQueue(data.messagesQueue)
        sharedPrefs.setStartChatMessage(data.startChatDatta)
    }

    fun clearSavedData() {
        userData = null
        sharedPrefs.saveUser(null)
        sharedPrefs.saveStaff(null)
        sharedPrefs.saveChatButtons(listOf())
        sharedPrefs.setMessagesQueue(listOf())
        sharedPrefs.setStartChatMessage(null)
        messageQueueService.disableQueue()
    }


    private suspend fun onServerResponse(json: JSONObject) {
        val action = json.get("action").toString()
        when (action) {
            ActionTypes.INIT_WIDGET -> {
                val initWidget = InitResponse.fromJson(json)
                _messagingEvents.emit(MessagingEvent.Server.InitWidget(initWidget))

                userData = initWidget.data.userData

                // надо сохранять полученного пользователя
                if (chatOptions.saveUserAfterConnection) {
                    sharedPrefs.saveUser(initWidget.data.userData)
                }

            }
            ActionTypes.NEW_MESSAGE -> {
                val newMessage = NewMessageResponse.fromJson(json)
                _messagingEvents.emit(MessagingEvent.Server.NewMessage(newMessage))

                when (val data = newMessage.data) {
                    is Message.User -> {
                        sharedPrefs.saveChatButtons(listOf())
                        messageQueueService.checkNextQueueMessage(newMessage)
                    }
                    is Message.Server -> {
                        if (data.chatButtons != null && data.chatButtons!!.isNotEmpty()) {
                            sharedPrefs.saveChatButtons(data.chatButtons!!)
                        }
                        else {
                            sharedPrefs.saveChatButtons(listOf())
                        }
                    }
                }

            }
            ActionTypes.PREPEND_MESSAGES -> {
                val prependMessage = PrependMessagesResponse.fromJson(json)
                _messagingEvents.emit(MessagingEvent.Server.PrependMessages(prependMessage))
            }
            ActionTypes.SET_STAFF -> {
                val setStaff = SetStaffResponse.fromJson(json)
                _messagingEvents.emit(MessagingEvent.Server.SetStaff(setStaff))
                sharedPrefs.saveStaff(setStaff.data.staff)
            }
            ActionTypes.START_CHAT -> {
                val startChat = StartChatResponse.fromJson(json)
                _messagingEvents.emit(MessagingEvent.Server.StartChat(startChat))
                sharedPrefs.saveChatButtons(listOf())
                sharedPrefs.setStartChatMessage(null)
                startChatQueueService.freeQueue()
            }
            ActionTypes.TICKET_CREATED -> {
                sharedPrefs.setStartChatMessage(null)
                startChatQueueService.freeQueue()
                _messagingEvents.emit(MessagingEvent.Server.TicketCreated)
            }
            ActionTypes.RATE_SUCCESS -> {
                val rate = RateResponse.fromJson(json)
                _messagingEvents.emit(MessagingEvent.Server.RateSuccess(rate.data))
            }
            ActionTypes.CLOSE_CHAT -> {
                _messagingEvents.emit(MessagingEvent.Server.CloseChat)
            }
        }
    }


}
