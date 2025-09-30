package com.example.online_chat_hde.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.core.ButtonTypes
import com.example.online_chat_hde.core.ServerOptions
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.ui.message.ServerMessage
import com.example.online_chat_hde.ui.message.UserMessage
import com.example.online_chat_hde.ui.message.ServerMessageScope
import com.example.online_chat_hde.ui.message.StartDate
import com.example.online_chat_hde.ui.message.UserMessageScope
import com.example.online_chat_hde.viewmodels.UiMessage
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.distinctUntilChanged


@Composable
fun rememberChatListState(
    key: Any,
    initialIndex: () -> Int = { 0 }
): LazyListState {
    val saver = remember {
        Saver<LazyListState, Pair<Int, Int>>(
            save = { it.firstVisibleItemIndex to it.firstVisibleItemScrollOffset },
            restore = { (i, o) -> LazyListState(i, o) }
        )
    }
    return rememberSaveable(key, saver = saver) {
        LazyListState(
            firstVisibleItemIndex = initialIndex()
        )
    }
}

data class Anchor(val key: Any, val offset: Int)

fun LazyListState.captureAnchor(): Anchor? =
    layoutInfo.visibleItemsInfo.firstOrNull()?.let { Anchor(it.key, it.offset) }

suspend fun LazyListState.restoreAnchor(anchor: Anchor) {
    println("=[try restore anchor] >>> $anchor")
    var newOffset: Int? = null
    while (newOffset == null) {
        newOffset = layoutInfo.visibleItemsInfo.firstOrNull { it.key == anchor.key }?.offset
        if (newOffset == null) awaitFrame()
    }
    val delta = (newOffset - anchor.offset).toFloat()
    if (delta != 0f) scrollBy(-delta)
}



@Stable
interface ChatListMessagesScope: ChatUIScope {
    val messages: List<UiMessage>
//    val lazyListState: LazyListState
    val serverOptions: ServerOptions
    val onClickFile: (FileData.Text) -> Unit
    val onClickImage: (FileData.Image) -> Unit
    val onClickChatButton: ((ChatButton) -> Unit)?
    val deleteMessage: (uuid: String) -> Unit
}

@Composable
fun ChatListMessagesScope.ChatListMessages(
    serverMessage: @Composable ServerMessageScope.(UiMessage) -> Unit = { ServerMessage() },
    userMessage: @Composable UserMessageScope.(UiMessage) -> Unit = { UserMessage() },
    startDate: @Composable ChatUIScope.(date: String) -> Unit = { StartDate(it) }
) {
    val focusManager = LocalFocusManager.current

    val lazyListState = rememberChatListState(Unit) { maxOf(messages.size - 1, 0) }
    var lastAnchor by remember { mutableStateOf<Anchor?>(null) }

    val firstKey = messages.firstOrNull()?.message?.uuid
    val lastKey  = messages.lastOrNull()?.message?.uuid
    var prevFirstKey by remember(Unit) { mutableStateOf(firstKey) }
    var prevLastKey  by remember(Unit) { mutableStateOf(lastKey) }

    LaunchedEffect(messages.size, firstKey, lastKey) {
        val appended  = prevLastKey != null && lastKey != null && lastKey != prevLastKey
        val prepended = prevFirstKey != null && firstKey != null && firstKey != prevFirstKey

        when {
            prepended && lastAnchor != null -> lazyListState.restoreAnchor(lastAnchor!!)
            appended  -> lazyListState.animateScrollToItem(0)
        }
        prevFirstKey = firstKey
        prevLastKey  = lastKey
    }

    // срабатывает при любом движении скролла
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.captureAnchor() }
            .distinctUntilChanged()
            .collect {
                lastAnchor = it
                println("=[] $it")
            }
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val measureText: (text: String, fontSize: TextUnit) -> Dp = {text, fs ->
        with (density) {
            textMeasurer.measure(
                text = AnnotatedString(text),
                style = TextStyle(fontSize = fs),
                maxLines = 1,
                softWrap = false
            ).size.width.toDp()
        }
    }

    val timeWidthPx = remember(textMeasurer, density.fontScale) {
        measureText("00:00", uiConfig.dimensions.timeFontSize)
    }
    

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .background(uiConfig.colors.background)
    ) {

        LazyColumn (
            state = lazyListState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = uiConfig.dimensions.contentHorizontalPadding)
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
                val item = messages[idx]

                val messageFontSize = uiConfig.dimensions.messageFontSize
                val message = item.message

                when (message) {
                    is Message.User -> {

                        val itemScope = remember(item) {
                            object : UserMessageScope {
                                override val getTextWidth: (String, TextUnit) -> Dp = measureText
                                override val uiMessage: UiMessage = item
                                override val baseURL: String = serverOptions.originUrl
                                override val timeWidth = timeWidthPx
                                override val onFileClick = onClickFile
                                override val onImageClick = onClickImage
                                override val uiConfig = this@ChatListMessages.uiConfig
                            }
                        }

                        with (itemScope) {
                            userMessage(item)
                        }
                    }
                    is Message.Server -> {

                        val buttonMaxWidthPx: Dp = remember(item, textMeasurer, density.fontScale, messageFontSize) {
                            if (item.message.chatButtons.isNullOrEmpty()) 0.dp
                            else measureText(
                                item.message.chatButtons!!.maxBy { it.text.length }.text,
                                messageFontSize
                            )
                        }

                        val itemScope = remember(item) {
                            object : ServerMessageScope {
                                override val getTextWidth = measureText
                                override val uiMessage: UiMessage = item
                                override val baseURL: String = serverOptions.originUrl
                                override val timeWidth = timeWidthPx
                                override val buttonMaxTextWidth = buttonMaxWidthPx
                                override val onFileClick = onClickFile
                                override val onImageClick = onClickImage
                                override val onChatButtonClick: (ChatButton) -> Unit = { btn ->
                                    onClickChatButton?.let { it(btn) }
                                    if (message.isVirtual && btn.type == ButtonTypes.TEXT) {
                                        deleteMessage(message.uuid)
                                    }
                                    if (btn.hideButtons) {
                                        item.showButtons.value = false
                                    }
                                }
                                override val showButtons = (idx == 0) && item.showButtons.value
                                override val uiConfig = this@ChatListMessages.uiConfig
                            }
                        }



                        with (itemScope) {
                            serverMessage(item)
                        }

                    }
                }

                if (message.dates != null) {
                    val date = message.dates!!
                    val dateTxt = "${if (date.createdAtDate != null) date.createdAtDate+" " else ""}${uiConfig.texts.months.getOrDefault(date.createdAtName, date.createdAtName)}"
                    startDate(dateTxt)
                }


            }
        }


    }
}