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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.online_chat_hde.R
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.viewmodels.UiMessage
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault


@Composable
fun ServerMessageScope.ServerFileMessage() {

    val isVirtual = uiMessage.message.isVirtual
    val message = uiMessage.message as Message.Server
    val file = message.files!![0] as FileData.Text
    val name = message.name
    val time = message.time

    val background =
        if (isVirtual) uiConfig.colors.virtualMessageBackground
        else uiConfig.colors.serverMessageBackground

    val textColor =
        if (isVirtual) uiConfig.colors.virtualMessageText
        else uiConfig.colors.serverMessageText

    val textStyle = TextStyle(
        color = textColor,
        fontSize = uiConfig.dimensions.messageFontSize,
        textDecoration = TextDecoration.Underline
    )

    val nameStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = uiConfig.dimensions.agentNameFontSize,
        color = if (isVirtual) uiConfig.colors.virtualMessageAgent
                else uiConfig.colors.serverMessageAgent
    )

    val timeStyle = TextStyle(
        color = if (isVirtual) uiConfig.colors.virtualTimeText
                else uiConfig.colors.serverTimeText,
        fontSize = uiConfig.dimensions.timeFontSize
    )

    val ripple = ripple(
        color =
            if (isVirtual) uiConfig.colors.virtualRipple
            else uiConfig.colors.serverRipple

    )

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {
        Row {

            BoxWithConstraints(modifier = Modifier.weight(1f)) {

                val textWidth = getTextWidth(file.name, uiConfig.dimensions.messageFontSize)
                val maxBoxWidth = maxWidth
                val innerSpace = uiConfig.dimensions.innerIndent
                val padding = uiConfig.dimensions.messagePadding

                val placeHorizontal = textWidth + innerSpace + timeWidth + padding*2 <= maxBoxWidth

                if (placeHorizontal) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .clip(uiConfig.dimensions.serverTextMessagesCorners)
                            .background(
                                color = background
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple
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
                                    textColor
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
                                style = nameStyle
                            )

                            Text(
                                modifier = Modifier.constrainAs(textRef) {
                                    bottom.linkTo(imageRef.bottom)
                                    start.linkTo(imageRef.end, margin = uiConfig.dimensions.innerIndent/2)
                                },
                                text = file.name,
                                style = textStyle
                            )

                            Text(
                                modifier = Modifier.constrainAs(timeRef) {
                                    bottom.linkTo(parent.bottom)
                                    end.linkTo(parent.end)
                                    start.linkTo(textRef.end, margin = uiConfig.dimensions.innerIndent)
                                    horizontalBias = 1f
                                },
                                text = time,
                                style = timeStyle
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
                                color = background
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple
                            ) {
                                onFileClick(file)
                            }
                            .padding(uiConfig.dimensions.messagePadding)
                    ) {

                        Column {
                            Text(text = name, style = nameStyle)
                            Spacer(modifier = Modifier.height(uiConfig.dimensions.innerIndent))
                            Row {
                                Image(
                                    imageVector = ImageVector.vectorResource(R.drawable.piperclip),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(textColor),
                                    modifier = Modifier
                                        .size(uiConfig.dimensions.pyperclipSize)
                                )
                                Spacer(modifier = Modifier.width(uiConfig.dimensions.innerIndent/2))
                                Text(text = file.name, style = textStyle)
                            }
                            Spacer(modifier = Modifier.height(uiConfig.dimensions.innerIndent))
                            Row {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = time, style = timeStyle)
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
private fun PreviewServerFileMessage() {

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


    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        with (
            scopes(
                UiMessage(Message.Server("alex@hde.com").apply {
                    files = listOf(
                        FileData.Text().apply {
                            name = "hello.txt"
                        }
                    )
                    time = "09:10"
                })
            )
        ) {
            ServerFileMessage()
        }
        with(
            scopes(
                UiMessage(Message.Server("olgerd1450@hde.com").apply {
                    files = listOf(
                        FileData.Text().apply {
                            name = "Ipsum_dolor_sit_conse_ctetur_adipiscing_minimumven_jiam.txt"
                        }
                    )
                    time = "12:35"
                })
            )
        ) {
            ServerFileMessage()
        }
    }

}