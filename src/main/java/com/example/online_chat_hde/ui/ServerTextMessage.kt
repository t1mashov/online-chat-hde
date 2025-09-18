package com.example.online_chat_hde.ui

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.online_chat_hde.core.OrientedMessage
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.Message




@Composable
internal fun ServerTextMessageView(
    orientedMessage: OrientedMessage,
    uiConfig: ChatUIConfig,
    onButtonClick: (ChatButton) -> Unit = {},
    showButtons: Boolean,
) {

    val message = orientedMessage.message as Message.Server
    val isVirtual = orientedMessage.message.isVirtual

    val density = LocalDensity.current

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {
        Row {

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val maxWidth = maxWidth

                if (orientedMessage.placeHorizontal.value) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .background(
                                color = if (isVirtual) uiConfig.colors.virtualMessageBackground
                                        else uiConfig.colors.serverMessageBackground,
                                shape = uiConfig.dimensions.serverTextMessagesCorners
                            )
                            .padding(uiConfig.dimensions.messagePadding)
                    ) {

                        ConstraintLayout {
                            val (name, text, time, buttons) = createRefs()

                            Text(
                                modifier = Modifier.constrainAs(name) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                },
                                text = message.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = uiConfig.dimensions.agentNameFontSize,
                                color = if (isVirtual) uiConfig.colors.virtualMessageAgent
                                        else uiConfig.colors.serverMessageAgent
                            )

                            Text(
                                modifier = Modifier.constrainAs(text) {
                                    top.linkTo(name.bottom, margin = uiConfig.dimensions.innerIndent)
                                    start.linkTo(parent.start)
                                },
                                text = message.text,
                                color = if (isVirtual) uiConfig.colors.virtualMessageText
                                        else uiConfig.colors.serverMessageText,
                                fontSize = uiConfig.dimensions.messageFontSize
                            )

                            Text(
                                modifier = Modifier.constrainAs(time) {
                                    bottom.linkTo(text.bottom)
                                    end.linkTo(parent.end)
                                    start.linkTo(text.end, margin = uiConfig.dimensions.innerIndent)
                                    horizontalBias = 1f
                                },
                                text = message.time,
                                color = if (isVirtual) uiConfig.colors.virtualTimeText
                                        else uiConfig.colors.serverTimeText,
                                fontSize = uiConfig.dimensions.timeFontSize
                            )

                            if ( !message.chatButtons.isNullOrEmpty() && showButtons) {

                                val maxButtonWidth = calculateTextMessageWidth(
                                    message.chatButtons!!.maxBy { it.text.length }.text,
                                    density, uiConfig
                                ) + uiConfig.dimensions.buttonPadding*2

                                val horizontalWidgetSize = calculateTextMessageWidth(message.text, density, uiConfig)

                                var buttonsWidth = maxOf(horizontalWidgetSize, maxButtonWidth)
                                if (buttonsWidth + uiConfig.dimensions.messagePadding*2 > maxWidth) {
                                    buttonsWidth = maxWidth - uiConfig.dimensions.messagePadding*2
                                }

                                ButtonsColumn(
                                    message.chatButtons!!, uiConfig, buttonsWidth, onButtonClick,
                                    modifier = Modifier.constrainAs(buttons) {
                                        start.linkTo(parent.start)
                                        top.linkTo(text.bottom, margin = uiConfig.dimensions.innerIndent)
                                    },
                                )

                            }

                        }
                    }
                }
                else {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .background(
                                color = if (isVirtual) uiConfig.colors.virtualMessageBackground
                                        else uiConfig.colors.serverMessageBackground,
                                shape = uiConfig.dimensions.serverTextMessagesCorners
                            )
                            .padding(uiConfig.dimensions.messagePadding)
                    ) {
                        Column {
                            Text(
                                text = message.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = uiConfig.dimensions.agentNameFontSize,
                                color = if (isVirtual) uiConfig.colors.virtualMessageAgent
                                        else uiConfig.colors.serverMessageAgent
                            )
                            Spacer(Modifier.height(uiConfig.dimensions.innerIndent))
                            Text(
                                text = message.text,
                                color = if (isVirtual) uiConfig.colors.virtualMessageText
                                        else uiConfig.colors.serverMessageText,
                                fontSize = uiConfig.dimensions.messageFontSize
                            )
                            Spacer(Modifier.height(uiConfig.dimensions.innerIndent))
                            Row {
                                Box(modifier = Modifier.weight(1f))
                                Text(
                                    text = message.time,
                                    color = if (isVirtual) uiConfig.colors.virtualTimeText
                                            else uiConfig.colors.serverTimeText,
                                    fontSize = uiConfig.dimensions.timeFontSize
                                )
                            }
                            Spacer(Modifier.height(uiConfig.dimensions.innerIndent/2))

                            if ( !message.chatButtons.isNullOrEmpty() && showButtons) {
                                Column {
                                    for (button in message.chatButtons!!) {
                                        Spacer(Modifier.height(uiConfig.dimensions.innerIndent/2))
                                        Row {
                                            ChatMessageButton(
                                                button,
                                                uiConfig,
                                                onButtonClick,
                                                modifier = Modifier.weight(1f)
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

        Column {

            ServerTextMessageView(
                orientedMessage = OrientedMessage(
                    Message.Server(
                        name = "support@hde.com",
                    ).apply {
                        text = "Can I help you?"
                        time = "09:45"
                        chatButtons = listOf(
                            ChatButton(text = "Call operator"),
                            ChatButton(text = "Stop bot")
                        )
                        isVirtual = true
                    },
                    placeHorizontal = remember { mutableStateOf(true) }
                ),
                uiConfig = ChatUIConfigDefault,
                showButtons = true,
            )
            ServerTextMessageView(
                orientedMessage = OrientedMessage(
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
                ),
                uiConfig = ChatUIConfigDefault,
                showButtons = true,
            )
            ServerTextMessageView(
                orientedMessage = OrientedMessage(
                    message = Message.Server(
                        name = "alex@support.hde.com",
                    ).apply {
                        text = "150"
                        time = "12:40"
                        visitor = true
                        chatButtons = listOf()
                    },
                    placeHorizontal = remember { mutableStateOf(true) }
                ),
                uiConfig = ChatUIConfigDefault,
                showButtons = true,
            )
            ServerTextMessageView(
                orientedMessage = OrientedMessage(
                    message = Message.Server(
                        name = "alex@support.hde.com",
                    ).apply {
                        text = "Dolor sit amet, sdf xcvlon teza sdf voger rythedbdbd"
                        time = "12:40"
                        visitor = true
                    },
                ),
                uiConfig = ChatUIConfigDefault,
                showButtons = true,
            )
        }

}


