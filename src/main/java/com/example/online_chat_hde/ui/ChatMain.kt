package com.example.online_chat_hde.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.online_chat_hde.core.ButtonTypes
import com.example.online_chat_hde.models.ChatOptions
import com.example.online_chat_hde.core.ChatClient
import com.example.online_chat_hde.core.ChatViewModel
import com.example.online_chat_hde.core.ServerOptions
import com.example.online_chat_hde.models.TicketStatus
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch


internal val LocalTimeSize = staticCompositionLocalOf<Int> {
    error("No char size provided")
}

@Composable
internal fun ChatMain(
    viewModel: ChatViewModel,
    uiConfig: ChatUIConfig,
    modifier: Modifier = Modifier,
    onClickClose: () -> Unit = {},
    onClickSend: (String) -> Unit = {},
    onClickLoadDocument: () -> Unit = {},
    onClickFile: (FileData.Text) -> Unit = {},
    onClickImage: (FileData.Image) -> Unit = {},
    onMessageTyping: (String) -> Unit = {},
    onClickChatButton: (ChatButton) -> Unit = {},
) {

    val ticketStatus = viewModel.ticketStatus.value.status
    println("SDK[TicketStatus] >>> $ticketStatus")
    when (ticketStatus) {
        TicketStatus.CHAT_ACTIVE -> {
            ChatPage(viewModel, uiConfig, modifier, onClickClose, onClickSend, onClickLoadDocument, onClickFile, onClickImage, onMessageTyping, onClickChatButton)
        }
        else -> {
            TicketView(viewModel, uiConfig, viewModel.getTicketOptions(), viewModel.getUserData(), ticketStatus, onClickClose) {
                // Отправка тикета -> начало чата или уведомление
                viewModel.isGlobalLoading.value = true
                viewModel.startChat(it)

            }
        }
    }

}



@Composable
internal fun ChatTopPanel(
    viewModel: ChatViewModel,
    uiConfig: ChatUIConfig,
    onClickClose: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .background(uiConfig.colors.topPanelBackground)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
        ) {
            if (viewModel.staff.value != null) {
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data("https:" + viewModel.staff.value!!.image)
                        .crossfade(true)
                        .build()
                )
                val state by painter.state.collectAsState()
                when (state) {
                    is AsyncImagePainter.State.Success -> {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            alignment = Alignment.TopStart,
                            modifier = Modifier
                                .padding(uiConfig.dimensions.topPanelPadding)
                                .size(uiConfig.dimensions.logoSize)
                                .clip(uiConfig.dimensions.staffIconCorners)
                        )
                    }
                    else -> {
                        Image(
                            imageVector = ImageVector.vectorResource(uiConfig.media.noStaffLogo),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(uiConfig.colors.topPanelText),
                            alignment = Alignment.TopStart,
                            modifier = Modifier
                                .padding(uiConfig.dimensions.topPanelPadding)
                                .size(uiConfig.dimensions.logoSize)
                        )
                    }
                }

                Text(
                    text = viewModel.staff.value!!.name,
                    color = uiConfig.colors.topPanelText,
                    fontSize = uiConfig.dimensions.messageFontSize,
                    modifier = Modifier.weight(1f)
                )
            }
            else {
                Image(
                    imageVector = ImageVector.vectorResource(uiConfig.media.noStaffLogo),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(uiConfig.colors.topPanelText),
                    alignment = Alignment.TopStart,
                    modifier = Modifier
                        .padding(uiConfig.dimensions.topPanelPadding)
                        .size(uiConfig.dimensions.logoSize)
                )

                Text(
                    text = uiConfig.texts.unassigned,
                    color = uiConfig.colors.topPanelText,
                    fontSize = uiConfig.dimensions.messageFontSize,
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier
                    .background(uiConfig.colors.closeChatButtonBackground)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(
                            color = uiConfig.colors.userRipple
                        )
                    ) {
                        onClickClose()
                        viewModel.closeChat()
                    }
                    .padding(uiConfig.dimensions.topPanelPadding)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(uiConfig.media.closeChatLogo),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(uiConfig.colors.closeChatButtonIcon),
                    modifier = Modifier.size(uiConfig.dimensions.logoSize)
                )
            }
        }

    }
}


@Composable
internal fun ChatBottomPanel(
    viewModel: ChatViewModel,
    uiConfig: ChatUIConfig,
    onClickSend: (String) -> Unit,
    onClickLoadDocument: () -> Unit,
    onMessageTyping: (String) -> Unit
) {

    var messageText by remember { mutableStateOf("") }
    val filePickerExpanded = remember { mutableStateOf(false) }

    val typingEvents = remember { Channel<String>(capacity = Channel.CONFLATED) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        var lastEmission = 0L
        typingEvents.consumeAsFlow().collect { text ->
            val now = System.currentTimeMillis()
            if (now - lastEmission >= 1000) { // не чаще 1 раза в секунду
                viewModel.visitorIsTyping(text)
                lastEmission = now
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(uiConfig.colors.bottomPanelBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(uiConfig.colors.bottomDivider)
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .padding(
                    top = 1.dp,
                )
        ) {

            if (viewModel.isConnected.value) {
                // Разрешен ввод текста
                // Прикрепить файл
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(
                                color = uiConfig.colors.userMessageBackground
                            )
                        ) {
                            filePickerExpanded.value = true
                        }
                        .padding(uiConfig.dimensions.bottomPanelPadding)
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(uiConfig.media.linkFileLogo),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(uiConfig.colors.pyperclip),
                        modifier = Modifier
                            .size(uiConfig.dimensions.logoSize)
                    )
                }
                BasicTextField(
                    value = messageText,
                    onValueChange = {
                        messageText = it
                        onMessageTyping(it)
                        scope.launch { typingEvents.send(it) }
                    },

                    textStyle = TextStyle(
                        fontSize = uiConfig.dimensions.messageFontSize,
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = uiConfig.dimensions.logoSize)
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (messageText.isEmpty()) {
                                Text(
                                    text = uiConfig.texts.messagePlaceholder,
                                    color = uiConfig.colors.messagePlaceholder,
                                    fontSize = uiConfig.dimensions.messageFontSize
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .background(uiConfig.colors.bottomPanelBackground)
                        .weight(1f)
                        .padding(
                            top = uiConfig.dimensions.bottomPanelPadding,
                            bottom = uiConfig.dimensions.bottomPanelPadding
                        )
                )
                if (messageText.trim().isNotEmpty()) {
                    // Отправить сообщение
                    Box(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    color = uiConfig.colors.userMessageBackground
                                )
                            ) {
                                onClickSend(messageText.trim())
                                messageText = ""
                            }
                            .padding(uiConfig.dimensions.bottomPanelPadding)
                    ) {
                        Image(
                            imageVector = ImageVector.vectorResource(uiConfig.media.sendMessageLogo),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(uiConfig.colors.sendMessage),
                            modifier = Modifier
                                .size(uiConfig.dimensions.logoSize)
                        )
                    }
                }
            }
            else {
                // Ошибка "Нет интернета"
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(uiConfig.colors.errorPrimary)
                        .padding(uiConfig.dimensions.bottomPanelPadding)
                        .heightIn(min = uiConfig.dimensions.logoSize)
                ) {
                    Text(
                        text = uiConfig.texts.connectionError,
                        color = uiConfig.colors.errorSecondary,
                        fontSize = uiConfig.dimensions.messageFontSize,
                        textAlign = TextAlign.Center
                    )
                }
            }

        }
    }

    FilePickerBottom(filePickerExpanded, uiConfig) { uri, size ->
        viewModel.uploadFile(uri, size)
    }
}



@Composable
internal fun ChatPage(
    viewModel: ChatViewModel,
    uiConfig: ChatUIConfig,
    modifier: Modifier = Modifier,
    onClickClose: () -> Unit,
    onClickSend: (String) -> Unit,
    onClickLoadDocument: () -> Unit,
    onClickFile: (FileData.Text) -> Unit,
    onClickImage: (FileData.Image) -> Unit,
    onMessageTyping: (String) -> Unit,
    onClickChatButton: ((ChatButton) -> Unit)?,
) {
    println("SDK[ChatPage compose]")
    val messages = viewModel.messages + viewModel.loadingMessages

    val focusManager = LocalFocusManager.current

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = maxOf(messages.size - 1, 0)
    )

    val density = LocalDensity.current
    val resolver = LocalFontFamilyResolver.current


    val firstVisible = remember { viewModel.firstVisible  }
    val offset = remember { viewModel.offset }

    LaunchedEffect(Unit) {
        viewModel.saveScroll.collect {
            firstVisible.intValue = lazyListState.firstVisibleItemIndex
            offset.intValue = lazyListState.firstVisibleItemScrollOffset
        }
    }

    var prevCount by remember { mutableIntStateOf(messages.size) }
    LaunchedEffect(messages.size) {
        if (messages.size > prevCount) {
            lazyListState.scrollToItem(firstVisible.intValue, offset.intValue)
        }
        prevCount = messages.size
    }


    LaunchedEffect(Unit) {
        viewModel.scrollToBottom.collect {
            lazyListState.scrollToItem(0)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.restoreScroll.collect {
            lazyListState.scrollToItem(firstVisible.intValue, offset.intValue)
            firstVisible.intValue = 0
            offset.intValue = 0
        }
    }


    val isNetLoading by remember { viewModel.isGlobalLoading }

    BoxWithConstraints(
        modifier = modifier
    ) {

        val maxBubbleWidthPx = with(density) { maxWidth.roundToPx() }

        LaunchedEffect(maxWidth) {
            viewModel.attachTextEnv(density, resolver, maxBubbleWidthPx, uiConfig)
            viewModel.recheckHorizontalMode()
        }



        Column(
            modifier = modifier
                .fillMaxSize()
        ) {


            // Верхняя панель
            ChatTopPanel(viewModel, uiConfig, onClickClose)

            // Основной чат
            if (isNetLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(uiConfig.colors.loadingBackground)
                ) {
                    MessageLoading(
                        uiConfig.colors.userMessageBackground,
                        uiConfig.dimensions.loadingLogoSize
                    )
                }
            }
            else {

                // Ошибки
                viewModel.errorKey.value?.let {
                    ErrorTopView(uiConfig.texts.errors.getOrDefault(it, uiConfig.texts.uploadError), uiConfig)
                }

                // Предыдущие сообщения
                if (viewModel.totalTickets.intValue > 1 && viewModel.loadedTicket.intValue < viewModel.totalTickets.intValue && viewModel.isConnected.value) {
                    PrependMessagesView(viewModel, uiConfig)
                }

                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(uiConfig.colors.background)
                        .weight(1f)
                ) {

                    LazyColumn (
                        state = lazyListState,
                        reverseLayout = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                focusManager.clearFocus()
                            }
                    ) {
                        items(
                            messages.size,
                            key = {messages[it].message.uuid}
                        ) { idx ->
                            val curIdx = messages.size - idx - 1
                            val item = messages[curIdx]
                            when (val message = item.message) {
                                is Message.User -> {
                                    UserMessageView(
                                        item,
                                        viewModel.getServerOptions().originUrl,
                                        onFileClick = onClickFile,
                                        onImageClick = onClickImage,
                                        isLoading = item.isLoading,
                                        uiConfig = uiConfig,
                                        modifier = Modifier.padding(horizontal = uiConfig.dimensions.contentHorizontalPadding),
                                    )
                                }
                                is Message.Server -> {
                                    ServerMessageView(
                                        item,
                                        viewModel.getServerOptions().originUrl,
                                        onFileClick = onClickFile,
                                        onImageClick = onClickImage,
                                        uiConfig = uiConfig,
                                        onChatButtonClick = { btn ->
                                            onClickChatButton?.let { it(btn) }
                                            if (message.isVirtual && btn.type == ButtonTypes.TEXT) {
                                                viewModel.deleteMessage(message.uuid)
                                            }
                                            if (btn.hideButtons) {
                                                item.showButtons.value = false
                                            }
                                        },
                                        showButtons = (curIdx == messages.size-1) && item.showButtons.value,
                                        modifier = Modifier.padding(horizontal = uiConfig.dimensions.contentHorizontalPadding),
                                    )
                                }
                            }
                        }
                    }


                }

                // Нижняя панель
                ChatBottomPanel(
                    viewModel, uiConfig, onClickSend, onClickLoadDocument, onMessageTyping
                )
            }


        }

    }

}



@Composable
internal fun ErrorTopView(
    error: String,
    uiConfig: ChatUIConfig
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(uiConfig.colors.errorPrimary)
            .padding(uiConfig.dimensions.topPanelPadding)
    ) {
        Text(
            text = error,
            fontSize = uiConfig.dimensions.messageFontSize,
            color = uiConfig.colors.errorSecondary,
            textAlign = TextAlign.Center
        )
    }
}



@Composable
internal fun PrependMessagesView(
    viewModel: ChatViewModel,
    uiConfig: ChatUIConfig
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    color = uiConfig.colors.topPanelBackground
                )
            ) {
                viewModel.showPrependMessages()
            }
            .background(uiConfig.colors.showPrependMessagesBackground)
            .padding(uiConfig.dimensions.topPanelPadding)
    ) {
        Text(
            text = uiConfig.texts.showPrependMessages,
            fontSize = uiConfig.dimensions.messageFontSize,
            color = uiConfig.colors.topPanelBackground
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
    vm.isGlobalLoading.value = false
    vm.isConnected.value = true

    ChatMain(
        viewModel = vm,
        uiConfig = ChatUIConfigDefault
    )
}