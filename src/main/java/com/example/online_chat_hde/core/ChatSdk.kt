package com.example.online_chat_hde.core

import android.content.Context
import android.content.Intent
import com.example.online_chat_hde.ChatActivity
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.InitWidgetData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.VisitorData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChatSdk {
    @Volatile private var service: ChatService? = null
    @Volatile internal var defaultUi: ChatUIConfig = ChatUIConfigDefault

    fun init(
        context: Context,
        serverOptions: ServerOptions,
        chatOptions: ChatOptions,
        ticketOptions: TicketOptions = TicketOptions(),
        uiConfig: ChatUIConfig = ChatUIConfigDefault
    ) {
        service = ChatService(context.applicationContext, serverOptions, chatOptions, ticketOptions)
        defaultUi = uiConfig
    }


    fun setUser(id: String, name: String = "", email: String = "") {
        service?.setUser(VisitorData(
            id = id,
            name = name,
            email = email
        ))
    }


    fun clearUser() {
        service?.sharedPrefs?.saveUser(null)
    }


    internal fun requireService(): ChatService =
        requireNotNull(service) { "Call ChatSdk.init(...) first" }

    internal var onClickClose: () -> Unit = {}
    internal var onClickSend: (String) -> Unit = {}
    internal var onClickLoadDocument: () -> Unit = {}
    internal var onClickFile: ((FileData.Text) -> Unit)? = null
    internal var onClickImage: ((FileData.Image) -> Unit)? = null
    internal var onMessageTyping: (String) -> Unit = {}
    internal var onClickChatButton: ((ChatButton) -> Unit)? = null


    fun setOnClickEvents(
        onClickClose: () -> Unit = {},
        onClickSend: (String) -> Unit = {},
        onClickLoadDocument: () -> Unit = {},
        onClickFile: ((FileData.Text) -> Unit)? = null,
        onClickImage: ((FileData.Image) -> Unit)? = null,
        onMessageTyping: (String) -> Unit = {},
        onClickChatButton: ((ChatButton) -> Unit)? = null,
    ) {
        this.onClickClose = onClickClose
        this.onClickSend = onClickSend
        this.onClickLoadDocument = onClickLoadDocument
        this.onClickFile = onClickFile
        this.onClickImage = onClickImage
        this.onMessageTyping = onMessageTyping
        this.onClickChatButton = onClickChatButton
    }


    internal var onServerMessage: (Message.Server) -> Unit = {}
    internal var onUserMessage: (Message.User) -> Unit = {}
    internal var onInitMessage: (InitWidgetData) -> Unit = {}
    internal var onDetectUser: (VisitorData) -> Unit = {}

    fun setChatEventListeners(
        onServerMessage: (Message.Server) -> Unit = {},
        onUserMessage: (Message.User) -> Unit = {},
        onInitMessage: (InitWidgetData) -> Unit = {},
        onDetectUser: (VisitorData) -> Unit = {}
    ) {
        this.onServerMessage = onServerMessage
        this.onUserMessage = onUserMessage
        this.onInitMessage = onInitMessage
        this.onDetectUser = onDetectUser
    }


    fun openChatActivity(context: Context) {
        val i = Intent(context, ChatActivity::class.java)
        context.startActivity(i)
    }

    fun addVirtualMessage(
        name: String,
        text: String,
        time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
        files: List<FileData>? = null,
        chatButtons: List<ChatButton>? = null
    ) {
        val message = Message.Server(name = name).apply {
            this.text = text
            this.time = time
            this.files = files
            this.isVirtual = true
            this.chatButtons = chatButtons
        }
        service?.addVirtualMessage(message)
    }

    fun sendMessage(message: VisitorMessage) {
        service?.sendMessage(message)
    }

}