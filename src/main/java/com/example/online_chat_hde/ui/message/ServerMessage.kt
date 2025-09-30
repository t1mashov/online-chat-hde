package com.example.online_chat_hde.ui.message

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.viewmodels.UiMessage
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message

@Stable
interface ServerMessageScope: ChatUIScope {
    val getTextWidth: (text: String, fontSize: TextUnit) -> Dp
    val uiMessage: UiMessage
    val baseURL: String
    val timeWidth: Dp
    val buttonMaxTextWidth: Dp
    val onFileClick: (FileData.Text) -> Unit
    val onImageClick: (FileData.Image) -> Unit
    val onChatButtonClick: (ChatButton) -> Unit
    val showButtons: Boolean
}

@Composable
fun ServerMessageScope.ServerMessage(
    serverTextMessage: @Composable ServerMessageScope.() -> Unit = { ServerTextMessage() },
    serverFileMessage: @Composable ServerMessageScope.() -> Unit = { ServerFileMessage() },
    serverImageMessage: @Composable ServerMessageScope.(FileData.Image) -> Unit = { ServerImageMessageView(it) },
) {

    val message = uiMessage.message as Message.Server


    if (message.text.isNotEmpty() || !message.chatButtons.isNullOrEmpty()) {
        serverTextMessage()
    }
    if (!message.files.isNullOrEmpty()) {
        when (val file = message.files!![0]) {
            is FileData.Text -> {
                serverFileMessage()
            }
            is FileData.Image -> {
                file.thumb = if (file.thumb.contains("://")) file.thumb else baseURL + file.thumb
                serverImageMessage(file)
            }
        }

    }

}