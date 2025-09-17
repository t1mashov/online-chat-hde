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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.online_chat_hde.R
import com.example.online_chat_hde.core.OrientedMessage
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message


@Composable
fun ServerFileMessageView(
    orientedMessage: OrientedMessage,
    file: FileData.Text,
    name: String,
    time: String,
    uiConfig: ChatUIConfig,
    onFileClick: (FileData.Text) -> Unit,
) {

    val interactionSource = remember { MutableInteractionSource() }

    val isVirtual = orientedMessage.message.isVirtual

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {
        Row {

            Box(modifier = Modifier.weight(1f)) {

                if (orientedMessage.placeHorizontal.value) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .clip(uiConfig.dimensions.serverTextMessagesCorners)
                            .background(
                                color = if (isVirtual) uiConfig.colors.virtualMessageBackground
                                        else uiConfig.colors.serverMessageBackground
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(
                                    color = uiConfig.colors.serverRipple
                                )
                            ) {
                                onFileClick(file)
                            }
                            .padding(uiConfig.dimensions.messagePadding)
                    ) {

                        ConstraintLayout {
                            val (nameRef, textRef, timeRef, imageRef) = createRefs()

                            Image(
                                imageVector = ImageVector.vectorResource(R.drawable.piperclip),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(
                                    if (isVirtual) uiConfig.colors.virtualMessageText
                                    else uiConfig.colors.serverMessageText
                                ),
                                modifier = Modifier
                                    .size(uiConfig.dimensions.pyperclipSize)
                                    .constrainAs(imageRef) {
                                        start.linkTo(parent.start)
                                        top.linkTo(
                                            nameRef.bottom,
                                            margin = uiConfig.dimensions.innerIndent
                                        )
                                    }
                            )

                            Text(
                                modifier = Modifier.constrainAs(nameRef) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                },
                                text = name,
                                fontWeight = FontWeight.Bold,
                                fontSize = uiConfig.dimensions.agentNameFontSize,
                                color = if (isVirtual) uiConfig.colors.virtualMessageAgent
                                        else uiConfig.colors.serverMessageAgent
                            )

                            Text(
                                modifier = Modifier.constrainAs(textRef) {
                                    bottom.linkTo(imageRef.bottom)
                                    start.linkTo(imageRef.end, margin = uiConfig.dimensions.innerIndent/2)
                                },
                                text = file.name,
                                color = if (isVirtual) uiConfig.colors.virtualMessageText
                                        else uiConfig.colors.serverMessageText,
                                fontSize = uiConfig.dimensions.messageFontSize,
                                textDecoration = TextDecoration.Underline
                            )

                            Text(
                                modifier = Modifier.constrainAs(timeRef) {
                                    bottom.linkTo(parent.bottom)
                                    end.linkTo(parent.end)
                                    start.linkTo(textRef.end, margin = uiConfig.dimensions.innerIndent)
                                    horizontalBias = 1f
                                },
                                text = time,
                                color = if (isVirtual) uiConfig.colors.virtualTimeText
                                        else uiConfig.colors.serverTimeText,
                                fontSize = uiConfig.dimensions.timeFontSize
                            )

                        }
                    }
                }
                else {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .clip(uiConfig.dimensions.serverTextMessagesCorners)
                            .background(
                                color = if (isVirtual) uiConfig.colors.virtualMessageBackground
                                        else uiConfig.colors.serverMessageBackground
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(
                                    color = if (isVirtual) uiConfig.colors.virtualRipple
                                            else uiConfig.colors.serverRipple
                                )
                            ) {
                                onFileClick(file)
                            }
                            .padding(uiConfig.dimensions.messagePadding)
                    ) {

                        Column {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                fontSize = uiConfig.dimensions.agentNameFontSize,
                                color = if (isVirtual) uiConfig.colors.virtualMessageAgent
                                        else uiConfig.colors.serverMessageAgent
                            )
                            Spacer(modifier = Modifier.height(uiConfig.dimensions.innerIndent))
                            Row {
                                Image(
                                    imageVector = ImageVector.vectorResource(R.drawable.piperclip),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(
                                        if (isVirtual) uiConfig.colors.virtualMessageText
                                        else uiConfig.colors.serverMessageText
                                    ),
                                    modifier = Modifier
                                        .size(uiConfig.dimensions.pyperclipSize)
                                )
                                Spacer(modifier = Modifier.width(uiConfig.dimensions.innerIndent/2))
                                Text(
                                    text = file.name,
                                    color = if (isVirtual) uiConfig.colors.virtualMessageText
                                            else uiConfig.colors.serverMessageText,
                                    fontSize = uiConfig.dimensions.messageFontSize,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                            Spacer(modifier = Modifier.height(uiConfig.dimensions.innerIndent))
                            Row {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = time,
                                    color = if (isVirtual) uiConfig.colors.virtualTimeText
                                            else uiConfig.colors.serverTimeText,
                                    fontSize = uiConfig.dimensions.timeFontSize
                                )
                            }
                        }

                    }
                }

            }

            Box(modifier = Modifier.width(uiConfig.dimensions.messageMinEndIndent))
        }
    }
}






@Preview(showBackground = true)
@Composable
fun PreviewServerFileMessage() {

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        ServerFileMessageView(
            orientedMessage = OrientedMessage(Message.User(), placeHorizontal = remember { mutableStateOf(true) }) ,
            file = FileData.Text("", false).apply {
                name = "hello.txt"
            },
            name = "alex@hde.com",
            time = "12:56",
            uiConfig = ChatUIConfigDefault,
            onFileClick = {},
        )
        ServerFileMessageView(
            orientedMessage = OrientedMessage(Message.User()),
            file = FileData.Text("",  false).apply {
                name = "Ipsum_dolor_sit_conse_ctetur_adipiscing_minimumven_jiam.txt"
            },
            name = "olgerd1450@hde.com",
            time = "08:31",
            uiConfig = ChatUIConfigDefault,
            onFileClick = {},
        )
    }

}