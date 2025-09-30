package com.example.online_chat_hde.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.models.ChatOptions
import com.example.online_chat_hde.core.ChatClient
import com.example.online_chat_hde.viewmodels.ChatViewModel
import com.example.online_chat_hde.core.ServerOptions
import com.example.online_chat_hde.core.UploadError
import com.example.online_chat_hde.models.TicketStatus
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.RateFormat
import com.example.online_chat_hde.viewmodels.TicketViewModel
import com.example.online_chat_hde.viewmodels.UiEffect


@Composable
internal fun ChatUIScope.ChatRoute(
    chatViewModel: ChatViewModel,
    ticketViewModel: TicketViewModel,
    onClickClose: () -> Unit = {},
    onClickSend: (String) -> Unit = {},
    onClickFile: (FileData.Text) -> Unit = {},
    onClickImage: (FileData.Image) -> Unit = {},
    onMessageTyping: (String) -> Unit = {},
    onClickChatButton: (ChatButton) -> Unit = {},

    ticket: @Composable TicketScope.() -> Unit,
    chat: @Composable ChatScope.() -> Unit
) {

    val chatState by chatViewModel.state.collectAsStateWithLifecycle()

    val error = remember { mutableStateOf<UploadError?>(null) }
    LaunchedEffect(Unit) {
        chatViewModel.effects.collect { eff ->
            if (eff is UiEffect.ShowTopError) error.value = eff.error
        }
    }

    val errorFacet = remember(error.value) {
        object : ErrorBannerScope {
            override val uiConfig: ChatUIConfig = this@ChatRoute.uiConfig
            override val error: String = when (error.value) {
                is UploadError.FileTooLarge -> uiConfig.texts.errorFileTooLarge
                is UploadError.Unknown -> uiConfig.texts.errorUpload
                else -> ""
            }
        }
    }

    val topPanelFacet = remember(chatState.staff) {
        object : ChatTopPanelScope {
            override val staff = chatState.staff
            override val closeChat = onClickClose
            override val uiConfig = this@ChatRoute.uiConfig
        }
    }

    val bottomPanelFacet = remember(chatState.isConnected, chatState.messageText, chatState.filePickerExpanded) {
        object : ChatBottomPanelScope {
            override val isConnected = chatState.isConnected
            override val messageText = chatState.messageText
            override val filePickerExpanded = chatState.filePickerExpanded
            override val onMessageTextChange = chatViewModel::onMessageChange
            override val onFileExpandedChange = chatViewModel::onFilePickerExpandedChange
            override val sendMessage = chatViewModel::sendMessage
            override val uploadFile = chatViewModel::uploadFile
            override val uiConfig = this@ChatRoute.uiConfig
        }
    }

    val prependMessagesFacet = remember {
        object : PrependMessagesScope {
            override val showPrependMessages = chatViewModel::showPrependMessages
            override val uiConfig: ChatUIConfig = this@ChatRoute.uiConfig
        }
    }


    val combined by remember {
        derivedStateOf {
            buildList(chatViewModel.messages.size + chatViewModel.loadingMessages.size) {
                addAll(chatViewModel.messages)
                addAll(chatViewModel.loadingMessages)
            }.asReversed()
        }
    }

    val chatListFacet = remember(combined) {
        object : ChatListMessagesScope {
            override val messages = combined
            override val serverOptions = chatViewModel.getServerOptions()
            override val onClickFile = onClickFile
            override val onClickImage = onClickImage
            override val onClickChatButton = onClickChatButton
            override val deleteMessage = chatViewModel::deleteMessage
            override val uiConfig: ChatUIConfig = this@ChatRoute.uiConfig
        }
    }

    val ratingScope = remember(chatState.rate) {
        object : RateChatScope {
            override val info: String = chatState.rate?.template ?: ""
            override val maxRate: Int = chatState.rate?.maxScore ?: 0
            override val uiConfig: ChatUIConfig = this@ChatRoute.uiConfig
            override val onSubmit: (Int, String) -> Unit = chatViewModel::rateChat
        }
    }


    val total = chatState.totalTickets
    val loadedTicket = chatState.loadedTicket

    val chatScope = object : ChatScope {
        override val uiConfig = this@ChatRoute.uiConfig
        override val showPrepend: Boolean = total > 1 && loadedTicket < total - 1 && chatState.isConnected
        override val showError: Boolean = error.value != null
        override val isGlobalLoading: Boolean = chatState.isGlobalLoading
        override val isRating: Boolean = chatState.isRatingChat && chatState.rate != null

        override val topPanelScope = topPanelFacet
        override val bottomPanelScope = bottomPanelFacet
        override val ratingScope = ratingScope
        override val prependMessagesScope = prependMessagesFacet
        override val errorScope = errorFacet
        override val chatListScope = chatListFacet
    }

    when (chatState.ticketStatus) {
        TicketStatus.CHAT_ACTIVE -> {
            with (chatScope) {
                chat()
            }
        }
        else -> {
            TicketHost(
                ticketViewModel,
                chatState,
                onSubmit = {
                    chatViewModel.setGlobalLoading(true)
                    chatViewModel.startChat(it)
                },
                onClose = onClickClose,
                ticket = ticket
            )

        }
    }

}





interface ChatPageFrame {
    val topPanel: @Composable () -> Unit
    val globalLoading: @Composable () -> Unit
    val rating: @Composable () -> Unit
    val errorBanner: @Composable () -> Unit
    val prependMessages: @Composable () -> Unit
    val chatList: @Composable () -> Unit
    val bottomPanel: @Composable () -> Unit
}

interface ChatScope: ChatUIScope {
    val isGlobalLoading: Boolean
    val isRating: Boolean
    val showError: Boolean
    val showPrepend: Boolean

    val topPanelScope: ChatTopPanelScope
    val bottomPanelScope: ChatBottomPanelScope
    val ratingScope: RateChatScope
    val prependMessagesScope: PrependMessagesScope
    val errorScope: ErrorBannerScope
    val chatListScope: ChatListMessagesScope
}

@Composable
fun ChatScope.ChatPage(
    header: @Composable ChatTopPanelScope.() -> Unit = { ChatHeader() },
    footer: @Composable ChatBottomPanelScope.() -> Unit = { ChatFooter() },
    globalLoading: @Composable ChatUIScope.() -> Unit = { GlobalLoading() },
    rating: @Composable RateChatScope.() -> Unit = { RateChat() },
    errorBanner: @Composable ErrorBannerScope.() -> Unit = { ErrorBanner() },
    prependMessages: @Composable PrependMessagesScope.() -> Unit = { PrependMessages() },
    chatList: @Composable ChatListMessagesScope.() -> Unit = { ChatListMessages() },

    frame: @Composable ChatScope.(ChatPageFrame) -> Unit = {
        Column(modifier = Modifier.fillMaxSize()) {
            it.topPanel()
            if (isGlobalLoading) {
                it.globalLoading()
            }
            else if (isRating) {
                it.rating()
            }
            else {
                if (showError) {
                    it.errorBanner()
                }
                if (showPrepend) {
                    it.prependMessages()
                }
                Box(Modifier.weight(1f)) {
                    it.chatList()
                }
                it.bottomPanel()
            }
        }
    }
) {

    val layout = remember(topPanelScope, this, errorScope, prependMessagesScope, bottomPanelScope, chatListScope) {
        object : ChatPageFrame {
            override val topPanel: @Composable () -> Unit = { with(topPanelScope) { header() } }
            override val globalLoading: @Composable () -> Unit = { with(this@ChatPage) { globalLoading() } }
            override val rating: @Composable () -> Unit = { with(ratingScope) { rating() } }
            override val errorBanner: @Composable () -> Unit = { with(errorScope) { errorBanner() } }
            override val prependMessages: @Composable () -> Unit = { with(prependMessagesScope) { prependMessages() } }
            override val bottomPanel: @Composable () -> Unit = { with(bottomPanelScope) { footer() } }
            override val chatList: @Composable () -> Unit = { with(chatListScope) { chatList() } }
        }
    }

    frame(layout)

}


interface ErrorBannerScope: ChatUIScope {
    val error: String
}

@Composable
internal fun ErrorBannerScope.ErrorBanner(
    background: Color = uiConfig.colors.errorPrimary,
    padding: Dp = uiConfig.dimensions.topPanelPadding,
    fontSize: TextUnit = uiConfig.dimensions.messageFontSize,
    color: Color = uiConfig.colors.errorSecondary
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(padding)
    ) {
        Text(
            text = error,
            fontSize = fontSize,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}




@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
internal fun ChatPagePreview() {
    val service = ChatClient(
        serverOptions = ServerOptions(
            socketUrl = "wss://domain.com",
            originUrl = "https://domain.com",
            uploadUrl = "https://domain.com/upload",
        ),
        chatOptions = ChatOptions(
            welcomeMessage = "Hello user, chose an option",
            botName = "Support bot",
            saveUserAfterConnection = false
        ),
        context = LocalContext.current
    )
    val vm = ChatViewModel(service)
    val tvm = TicketViewModel(service)
    vm.setGlobalLoading(false)
    vm.setConnected(true)

    val scope = object : ChatUIScope {
        override val uiConfig: ChatUIConfig = ChatUIConfigDefault
    }

//    with (scope) {
//        ChatRoute(
//            chatViewModel = vm,
//            ticketViewModel = tvm,
//        )
//    }
}