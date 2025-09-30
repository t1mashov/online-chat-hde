package com.example.online_chat_hde.ui

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.R

@Composable
fun MessageLoading(
    color: Color,
    size: Dp
) {
    val context = LocalContext.current
    val avd = remember {
        AnimatedVectorDrawableCompat.create(context, R.drawable.loading)?.apply {
            setTint(color.toArgb())
        }
    }

    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                setImageDrawable(avd)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
        },
        modifier = Modifier.size(size)
    )

    LaunchedEffect(avd) {
        avd?.start()
    }
}



@Composable
fun ChatUIScope.GlobalLoading() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(uiConfig.colors.loadingBackground)
    ) {
        MessageLoading(
            uiConfig.colors.userMessageBackground,
            uiConfig.dimensions.loadingLogoSize
        )
    }
}