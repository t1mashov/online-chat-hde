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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.online_chat_hde.core.OrientedMessage
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message


@Composable
fun UserFileMessageView(
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
                                        indication = rememberRipple(
                                            color = uiConfig.colors.userRipple,
                                        )
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
                                            uiConfig.dimensions.loadingSize
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
                                    indication = rememberRipple(
                                        color = uiConfig.colors.userRipple,
                                    )
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
                                        uiConfig.dimensions.loadingSize
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
fun PreviewUserFileMessage() {

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        UserFileMessageView(
            orientedMessage = OrientedMessage(Message.User(), placeHorizontal = remember { mutableStateOf(true) }),
            file = FileData.Text("", false).apply {
                name = "hello.txt"
            },
            time = "12:56",
            uiConfig = ChatUIConfigDefault,
            onFileClick = {},
        )
        UserFileMessageView(
            orientedMessage = OrientedMessage(Message.User()),
            file = FileData.Text("", false).apply {
                name = "Ipsum_dolor_sit_conse_ctetur_adip_iscing_minimum_ven_jiam.txt"
            },
            time = "08:31",
            uiConfig = ChatUIConfigDefault,
            onFileClick = {},
        )
    }

}






@Composable
fun UserFileMessageOld(
    orientedMessage: OrientedMessage,
    file: FileData.Text,
    time: String,
    uiConfig: ChatUIConfig,
    onFileClick: (FileData.Text) -> Unit,
    isLoading: Boolean = false
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

            SubcomposeLayout { constraints ->

                val horElement = @Composable {
                    Row {
                        Box(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(uiConfig.dimensions.userTextMessagesCorners)
                                .background(color = uiConfig.colors.userMessageBackground,)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = rememberRipple(
                                        color = Color(0xFFFFFFFF),
                                    )
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
                                        .padding(end = 4.dp)
                                        .size(20.dp)
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
                                        uiConfig.dimensions.loadingSize
                                    )
                                }
                            }
                        }
                    }
                }

                val boxHorizontal = subcompose("h") {
                    horElement()
                }.first().measure(constraints.copy(minWidth = 0, minHeight = 0))

                val boxHorizontalToDraw = subcompose("h2") {
                    Row {
                        Box(modifier = Modifier.weight(1f))
                        horElement()
                    }
                }.first().measure(constraints.copy(minWidth = 0, minHeight = 0))

                if (constraints.maxWidth != boxHorizontal.width) {
                    layout(constraints.maxWidth, boxHorizontalToDraw.height) {
                        boxHorizontalToDraw.place(0, 0)
                    }
                }
                else {
                    val boxVertical = subcompose("v") {
                        Row {
                            Box(modifier = Modifier.weight(1f))
                            Box (
                                modifier = Modifier
                                    .clip(uiConfig.dimensions.userTextMessagesCorners)
                                    .background(color = uiConfig.colors.userMessageBackground)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = rememberRipple(
                                            color = Color(0xFFFFFFFF),
                                        )
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
                                                .padding(end = 4.dp)
                                                .size(20.dp)
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
                                            uiConfig.dimensions.loadingSize
                                        )
                                    }
                                }
                            }
                        }
                    }.first().measure(constraints.copy(minWidth = 0, minHeight = 0))

                    layout(constraints.maxWidth, boxVertical.height) {
                        boxVertical.place(0, 0)
                    }
                }

            }
        }
    }

}