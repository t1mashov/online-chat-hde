package com.example.online_chat_hde.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.online_chat_hde.core.OrientedMessage
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message


@Composable
internal fun UserFileMessageView(
    orientedMessage: OrientedMessage,
    file: FileData.Text,
    time: String,
    uiConfig: ChatUIConfig,
    onFileClick: (FileData.Text) -> Unit,
    isLoading: Boolean = false,
) {

    val interactionSource = remember { MutableInteractionSource() }


    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box (
                modifier = Modifier.width(uiConfig.dimensions.messageMinEndIndent)
            )

            Box(modifier = Modifier.weight(1f)) {

                if (orientedMessage.placeHorizontal.value) {
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
                                    .padding(uiConfig.dimensions.messagePadding)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Image(
                                        imageVector = ImageVector.vectorResource(com.example.online_chat_hde.R.drawable.piperclip),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(uiConfig.colors.userMessageText),
                                        modifier = Modifier.size(uiConfig.dimensions.pyperclipSize)
                                            .padding(end = uiConfig.dimensions.innerIndent/2)
                                            .size(uiConfig.dimensions.pyperclipSize)
                                    )
                                    Text(
                                        text = file.name,
                                        color = uiConfig.colors.userMessageText,
                                        fontSize = uiConfig.dimensions.messageFontSize,
                                        textDecoration = TextDecoration.Underline
                                    )

                                    Spacer(modifier = Modifier.width(uiConfig.dimensions.innerIndent))
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
                                .padding(uiConfig.dimensions.messagePadding)
                        ) {
                            Column (
                                horizontalAlignment = Alignment.End
                            ) {
                                Row {
                                    Image(
                                        imageVector = ImageVector.vectorResource(com.example.online_chat_hde.R.drawable.piperclip),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(uiConfig.colors.userMessageText),
                                        modifier = Modifier.size(uiConfig.dimensions.pyperclipSize)
                                            .padding(end = uiConfig.dimensions.innerIndent/2)
                                            .size(uiConfig.dimensions.pyperclipSize)
                                    )
                                    Text(
                                        text = file.name,
                                        color = uiConfig.colors.userMessageText,
                                        fontSize = uiConfig.dimensions.messageFontSize,
                                        textDecoration = TextDecoration.Underline,
                                    )
                                }
                                Spacer(modifier = Modifier.height(uiConfig.dimensions.innerIndent))
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

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        UserFileMessageView(
            orientedMessage = OrientedMessage(Message.User(), placeHorizontal = remember { mutableStateOf(true) }),
            file = FileData.Text().apply {
                name = "hello.txt"
            },
            time = "12:56",
            uiConfig = ChatUIConfigDefault,
            onFileClick = {},
        )
        UserFileMessageView(
            orientedMessage = OrientedMessage(Message.User()),
            file = FileData.Text().apply {
                name = "Ipsum_dolor_sit_conse_ctetur_adip_iscing_minimum_ven_jiam.txt"
            },
            time = "08:31",
            uiConfig = ChatUIConfigDefault,
            onFileClick = {},
        )
    }

}

