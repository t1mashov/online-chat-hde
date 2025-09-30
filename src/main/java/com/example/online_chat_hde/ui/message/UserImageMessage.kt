package com.example.online_chat_hde.ui.message

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ChatUIConfigDefault
import com.example.online_chat_hde.ui.MessageLoading
import com.example.online_chat_hde.viewmodels.UiMessage




@Composable
fun UserMessageScope.UserImageMessage(image: FileData.Image) {

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(image.thumb)
            .crossfade(true)
            .build()
    )

    val state by painter.state.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {

        Row(horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()) {

            when (state) {
                is AsyncImagePainter.State.Loading -> {
                    ImageLoading()
                }

                is AsyncImagePainter.State.Success -> {
                    Box(
                        modifier = Modifier
                            .clip(uiConfig.dimensions.userImageMessagesCorners)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = uiConfig.colors.imageRipple)
                            ) { onImageClick(image) }
                    ) {
                        ImageSuccess(painter)
                        ImageTime()
                    }
                }

                else -> {
                    Box {
                        ImageError()
                        ImageTime()
                    }
                }
            }
        }
    }
}



@Composable
fun UserMessageScope.ImageSuccess(painter: Painter) {
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(uiConfig.dimensions.userImageMessageSize)
    )
}


@Composable
fun ChatUIScope.ImageError() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(uiConfig.dimensions.userImageMessageSize)
            .clip(uiConfig.dimensions.userImageMessagesCorners)
            .background(uiConfig.colors.userLoadingImageColor)
    ) {
        Text(text = "ОШИБКА", color = uiConfig.colors.userMessageText, fontSize = 20.sp)
    }
}


@Composable
fun ChatUIScope.ImageLoading() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(uiConfig.dimensions.userImageMessageSize)
            .clip(uiConfig.dimensions.userImageMessagesCorners)
            .background(uiConfig.colors.userLoadingImageColor)
    ) {
        MessageLoading(
            color = uiConfig.colors.userMessageText,
            size = 40.dp
        )
    }
}


@Composable
fun UserMessageScope.ImageTime() {

    val time = uiMessage.message.time

    Box(contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .size(uiConfig.dimensions.userImageMessageSize)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(uiConfig.dimensions.timeOnImageCorners)
                .background(uiConfig.colors.timeOnImageBackground)
                .padding(4.dp)
        ) {
            Text(
                text = time,
                color = uiConfig.colors.timeOnImageText,
                fontSize = uiConfig.dimensions.timeFontSize
            )
        }
    }
}





@Preview
@Composable
internal fun UserImageMessagePreview() {

    val itemScope = object : UserMessageScope {
        override val getTextWidth: (text: String, fontSize: TextUnit) -> Dp
            get() = {_, _ -> 0.dp}
        override val uiMessage: UiMessage = UiMessage(Message.User().apply {
            time = "19:45"
        })
        override val baseURL: String = "https://tomass.helpdeskeddy.com"
        override val timeWidth: Dp = 0.dp
        override val onFileClick: (FileData.Text) -> Unit = {}
        override val onImageClick: (FileData.Image) -> Unit = {}
        override val uiConfig = ChatUIConfigDefault
    }

    with(itemScope) {
        UserImageMessage(FileData.Image(
            thumb = "/ru/file/image_thumb/278c438bd653f82adfc93249ed059f5481b714db/size/150"
        ).apply {
            name = "mountain-landscape.jpg"
            link = "/ru/file/inline_image/278c438bd653f82adfc93249ed059f5481b714db"
        })
    }

}