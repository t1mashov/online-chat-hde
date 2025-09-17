package com.example.online_chat_hde.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.online_chat_hde.core.OrientedMessage
import com.example.online_chat_hde.models.Message





@Composable
fun UserTextMessageView(
    orientedMessage: OrientedMessage,
    uiConfig: ChatUIConfig,
    isLoading: Boolean = false,
) {
    val message = orientedMessage.message as Message.User

    val textStyle = TextStyle(
        fontSize = uiConfig.dimensions.messageFontSize,
        color = uiConfig.colors.userMessageText
    )
    val timeStyle = TextStyle(
        fontSize = uiConfig.dimensions.timeFontSize,
        color = uiConfig.colors.userTimeText
    )


    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {
        Row(Modifier.fillMaxWidth()) {
            // левый отступ
            Box(Modifier.width(uiConfig.dimensions.messageMinEndIndent))

            BoxWithConstraints(Modifier.weight(1f)) {
                // ограничим максимальную ширину пузыря, чтобы он не растягивался на всю
                val maxBubbleWidth = maxWidth
                val innerSpace = uiConfig.dimensions.innerIndent


                Row {

                    Spacer(Modifier.weight(1f))

                    Surface(
                        shape = uiConfig.dimensions.userTextMessagesCorners,
                        color = uiConfig.colors.userMessageBackground,
                        tonalElevation = 0.dp
                    ) {
                        if (orientedMessage.placeHorizontal.value) {
                            Row(
                                modifier = Modifier
                                    .widthIn(max = maxBubbleWidth)
                                    .padding(uiConfig.dimensions.messagePadding),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = message.text,
                                    style = textStyle
                                )
                                Spacer(Modifier.width(innerSpace))
                                if (!isLoading) {
                                    Text(
                                        text = message.time,
                                        style = timeStyle
                                    )
                                } else {
                                    MessageLoading(
                                        uiConfig.colors.userMessageText,
                                        uiConfig.dimensions.messageLoadingIconSize
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = maxBubbleWidth)
                                    .padding(uiConfig.dimensions.messagePadding),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = message.text,
                                    style = textStyle
                                )
                                Spacer(Modifier.height(innerSpace))
                                if (!isLoading) {
                                    Text(
                                        text = message.time,
                                        style = timeStyle
                                    )
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

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        UserTextMessageView(
            orientedMessage = OrientedMessage(
                Message.User ().apply {
                    text = "hello"
                    time = "12:45"
                },
            ),
            uiConfig = ChatUIConfigDefault,
        )
        UserTextMessageView(
            orientedMessage = OrientedMessage(
                message = Message.User().apply {
                    text = "Lorem ipsum doloc za dom braco za slobodu, boremo se mi, csuite srpski dobrovolci, bando csetnice...\nstichize vas nasa ruka i u srbii"
                    time = "18:58"
                },
            ),
            uiConfig = ChatUIConfigDefault,
        )
    }

}

