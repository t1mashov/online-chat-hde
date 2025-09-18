package com.example.online_chat_hde.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.online_chat_hde.core.OrientedMessage
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message

@Composable
internal fun ServerMessageView(
    orientedMessage: OrientedMessage,
    baseURL: String,
    onFileClick: (FileData.Text) -> Unit,
    onImageClick: (FileData.Image) -> Unit,
    onChatButtonClick: (ChatButton) -> Unit,
    uiConfig: ChatUIConfig,
    showButtons: Boolean,
    modifier: Modifier = Modifier,
) {

    val message = orientedMessage.message as Message.Server

    Column(modifier = modifier) {
        if (message.text.isNotEmpty() || !message.chatButtons.isNullOrEmpty()) {
            ServerTextMessageView(
                orientedMessage,
                uiConfig,
                onChatButtonClick,
                showButtons
            )
        }
        if (message.files != null) {
            for (file in message.files!!) {
                when (file) {
                    is FileData.Text -> {
                        ServerFileMessageView(
                            orientedMessage = orientedMessage,
                            file = file,
                            name = message.name,
                            time = message.time,
                            uiConfig = uiConfig,
                            onFileClick = onFileClick
                        )
                    }
                    is FileData.Image -> {
                        ServerImageMessageView(
                            image = file,
                            time = message.time,
                            uiConfig = uiConfig,
                            onImageClick = onImageClick,
                            baseURL = baseURL,
                        )
                    }
                }
            }
        }
    }
}