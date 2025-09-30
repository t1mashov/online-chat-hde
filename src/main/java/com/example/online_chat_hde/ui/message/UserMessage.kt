package com.example.online_chat_hde.ui.message

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.viewmodels.UiMessage
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault

@Stable
interface UserMessageScope: ChatUIScope {
    val getTextWidth: (text: String, fontSize: TextUnit) -> Dp
    val uiMessage: UiMessage
    val baseURL: String
    val timeWidth: Dp
    val onFileClick: (FileData.Text) -> Unit
    val onImageClick: (FileData.Image) -> Unit
}

@Composable
fun UserMessageScope.UserMessage(
    userTextMessage: @Composable UserMessageScope.() -> Unit = { UserTextMessage() },
    userFileMessage: @Composable UserMessageScope.(FileData.Text) -> Unit = { UserFileMessage(it) },
    userImageMessage: @Composable UserMessageScope.(FileData.Image) -> Unit = { UserImageMessage(it) },
) {

    val message = uiMessage.message as Message.User

    if (message.text.isNotEmpty()) {
        userTextMessage()
    }
    if (!message.files.isNullOrEmpty()) {
        when (val file = message.files!![0]) {
            is FileData.Text -> {
                userFileMessage(file)
            }
            is FileData.Image -> {
                if (!file.thumb.contains("://")) file.thumb = baseURL + file.thumb
                userImageMessage(file)
            }
        }

    }

}


@Preview
@Composable
internal fun UserMessageWidgetPreview() {

    val scope = object : UserMessageScope {
        override val getTextWidth: (String, TextUnit) -> Dp = {_, _ -> 0.dp}
        override val uiMessage = UiMessage(
            message = Message.User().apply {
                text = "This is 2 files"
                time = "16:45"
                files = listOf(
                    FileData.Image(
                        thumb = "/ru/file/image_thumb/278c438bd653f82adfc93249ed059f5481b714db/size/150"
                    ).apply {
                        name = "mountain-landscape.jpg"
                        link = "/ru/file/inline_image/278c438bd653f82adfc93249ed059f5481b714db"
                    },
                    FileData.Text().apply {
                        name = "very-important-text.txt"
                        link = "/ru/file/inline_image/278c438bd653f82adfc93249ed059f5481b714db"
                    }
                )
            }
        )
        override val baseURL: String = ""
        override val timeWidth: Dp = 12.dp
        override val onFileClick: (FileData.Text) -> Unit = {}
        override val onImageClick: (FileData.Image) -> Unit = {}
        override val uiConfig: ChatUIConfig = ChatUIConfigDefault
    }

    with (scope) {
        Column {
            UserMessage()
        }
    }
}