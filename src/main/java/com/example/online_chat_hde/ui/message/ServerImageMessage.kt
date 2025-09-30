package com.example.online_chat_hde.ui.message

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault
import com.example.online_chat_hde.ui.MessageLoading
import com.example.online_chat_hde.viewmodels.UiMessage


interface ServerMessageImageScope: ServerMessageScope {
    val imageLink: String
}

@Composable
fun ServerMessageScope.ServerImageMessageView(image: FileData.Image) {

    val interactionSource = remember { MutableInteractionSource() }

    val message = uiMessage.message
    val time = message.time


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {

        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(image.thumb)
                .crossfade(true)
                .build()
        )

        val state by painter.state.collectAsState()

        Row {

            when (state) {
                is AsyncImagePainter.State.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(uiConfig.dimensions.userImageMessageSize)
                            .clip(uiConfig.dimensions.userImageMessagesCorners)
                            .background(uiConfig.colors.userLoadingImageColor)
                    ) {
                        MessageLoading(
                            color = uiConfig.colors.userMessageText,
                            size = 40.dp
                        )
                    }
                }

                is AsyncImagePainter.State.Success -> {
                    Box(
                        modifier = Modifier
                            .clip(uiConfig.dimensions.userImageMessagesCorners)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(
                                    color = uiConfig.colors.imageRipple
                                )
                            ) {
                                onImageClick(image)
                            }
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = image.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(uiConfig.dimensions.userImageMessageSize)
                                .clip(uiConfig.dimensions.userImageMessagesCorners)
                        )
                        Box(contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier
                                .size(uiConfig.dimensions.userImageMessageSize)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(uiConfig.dimensions.timeOnImageCorners)
                                    .background(uiConfig.colors.timeOnImageBackground)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = time,
                                    color = uiConfig.colors.timeOnImageText,
                                    fontSize = uiConfig.dimensions.timeFontSize
                                )
                            }
                        }
                    }
                }

                else -> {
                    Box {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(uiConfig.dimensions.userImageMessageSize)
                                .clip(uiConfig.dimensions.userImageMessagesCorners)
                                .background(uiConfig.colors.userLoadingImageColor)
                        ) {
                            Text(text = "ОШИБКА", color = uiConfig.colors.userMessageText, fontSize = 20.sp)
                        }

                        Box(contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier
                                .size(uiConfig.dimensions.userImageMessageSize)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(uiConfig.dimensions.timeOnImageCorners)
                                    .background(uiConfig.colors.timeOnImageBackground)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = time,
                                    color = uiConfig.colors.timeOnImageText,
                                    fontSize = uiConfig.dimensions.timeFontSize
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun ServerImageMessagePreview() {

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val measureText: (text: String, fontSize: TextUnit) -> Dp = { text, fs ->
        with (density) {
            textMeasurer.measure(
                text = AnnotatedString(text),
                style = TextStyle(fontSize = fs),
                maxLines = 1,
                softWrap = false
            ).size.width.toDp()
        }
    }

    val timeWidth = measureText("00:00", ChatUIConfigDefault.dimensions.timeFontSize)

    val scopes: @Composable (UiMessage) -> ServerMessageScope = { message ->
        object : ServerMessageScope {
            override val getTextWidth = measureText
            override val uiMessage: UiMessage = message
            override val baseURL: String = ""
            override val timeWidth: Dp = timeWidth
            override val buttonMaxTextWidth: Dp =
                if (message.message.chatButtons.isNullOrEmpty()) 0.dp
                else measureText(
                    message.message.chatButtons!!.maxBy { it.text.length }.text,
                    ChatUIConfigDefault.dimensions.messageFontSize
                )
            override val onFileClick: (FileData.Text) -> Unit = {}
            override val onImageClick: (FileData.Image) -> Unit = {}
            override val onChatButtonClick: (ChatButton) -> Unit = {}
            override val showButtons: Boolean = true
            override val uiConfig: ChatUIConfig = ChatUIConfigDefault
        }
    }

    with (scopes(UiMessage(Message.Server("server").apply {
        time = "12:25"
    }))) {
        ServerImageMessageView(FileData.Image(
            thumb = "/ru/file/image_thumb/278c438bd653f82adfc93249ed059f5481b714db/size/150"
        ).apply {
            name = "mountain-landscape.jpg"
            link = "/ru/file/inline_image/278c438bd653f82adfc93249ed059f5481b714db"
        })
    }
}