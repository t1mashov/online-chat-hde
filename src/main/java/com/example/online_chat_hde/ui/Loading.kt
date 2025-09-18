package com.example.online_chat_hde.ui

import android.widget.ImageView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.online_chat_hde.R

@Composable
internal fun MessageLoading(
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