package com.example.online_chat_hde.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.online_chat_hde.core.OrientedMessage
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message

@Composable
internal fun UserMessageView(
    orientedMessage: OrientedMessage,
    baseURL: String,
    onFileClick: (FileData.Text) -> Unit,
    onImageClick: (FileData.Image) -> Unit,
    isLoading: Boolean,
    uiConfig: ChatUIConfig,
    modifier: Modifier = Modifier,
) {

    val message = orientedMessage.message as Message.User

    Column(modifier = modifier) {
        if (message.dates != null) {
            DateView(message.dates!!, uiConfig)
        }
        if (message.text.isNotEmpty()) {
            UserTextMessageView(
                orientedMessage,
                uiConfig,
                isLoading
            )
        }
        if (message.files != null) {
            for (file in message.files!!) {
                when (file) {
                    is FileData.Text -> {
                        UserFileMessageView(
                            orientedMessage = orientedMessage,
                            file = file,
                            time = message.time,
                            uiConfig = uiConfig,
                            onFileClick = onFileClick,
                            isLoading = isLoading,
                        )
                    }
                    is FileData.Image -> {
                        UserImageMessageView(
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


@Preview
@Composable
internal fun UserMessageWidgetPreview() {
    Column {
        UserMessageView(
            orientedMessage = OrientedMessage(
                message = Message.User().apply {
                    text = "hello world"
                    time = "10:14"
                    visitor = true
                }
            ),
            baseURL = "",
            onFileClick = {},
            onImageClick = {},
            isLoading = false,
            uiConfig = ChatUIConfigDefault,
        )
        UserMessageView(
            orientedMessage = OrientedMessage(
                message = Message.User().apply {
                    text = "This is 2 files"
                    time = "16:45"
                    files = listOf(
                        FileData.Image(
                            preview = "/ru/file/image_thumb/278c438bd653f82adfc93249ed059f5481b714db/size/150",
                            thumb = "/ru/file/image_thumb/278c438bd653f82adfc93249ed059f5481b714db/size/150"
                        ).apply {
                            name = "mountain-landscape.jpg"
                            link = "/ru/file/inline_image/278c438bd653f82adfc93249ed059f5481b714db"
                        },
                        FileData.Text(
                            preview = false,
                            thumb = null
                        ).apply {
                            name = "very-important-text.txt"
                            link = "/ru/file/inline_image/278c438bd653f82adfc93249ed059f5481b714db"
                        }
                    )
                },
            ),
            baseURL = "https://tomass.helpdeskeddy.com",
            onFileClick = {},
            onImageClick = {},
            isLoading = false,
            uiConfig = ChatUIConfigDefault,
        )
    }
}