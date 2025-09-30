package com.example.online_chat_hde.ui.message

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.R
import com.example.online_chat_hde.viewmodels.UiMessage
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault
import com.example.online_chat_hde.ui.MessageLoading




@Composable
fun UserMessageScope.UserFileMessage(file: FileData.Text) {

    val isLoading = uiMessage.isLoading
    val interactionSource = remember { MutableInteractionSource() }

    val leftIndent = uiConfig.dimensions.messageMinEndIndent
    val time = uiMessage.message.time

    BoxWithConstraints (
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {

        Row(modifier = Modifier.fillMaxWidth()) {
            Box (modifier = Modifier.width(leftIndent))

            BoxWithConstraints {

                val textWidth = getTextWidth(
                    file.name,
                    uiConfig.dimensions.messageFontSize
                )
                val maxBoxWidth = maxWidth
                val innerSpace = uiConfig.dimensions.innerIndent
                val padding = uiConfig.dimensions.messagePadding
                val pyperclip = uiConfig.dimensions.pyperclipSize

                val placeHorizontal = pyperclip + textWidth + innerSpace*2 + timeWidth + padding*2 <= maxBoxWidth


                if (placeHorizontal) {
                    Row {
                        Box(modifier = Modifier.weight(1f))
                        Row {
                            Box(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(uiConfig.dimensions.userTextMessagesCorners)
                                    .background(color = uiConfig.colors.userMessageBackground,)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(color = uiConfig.colors.userRipple)
                                    ) {
                                        onFileClick(file)
                                    }
                                    .padding(padding)
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Image(
                                        imageVector = ImageVector.vectorResource(R.drawable.piperclip),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(uiConfig.colors.userMessageText),
                                        modifier = Modifier
                                            .size(uiConfig.dimensions.pyperclipSize)
                                            .padding(end = uiConfig.dimensions.innerIndent / 2)
                                    )
                                    Text(
                                        text = file.name,
                                        color = uiConfig.colors.userMessageText,
                                        fontSize = uiConfig.dimensions.messageFontSize,
                                        textDecoration = TextDecoration.Underline
                                    )

                                    Spacer(modifier = Modifier.width(innerSpace))
                                    if (!isLoading) {
                                        Text(
                                            text = time,
                                            color = uiConfig.colors.userTimeText,
                                            fontSize = uiConfig.dimensions.timeFontSize
                                        )
                                    } else {
                                        MessageLoading(
                                            uiConfig.colors.userMessageText,
                                            uiConfig.dimensions.messageLoadingIconSize
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    Row {
                        Box(modifier = Modifier.weight(1f))
                        Box (
                            modifier = Modifier
                                .clip(uiConfig.dimensions.userTextMessagesCorners)
                                .background(color = uiConfig.colors.userMessageBackground)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = ripple(color = uiConfig.colors.userRipple)
                                ) {
                                    onFileClick(file)
                                }
                                .padding(padding)
                        ) {
                            Column (horizontalAlignment = Alignment.End) {
                                Row {
                                    Image(
                                        imageVector = ImageVector.vectorResource(R.drawable.piperclip),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(uiConfig.colors.userMessageText),
                                        modifier = Modifier
                                            .size(uiConfig.dimensions.pyperclipSize)
                                            .padding(end = uiConfig.dimensions.innerIndent / 2)
                                    )
                                    Text(
                                        text = file.name,
                                        color = uiConfig.colors.userMessageText,
                                        fontSize = uiConfig.dimensions.messageFontSize,
                                        textDecoration = TextDecoration.Underline
                                    )
                                }
                                Spacer(modifier = Modifier.height(innerSpace))
                                if (!isLoading) {
                                    Text(
                                        text = time,
                                        color = uiConfig.colors.userTimeText,
                                        fontSize = uiConfig.dimensions.timeFontSize
                                    )
                                }
                                else {
                                    MessageLoading(
                                        uiConfig.colors.userMessageText,
                                        uiConfig.dimensions.messageLoadingIconSize
                                    )
                                }
                            }
                        }
                    }
                }
            }


        }
    }


}




@Preview(showBackground = true)
@Composable
internal fun PreviewUserFileMessage() {

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


    val itemScope =
        object : UserMessageScope {
            override val getTextWidth: (text: String, fontSize: TextUnit) -> Dp = measureText
            override val uiMessage: UiMessage = UiMessage(Message.User().apply {
                time = "19:45"
            })
            override val baseURL: String = ""
            override val timeWidth: Dp = timeWidth
            override val onFileClick: (FileData.Text) -> Unit = {}
            override val onImageClick: (FileData.Image) -> Unit = {}
            override val uiConfig = ChatUIConfigDefault
        }


    with (itemScope) {
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            UserFileMessage(FileData.Text().apply { name = "hello.txt" })
            UserFileMessage(FileData.Text().apply { name = "hello everybody how are you doing 12345678.txt" })
        }
    }

}

