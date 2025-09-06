package com.example.online_chat_hde.ui

import android.text.TextPaint
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit


fun calculateFileMessageWidth(
    text: String,
    density: Density,
    uiConfig: ChatUIConfig
): Dp {
    return calculateTextWidth(text, density, uiConfig.dimensions.messageFontSize) +
            uiConfig.dimensions.innerIndent*4 +
            uiConfig.dimensions.pyperclipSize
}



fun calculateTextMessageWidth(
    text: String,
    density: Density,
    uiConfig: ChatUIConfig
): Dp {
    return calculateTextWidth(text, density, uiConfig.dimensions.messageFontSize) +
            uiConfig.dimensions.innerIndent +
            uiConfig.dimensions.pyperclipSize +
            uiConfig.dimensions.messagePadding*2
}


fun calculateTimeMessageWidth(
    text: String,
    density: Density,
    uiConfig: ChatUIConfig
): Dp {
    return calculateTextWidth(text, density, uiConfig.dimensions.timeFontSize) +
            uiConfig.dimensions.innerIndent +
            uiConfig.dimensions.pyperclipSize +
            uiConfig.dimensions.messagePadding*2
}


fun calculateTextWidth(
    text: String,
    density: Density,
    fontSize: TextUnit
): Dp {
    val textPaint = TextPaint()

    with(density) {
        textPaint.textSize = fontSize.toPx()
    }

    val textWidthPx = textPaint.measureText(text)
    val textSize = with(density) { textWidthPx.toDp() }

    return textSize
}