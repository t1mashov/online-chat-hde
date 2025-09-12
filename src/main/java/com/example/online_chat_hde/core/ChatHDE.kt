package com.example.online_chat_hde.core

import android.content.Context
import android.content.Intent
import com.example.online_chat_hde.ChatActivity
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.InitWidgetData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.UserData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChatHDE {
    @Volatile private var service: ChatService? = null
    @Volatile internal var defaultUi: ChatUIConfig = ChatUIConfigDefault

    fun init(
        context: Context,
        serverOptions: ServerOptions,
        chatOptions: ChatOptions,
        userData: UserData? = null,
        ticketOptions: TicketOptions = TicketOptions(),
        uiConfig: ChatUIConfig = ChatUIConfigDefault
    ) {
        service = ChatService(context.applicationContext, serverOptions, chatOptions, ticketOptions, userData)
        defaultUi = uiConfig
    }


    fun requireService(): ChatService =
        requireNotNull(service) { "Call ChatSdk.init(...) first" }

    /** Поток событий сервиса */
    val events: SharedFlow<ChatEvent>
        get() = requireService().events

    internal var onMessageTyping: ((String) -> Unit)? = null
    internal var onClickClose: (() -> Unit)? = null

    internal var clickSendAction: ((String) -> Unit)? = null
    internal var clickLoadDocumentAction: (() -> Unit)? = null
    internal var clickFileAction: ((FileData.Text) -> Unit)? = null
    internal var clickImageAction: ((FileData.Image) -> Unit)? = null
    internal var clickChatButtonAction: ((ChatButton) -> Unit)? = null

    var clickSendActionDefault: (String) -> Unit = {}
        internal set
    var clickLoadDocumentActionDefault: () -> Unit = {}
        internal set
    var clickFileActionDefault: (FileData.Text) -> Unit = {}
        internal set
    var clickImageActionDefault: (FileData.Image) -> Unit = {}
        internal set
    var clickChatButtonActionDefault: (ChatButton) -> Unit = {}
        internal set


    fun setClickActions(
        clickSendAction: ((String) -> Unit)? = null,
        clickLoadDocumentAction: (() -> Unit)? = null,
        clickFileAction: ((FileData.Text) -> Unit)? = null,
        clickImageAction: ((FileData.Image) -> Unit)? = null,
        clickChatButtonAction: ((ChatButton) -> Unit)? = null,
    ) {
        this.clickSendAction = clickSendAction
        this.clickLoadDocumentAction = clickLoadDocumentAction
        this.clickFileAction = clickFileAction
        this.clickImageAction = clickImageAction
        this.clickChatButtonAction = clickChatButtonAction
    }


    internal var onServerMessage: ((Message.Server) -> Unit)? = null
    internal var onUserMessage: ((Message.User) -> Unit)? = null
    internal var onInitMessage: ((InitWidgetData) -> Unit)? = null
    internal var onDetectUser: ((UserData) -> Unit)? = null

    fun setChatEventListeners(
        onServerMessage: (Message.Server) -> Unit = {},
        onUserMessage: (Message.User) -> Unit = {},
        onInitMessage: (InitWidgetData) -> Unit = {},
        onDetectUser: (UserData) -> Unit = {}
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

    fun sendVirtualMessage(
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
        service?.sendVirtualMessage(message)
    }

    fun sendMessage(message: VisitorMessage) {
        service?.sendMessage(message)
    }





    fun setUser(user: UserData?) {
        service?.setUser(user)
    }

    fun connect() {
        service?.initConnect()
    }

    fun disconnect() {
        service?.disconnect()
    }


}