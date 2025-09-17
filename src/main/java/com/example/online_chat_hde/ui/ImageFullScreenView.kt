package com.example.online_chat_hde.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import coil3.size.Size
import com.example.online_chat_hde.models.FileData

@Composable
fun ImageFullScreen(
    image: FileData.Image,
    baseURL: String,
    uiConfig: ChatUIConfig
) {
    println("SDK[ImageFullScreen] >>> ${image.name}")
    val ctx = LocalContext.current

    val url = remember(image.link, baseURL) {
        if ((image.preview ?: image.thumb).contains("://")) image.link else baseURL + image.link
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(ctx)
            .data(url)
            .crossfade(false)
            .size(Size.ORIGINAL)
            .precision(Precision.EXACT)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    )
    val state by painter.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MessageLoading(color = uiConfig.colors.userLoadingImageColor, size = 40.dp)
                }
            }
            is AsyncImagePainter.State.Success -> {
                ZoomableImage(
                    painter = painter,
                    contentDescription = image.name
                )
            }
            is AsyncImagePainter.State.Error -> {
                Text(
                    text = "Не удалось загрузить изображение",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {}
        }

    }
}


@Composable
private fun ZoomableImage(
    painter: Painter,
    contentDescription: String?
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val minScale = 1f
    val maxScale = 5f

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    fun clampOffset(off: Offset, scl: Float): Offset {
        if (scl <= 1f || containerSize == IntSize.Zero) return Offset.Zero
        val maxX = (containerSize.width  * (scl - 1f)) / 2f
        val maxY = (containerSize.height * (scl - 1f)) / 2f
        return Offset(
            x = off.x.coerceIn(-maxX, maxX),
            y = off.y.coerceIn(-maxY, maxY)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, gestureZoom, _ ->
                    val old = scale
                    val new = (scale * gestureZoom).coerceIn(minScale, maxScale)

                    // компенсируем зум относительно центра контейнера
                    val center = Offset(
                        x = containerSize.width / 2f,
                        y = containerSize.height / 2f
                    )
                    val focus = centroid - center
                    // смещение = пан + компенсация под фокус
                    offset = clampOffset(
                        off = offset + pan + focus * (1 - new / old),
                        scl = new
                    )
                    scale = new
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { pos ->
                        val target = if (scale < 2f) 2.5f else 1f
                        val old = scale
                        val center = Offset(
                            x = containerSize.width / 2f,
                            y = containerSize.height / 2f
                        )
                        val focus = pos - center
                        scale = target
                        offset = clampOffset(
                            off = if (target == 1f) Offset.Zero
                            else offset + focus * (1 - target / old),
                            scl = target
                        )
                    }
                )
            }
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,   // показывает целиком при scale=1
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    transformOrigin = TransformOrigin(0.5f, 0.5f),
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}

