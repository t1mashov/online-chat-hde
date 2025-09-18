package com.example.online_chat_hde.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import com.example.online_chat_hde.ChatActivity
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.ChatOptions
import com.example.online_chat_hde.models.ConnectionEvent
import com.example.online_chat_hde.models.ConnectionState
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.MessagingEvent
import com.example.online_chat_hde.models.TicketOptions
import com.example.online_chat_hde.models.UserData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChatHDE {
    @Volatile private var client: ChatClient? = null
    @Volatile internal var defaultUi: ChatUIConfig = ChatUIConfigDefault

    fun init(
        context: Context,
        serverOptions: ServerOptions,
        chatOptions: ChatOptions,
        ticketOptions: TicketOptions = TicketOptions(),
        uiConfig: ChatUIConfig = ChatUIConfigDefault
    ) {
        if (client == null) {
            client = ChatClient(context.applicationContext, serverOptions, chatOptions, ticketOptions)
            defaultUi = uiConfig
        }
    }

    fun setServerOptions(options: ServerOptions) {
        requireClient().serverOptions = options
    }


    fun requireClient(): ChatClient =
        requireNotNull(client) { "Call ChatSdk.init(...) first" }

    fun chatViewModelFactory(): ViewModelProvider.Factory = ChatViewModelFactory(requireClient())

    /** события сервиса */
    val connectionEvents: SharedFlow<ConnectionEvent>
        get() = requireClient().connectionEvents

    /** События сокета */
    val messagingEvents: SharedFlow<MessagingEvent>
        get() = requireClient().messagingEvents

    /** Состояние соединения */
    val connectionState: SharedFlow<ConnectionState>
        get() = requireClient().connectionState


    internal var onMessageTyping: ((String) -> Unit)? = null

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
        client?.sendVirtualMessage(message)
    }

    fun sendMessage(message: VisitorMessage) {
        client?.sendMessage(message)
    }

    fun uploadFile(file: Uri) {
        client?.uploadFileService?.uploadFile(file)
    }





    fun setUser(user: UserData) {
        client?.setUser(user)
    }

    fun clearUser() {
        client?.setUser(null)
    }

    fun connect() {
        client?.initConnect()
    }

    fun disconnect() {
        client?.disconnect()
    }


}