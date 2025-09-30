package com.example.online_chat_hde.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.online_chat_hde.ChatUIScope


interface ChatBottomPanelFrame {
    val divider : @Composable () -> Unit
    val filePickButton : @Composable () -> Unit
    val textInput : @Composable () -> Unit
    val sendButton : @Composable () -> Unit
    val noInternetBanner : @Composable () -> Unit
}

interface ChatBottomPanelScope: ChatUIScope {
    val isConnected: Boolean
    val messageText: String
    val filePickerExpanded: Boolean
    val onMessageTextChange: (String) -> Unit
    val onFileExpandedChange: (Boolean) -> Unit
    val sendMessage: (text: String) -> Unit
    val uploadFile: (uri: Uri, size: Long) -> Unit
}

@Composable
fun ChatBottomPanelScope.ChatFooter(
    divider: @Composable ChatUIScope.() -> Unit = { ChatBottomDivider() },
    filePickButton: @Composable ChatBottomPanelScope.() -> Unit = { FilePickButton() },
    textInput: @Composable ChatBottomPanelScope.() -> Unit = { TextInput() },
    sendMessageButton: @Composable ChatBottomPanelScope.() -> Unit = { SendMessageButton() },
    noInternetBanner: @Composable ChatUIScope.() -> Unit = { NoInternetBanner() },

    frame: @Composable ChatBottomPanelScope.(ChatBottomPanelFrame) -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(uiConfig.colors.bottomPanelBackground)
        ) {
            it.divider()
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                if (isConnected) {
                    it.filePickButton()
                    Box(modifier = Modifier.weight(1f)) {
                        it.textInput()
                    }
                    if (messageText.trim().isNotEmpty()) {
                        it.sendButton()
                    }
                }
                else {
                    it.noInternetBanner()
                }
            }
        }
    }
) {

    val layout = remember(isConnected, messageText, filePickerExpanded) {
        object : ChatBottomPanelFrame {
            override val divider: @Composable () -> Unit = { with(this@ChatFooter) { divider() } }
            override val filePickButton: @Composable () -> Unit = { with(this@ChatFooter) { filePickButton() } }
            override val textInput: @Composable () -> Unit = { with(this@ChatFooter) { textInput() } }
            override val sendButton: @Composable () -> Unit = { with(this@ChatFooter) { sendMessageButton() } }
            override val noInternetBanner: @Composable () -> Unit = { with(this@ChatFooter) { noInternetBanner() } }
        }
    }

    frame(layout)

    FilePickerBottom(filePickerExpanded, onFileExpandedChange, uiConfig) { uri, size ->
        uploadFile(uri, size)
    }
}


@Composable
fun ChatUIScope.ChatBottomDivider(
    color: Color = uiConfig.colors.bottomDivider,
    height: Dp = 1.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(color)
    )
}


@Composable
fun ChatBottomPanelScope.FilePickButton(
    color: Color = uiConfig.colors.pyperclip,
    ripple: Color = uiConfig.colors.userMessageBackground,
    padding: Dp = uiConfig.dimensions.bottomPanelPadding,
    logo: Int = uiConfig.media.linkFileLogo,
    size: Dp = uiConfig.dimensions.logoSize
) {
    Box(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    color = ripple
                )
            ) {
                onFileExpandedChange(true)
            }
            .padding(padding)
    ) {
        Image(
            imageVector = ImageVector.vectorResource(logo),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier
                .size(size)
        )
    }
}


@Composable
fun ChatBottomPanelScope.TextInput(

) {
    BasicTextField(
        value = messageText,
        onValueChange = onMessageTextChange,

        textStyle = TextStyle(
            fontSize = uiConfig.dimensions.messageFontSize,
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = uiConfig.dimensions.logoSize),
                contentAlignment = Alignment.CenterStart
            ) {
                if (messageText.isEmpty()) {
                    Text(
                        text = uiConfig.texts.messagePlaceholder,
                        color = uiConfig.colors.messagePlaceholder,
                        fontSize = uiConfig.dimensions.messageFontSize
                    )
                }
                innerTextField()
            }
        },
        modifier = Modifier
            .background(uiConfig.colors.bottomPanelBackground)
            .padding(
                top = uiConfig.dimensions.bottomPanelPadding,
                bottom = uiConfig.dimensions.bottomPanelPadding
            )
    )
}


@Composable
fun ChatBottomPanelScope.SendMessageButton(
    logo: Int = uiConfig.media.sendMessageLogo,
    color: Color = uiConfig.colors.sendMessage,
    size: Dp = uiConfig.dimensions.logoSize,
    padding: Dp = uiConfig.dimensions.bottomPanelPadding,
    ripple: Color = uiConfig.colors.userMessageBackground
) {
    Box(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    color = ripple
                )
            ) {
                sendMessage(messageText.trim())
                onMessageTextChange("")
            }
            .padding(padding)
    ) {
        Image(
            imageVector = ImageVector.vectorResource(logo),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier
                .size(size)
        )
    }
}


@Composable
fun ChatUIScope.NoInternetBanner(
    background: Color = uiConfig.colors.errorPrimary,
    color: Color = uiConfig.colors.errorSecondary,
    text: String = uiConfig.texts.connectionError,
    fontSize: TextUnit = uiConfig.dimensions.messageFontSize,
    padding: Dp = uiConfig.dimensions.bottomPanelPadding,
    minHeight: Dp = uiConfig.dimensions.logoSize,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(padding)
            .heightIn(min = minHeight)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}


