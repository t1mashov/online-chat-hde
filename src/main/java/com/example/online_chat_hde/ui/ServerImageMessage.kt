package com.example.online_chat_hde.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.online_chat_hde.models.FileData


@Composable
internal fun ServerImageMessageView(
    image: FileData.Image,
    time: String,
    baseURL: String,
    onImageClick: (FileData.Image) -> Unit,
    uiConfig: ChatUIConfig
) {

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = uiConfig.dimensions.messageIndent)
    ) {

        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data( if (image.thumb.contains("://")) image.thumb else baseURL + image.thumb)
                .crossfade(true)
                .build()
        )

        val state by painter.state.collectAsState()

        Row {

            when (state) {
                is AsyncImagePainter.State.Loading -> {
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

                is AsyncImagePainter.State.Success -> {
                    Box(
                        modifier = Modifier
                            .clip(uiConfig.dimensions.userImageMessagesCorners)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(
                                    color = uiConfig.colors.imageRipple
                                )
                            ) {
                                onImageClick(image)
                            }
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = image.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(uiConfig.dimensions.userImageMessageSize)
                                .clip(uiConfig.dimensions.userImageMessagesCorners)
                        )
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
                }

                else -> {
                    Box {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(uiConfig.dimensions.userImageMessageSize)
                                .clip(uiConfig.dimensions.userImageMessagesCorners)
                                .background(uiConfig.colors.userLoadingImageColor)
                        ) {
                            Text(text = "ОШИБКА", color = uiConfig.colors.userMessageText, fontSize = 20.sp)
                        }

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
                }
            }
        }
    }
}


@Preview
@Composable
internal fun ServerImageMessagePreview() {
    ServerImageMessageView(
        image = FileData.Image(
            thumb = "/ru/file/image_thumb/278c438bd653f82adfc93249ed059f5481b714db/size/150"
        ).apply {
            name = "mountain-landscape.jpg"
            link = "/ru/file/inline_image/278c438bd653f82adfc93249ed059f5481b714db"
        },
        baseURL = "https://tomass.helpdeskeddy.com",
        time = "12:00",
        onImageClick = {},
        uiConfig = ChatUIConfigDefault
    )
}