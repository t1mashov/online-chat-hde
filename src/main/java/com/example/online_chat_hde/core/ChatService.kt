package com.example.online_chat_hde.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.example.online_chat_hde.models.WInit
import com.example.online_chat_hde.models.InitWidgetData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.WNewMessage
import com.example.online_chat_hde.models.WPrependMessages
import com.example.online_chat_hde.models.WSetStaff
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.VisitorData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.models.WStartChat
import okhttp3.OkHttpClient
import okhttp3.Request
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject


class ChatService(
    context: Context,
    val serverOptions: ServerOptions,
    val chatOptions: ChatOptions,
    val ticketOptions: TicketOptions = TicketOptions(),
    private val listener: ChatListener = object : ChatListener {}
) {

    private val appContext = context.applicationContext

    val sharedPrefs = SharedPrefs(appContext)
    private val queueService = MessagesQueueService(this, appContext)
    internal val uploadFileService = UploadFileService(this, appContext)

    private lateinit var socket: Socket
    var userData: VisitorData? = null

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

    // триггер состояния подключения к интернету
    private val _internetAvailable = MutableSharedFlow<Boolean>()
    internal val internetAvailable: SharedFlow<Boolean> = _internetAvailable

    private val _setStaffFlow = MutableSharedFlow<WSetStaff>()
    internal val setStaffFlow: SharedFlow<WSetStaff> = _setStaffFlow

    private val _ticketDataFlow = MutableSharedFlow<TicketOptionsWithStatus>()
    internal val ticketDataFlow: SharedFlow<TicketOptionsWithStatus> = _ticketDataFlow

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


//    // отслеживание обрыва и восстановления подключения
    private val connectivityManager by lazy {
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            println("[networkCallback onAvailable]")
            connect()
            scope.launch {
                _internetAvailable.emit(true)
            }
        }

        override fun onLost(network: Network) {
            // писать ошибку что нет соединения
            socket.close()
            println("[networkCallback onLost]")
            scope.launch {
                _internetAvailable.emit(false)
            }
        }
    }

    fun startNetworkMonitoring() {
        // Автоматически отписывается при уничтожении Context
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }



    init {
        if (chatOptions.saveUserAfterConnection) {
            val userData = sharedPrefs.getUser()
            val payload = if (userData != null) {
                // получаем юзера из shared prefs
                Payload.Auth(
                    visitorId = userData.id,
                    visitorEmail = userData.email,
                    visitorName = userData.name
                )
            } else {
                // Создаем нового юзера
                Payload.NewUser()
            }

            prepare(payload)
        }
        else {
            prepare(Payload.NewUser())
        }

        if (socket.connected()) {
            // Загрузка loading сообщений в VM
            val loadingMessages = sharedPrefs.getMessagesQueue()
            scope.launch {
                for (item in loadingMessages) {
                    _loadingMessageFlow.emit(item)
                }
            }
        }
    }

    private fun prepare(payload: Payload) {
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
                userData = VisitorData.fromPayloadAuthUser(payload)
            }
            else -> {}
        }

        val payloadString = payload.encode()
        println("[payloadString] >>> $payloadString")

        val options = IO.Options().apply {
            this.reconnection = true
            this.reconnectionDelay = 2000
            this.reconnectionAttempts = Int.MAX_VALUE
            this.forceNew = true
            this.query = "type=web&payload=$payloadString"
            this.callFactory = httpClient
            this.webSocketFactory = httpClient
            this.transports = arrayOf(WebSocket.NAME)
        }

        socket = IO.socket(serverOptions.socketUrl, options)

    }


    fun setUser(data: VisitorData) {
        scope.launch {
            reconnectWithAuth(data)
        }
    }


    internal fun connect() {
        if (socket.connected()) return
        socket.connect()
    }


    fun initConnect() {
        if (socket.connected()) return

        // Удаляем все слушатели
        socket.off()
        // Навешиваем новые слушатели
        socket.on(Socket.EVENT_CONNECT) {
            println("#[EVENT_CONNECT]")
            listener.onConnect()
            queueService.startSendMessageQueue()
        }
        socket.on(SocketEvents.SERVER_RESPONSE) {args ->
            scope.launch {
                val json = when (args.size) {
                    2 -> args[1] as JSONObject
                    1 -> args[0] as JSONObject
                    else -> JSONObject()
                }
                onServerResponse(json)
            }
            listener.onServerResponse(args)
        }
        socket.on(SocketEvents.VISITOR_MESSAGE) { args ->
            println("#[visitor-message] >>> ${args.contentToString()}")
            listener.onSendMessage()
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            println("#[EVENT_DISCONNECT]")
            listener.onDisconnect()
            queueService.disableQueue()
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) {args ->
            listener.onConnectError()
            queueService.disableQueue()
            println("#[EVENT_CONNECT_ERROR] >>> ${args.contentToString()}")
        }

        connect()

        // устанавливаем сохраненного сотрудника (если был)
        sharedPrefs.getStaff()?.let {
            scope.launch {
                _setStaffFlow.emit(WSetStaff.fromStaff(it))
            }
        }
    }




    internal fun sendMessageToServer(message: VisitorMessage) {
        if (socket.connected()) {
            socket.emit(
                SocketEvents.VISITOR_MESSAGE,
                message.toJson()
            )
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
        println("[start-visitor-chat] >>> ${data.toJsonString()}")
        if (socket.connected()) {
            socket.emit(
                SocketEvents.START_VISITOR_CHAT,
                JSONObject(data.toJsonString())
            )
        }
    }

    fun sendMessage(message: VisitorMessage, addToQueue: Boolean = true) {
        if (addToQueue) {
            sharedPrefs.addMessageToQueue(message)
        }
        scope.launch {
            _loadingMessageFlow.emit(message)
            if (message.files.isNotEmpty()) {
                _globalLoadingFlow.emit(true)
            }
        }
        queueService.startSendMessageQueue()
    }


    fun disconnectForce() {
        socket.disconnect()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        socket.close()
    }


    fun isConnected(): Boolean = socket.connected()


    private val reconnectMutex = Mutex()
    private suspend fun reconnectWithAuth(data: VisitorData) = reconnectMutex.withLock {
        socket.off()
        socket.disconnect()
        socket.close()
        prepare(Payload.Auth(
            visitorId = data.id,
            visitorName = data.name,
            visitorEmail = data.email,
        ))
        initConnect()
    }


    private val virtualMessages = mutableListOf<Message.Server>()
    fun addVirtualMessage(message: Message.Server) {
        virtualMessages.add(message)
    }


    fun showPrependMessages(ticket: Int) {
        socket.emit(
            SocketEvents.LOAD_TICKET,
            ticket
        )
    }


    private suspend fun onServerResponse(json: JSONObject) {
        println("[server-message] >>> $json")
        val action = json.get("action").toString()
        when (action) {
            ActionTypes.INIT_WIDGET -> {
                val initWidget = WInit.fromJson(json)

                scope.launch {
                    _ticketDataFlow.emit(
                        TicketOptionsWithStatus(
                            options = ticketOptions,
                            status = if (initWidget.data.ticketForm) TicketStatus.STAFF_OFFLINE
                                     else if (sharedPrefs.getStartChatMessage() != null) TicketStatus.DISABLED
                                     else if (initWidget.data is InitWidgetData.First) TicketStatus.FIRST_MESSAGE
                                     else TicketStatus.DISABLED
                        )
                    )
                }

                when (val data = initWidget.data) {
                    is InitWidgetData.First -> {
                        // надо сохранять полученного пользователя
                        if (chatOptions.saveUserAfterConnection) {
                            sharedPrefs.saveUser(
                                VisitorData(
                                    id = data.visitorData.id,
                                    name = data.visitorData.name,
                                    email = data.visitorData.email,
                                )
                            )
                        }

                        if (userData == null) {
                            reconnectWithAuth(data.visitorData)
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
                        _globalLoadingFlow.emit(false)
                    }
                    is InitWidgetData.Progress -> {

                        println("[TOTAL_TICKETS] >>> ${data.widgetChat.totalTickets}")
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

                ChatSdk.onInitMessage(initWidget.data)
            }
            ActionTypes.NEW_MESSAGE -> {
                _globalLoadingFlow.emit(false)
                val newMessage = WNewMessage.fromJson(json)

                when (val data = newMessage.data) {
                    is Message.User -> {
                        ChatSdk.onUserMessage(data)
                        sharedPrefs.saveChatButtons(listOf())
                        _newUserMessageFlow.emit(data)
                        queueService.checkNextQueueMessage(newMessage)
                    }
                    is Message.Server -> {
                        ChatSdk.onServerMessage(data)
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
                val prependMessage = WPrependMessages.fromJson(json)
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
                val setStaff = WSetStaff.fromJson(json)
                scope.launch {
                    _setStaffFlow.emit(setStaff)
                }
                sharedPrefs.saveStaff(setStaff.data.staff)
            }
            ActionTypes.START_CHAT -> {
                val startChat = WStartChat.fromJson(json)
                _globalLoadingFlow.emit(false)
                ChatSdk.onUserMessage(startChat.data)
                sharedPrefs.saveChatButtons(listOf())
                _newUserMessageFlow.emit(startChat.data)
                sharedPrefs.setStartChatMessage(null)
                queueService.checkNextQueueMessage(startChat.toWNewMessage())
            }
            ActionTypes.TICKET_CREATED -> {
                sharedPrefs.setStartChatMessage(null)
            }
        }

        if (virtualMessages.isNotEmpty()) {
            virtualMessages.forEach {
                _newServerMessageFlow.emit(it)
            }
            virtualMessages.clear()
        }
    }


}
