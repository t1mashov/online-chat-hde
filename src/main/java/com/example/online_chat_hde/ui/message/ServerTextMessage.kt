package com.example.online_chat_hde.ui.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.online_chat_hde.viewmodels.UiMessage
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.ui.ChatMessageButton
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault


@Composable
fun ServerMessageScope.ServerTextMessage() {

    val message = uiMessage.message as Message.Server
    val isVirtual = uiMessage.message.isVirtual

    val nameTextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = uiConfig.dimensions.agentNameFontSize,
        color = if (isVirtual) uiConfig.colors.virtualMessageAgent
                else uiConfig.colors.serverMessageAgent
    )

    val contentStyle = TextStyle(
        color = if (isVirtual) uiConfig.colors.virtualMessageText
                else uiConfig.colors.serverMessageText,
        fontSize = uiConfig.dimensions.messageFontSize
    )

    val timeStyle = TextStyle(
        color = if (isVirtual) uiConfig.colors.virtualTimeText
                else uiConfig.colors.serverTimeText,
        fontSize = uiConfig.dimensions.timeFontSize
    )

    val background = if (isVirtual) uiConfig.colors.virtualMessageBackground
                    else uiConfig.colors.serverMessageBackground

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {
        Row {

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val maxWidth = maxWidth

                val textWidth = getTextWidth(message.text, uiConfig.dimensions.messageFontSize)
                val maxBoxWidth = maxWidth
                val innerSpace = uiConfig.dimensions.innerIndent
                val padding = uiConfig.dimensions.messagePadding

                val placeHorizontal = textWidth + innerSpace + timeWidth + padding*2 <= maxBoxWidth

                if (placeHorizontal) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .background(
                                color = background,
                                shape = uiConfig.dimensions.serverTextMessagesCorners
                            )
                            .padding(padding)
                    ) {

                        ConstraintLayout {
                            val (nameRef, textRef, timeRef, buttonsRef) = createRefs()

                            Text(
                                modifier = Modifier.constrainAs(nameRef) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                },
                                text = message.name,
                                style = nameTextStyle
                            )

                            Text(
                                modifier = Modifier.constrainAs(textRef) {
                                    top.linkTo(nameRef.bottom, margin = innerSpace)
                                    start.linkTo(parent.start)
                                },
                                text = message.text,
                                style = contentStyle
                            )

                            Text(
                                modifier = Modifier.constrainAs(timeRef) {
                                    bottom.linkTo(textRef.bottom)
                                    end.linkTo(parent.end)
                                    start.linkTo(textRef.end, margin = innerSpace)
                                    horizontalBias = 1f
                                },
                                text = message.time,
                                style = timeStyle
                            )

                            if ( !message.chatButtons.isNullOrEmpty() && showButtons) {
                                val buttons = message.chatButtons!!

                                val maxButtonWidth = getTextWidth(
                                    buttons.maxBy { it.text.length }.text,
                                    uiConfig.dimensions.messageFontSize
                                ) + uiConfig.dimensions.buttonPadding*2

                                val horizontalWidgetSize = textWidth + timeWidth + innerSpace
                                var buttonsWidth = maxOf(horizontalWidgetSize, maxButtonWidth)
                                if (buttonsWidth + padding*2 > maxWidth) {
                                    buttonsWidth = maxWidth - padding*2
                                }

                                Column(
                                    modifier = Modifier.constrainAs(buttonsRef) {
                                        start.linkTo(parent.start)
                                        top.linkTo(textRef.bottom, margin = innerSpace)
                                    }
                                ) {
                                    for (button in buttons) {
                                        Spacer(Modifier.height(innerSpace/2))
                                        Box(modifier = Modifier.width(buttonsWidth)) {
                                            ChatMessageButton(
                                                button,
                                                { onChatButtonClick(button) }
                                            )
                                        }
                                    }
                                }

                            }

                        }
                    }
                }
                else {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .background(
                                color = background,
                                shape = uiConfig.dimensions.serverTextMessagesCorners
                            )
                            .padding(padding)
                    ) {
                        Column {
                            Text(text = message.name, style = nameTextStyle)
                            Spacer(Modifier.height(innerSpace))
                            Text(text = message.text, style = contentStyle)
                            Spacer(Modifier.height(innerSpace))
                            Row {
                                Box(modifier = Modifier.weight(1f))
                                Text(text = message.time, style = timeStyle)
                            }

                            Spacer(Modifier.height(innerSpace/2))

                            if ( !message.chatButtons.isNullOrEmpty() && showButtons) {
                                Column {
                                    for (button in message.chatButtons!!) {
                                        Spacer(Modifier.height(innerSpace/2))
                                        Row {
                                            ChatMessageButton(
                                                button,
                                                onChatButtonClick
                                            )
                                        }
                                    }
                                }
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
internal fun TestScreen2() {

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


    Column {

        val sc1 = scopes(
            UiMessage(
                Message.Server(
                    name = "support@hde.com",
                ).apply {
                    text = "Can I help you?"
                    time = "09:45"
                    chatButtons = listOf(
                        ChatButton(text = "Call operator aoaoaoaoaoao w234 edfg"),
                        ChatButton(text = "Stop bot")
                    )
                    isVirtual = true
                }
            )
        )
        with(sc1) {
            ServerTextMessage()
        }

        val sc2 = scopes(
            UiMessage(
                message = Message.Server(
                    name = "alex@support.hde.com",
                ).apply {
                    text = "Dolor sit amet, conse  veni am, quis nos tur dur travos malesoc uthurcz pelon tezavoger rythed qer"
                    time = "12:40"
                    visitor = true
                    chatButtons = listOf(
                        ChatButton(text = "Call operator"),
                        ChatButton(text = "Call operator"),
                    )
                },
            )
        )
        with(sc2) {
            ServerTextMessage()
        }

        val sc3 = scopes(
            UiMessage(
                message = Message.Server(
                    name = "alex@support.hde.com",
                ).apply {
                    text = "150"
                    time = "12:40"
                    visitor = true
                    chatButtons = listOf()
                },
            )
        )
        with(sc3) {
            ServerTextMessage()
        }

        val sc4 = scopes(
            UiMessage(
                message = Message.Server(
                    name = "alex@support.hde.com888",
                ).apply {
                    text = "Dolor sit amet, sdf xcvlon teza sdf voger rythedbdbd"
                    time = "12:40"
                    visitor = true
                },
            )
        )
        with(sc4) {
            ServerTextMessage()
        }
    }

}


