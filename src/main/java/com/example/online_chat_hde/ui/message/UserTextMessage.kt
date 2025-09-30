package com.example.online_chat_hde.ui.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.viewmodels.UiMessage
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault
import com.example.online_chat_hde.ui.MessageLoading



@Composable
fun UserMessageScope.UserTextMessage() {

    val isLoading = uiMessage.isLoading

    val leftIndent = uiConfig.dimensions.messageMinEndIndent

    val contentStyle = TextStyle(
        fontSize = uiConfig.dimensions.messageFontSize,
        color = uiConfig.colors.userMessageText
    )

    val timeStyle = TextStyle(
        fontSize = uiConfig.dimensions.timeFontSize,
        color = uiConfig.colors.userTimeText
    )

    val text = uiMessage.message.text

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {
        Row(Modifier.fillMaxWidth()) {

            Box(Modifier.width(leftIndent))

            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {

                val textWidth = getTextWidth(
                    if (uiMessage.message.files.isNullOrEmpty()) text
                    else uiMessage.message.files!!.maxBy { it.name }.name,
                    uiConfig.dimensions.messageFontSize
                )
                val maxBoxWidth = maxWidth
                val innerSpace = uiConfig.dimensions.innerIndent
                val padding = uiConfig.dimensions.messagePadding

                val placeHorizontal = textWidth + innerSpace + timeWidth + padding*2 <= maxBoxWidth

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = uiConfig.dimensions.userTextMessagesCorners,
                        color = uiConfig.colors.userMessageBackground,
                        tonalElevation = 0.dp
                    ) {
                        if (placeHorizontal) {
                            Row(
                                modifier = Modifier.padding(padding),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(text = uiMessage.message.text, style = contentStyle)
                                Spacer(Modifier.width(innerSpace))
                                if (!isLoading) {
                                    Text(text = uiMessage.message.time, style = timeStyle)
                                } else {
                                    MessageLoading(
                                        uiConfig.colors.userTimeText,
                                        uiConfig.dimensions.messageLoadingIconSize
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(padding),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(text = uiMessage.message.text, style = contentStyle)
                                Spacer(Modifier.height(innerSpace))
                                if (!isLoading) {
                                    Text(text = uiMessage.message.time, style = timeStyle)
                                } else {
                                    MessageLoading(
                                        uiConfig.colors.userTimeText,
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
fun PreviewUserTextMessage() {

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val measureText: (text: String, fontSize: TextUnit) -> Dp = { text, fs ->
        with (density) {
            textMeasurer.measure(
                text = AnnotatedString("00:00"),
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
            override val uiMessage: UiMessage = UiMessage(
                Message.User ().apply {
                    text = "hello world qwerty asdfghj zxcvbnm 1234123235"
                    time = "12:45"
                },
            )
            override val baseURL: String = ""
            override val timeWidth: Dp = timeWidth
            override val onFileClick: (FileData.Text) -> Unit = {}
            override val onImageClick: (FileData.Image) -> Unit = {}
            override val uiConfig = ChatUIConfigDefault
        }


    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        with (itemScope) {
            UserTextMessage()
        }
//        UserTextMessage(
//            uiMessage = UiMessage(
//                message = Message.User().apply {
//                    text = "Lorem ipsum doloc za dom braco za slobodu, boremo se mi, csuite srpski dobrovolci, bando csetnice...\nstichize vas nasa ruka i u srbii"
//                    time = "18:58"
//                },
//            ),
//            uiConfig = ChatUIConfigDefault,
//        )
    }

}

