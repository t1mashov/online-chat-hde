package com.example.online_chat_hde.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.online_chat_hde.ChatUIScope

interface PrependMessagesScope: ChatUIScope {
    val showPrependMessages: () -> Unit
}

@Composable
fun PrependMessagesScope.PrependMessages() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    color = uiConfig.colors.topPanelBackground
                )
            ) {
                showPrependMessages()
            }
            .background(uiConfig.colors.showPrependMessagesBackground)
            .padding(uiConfig.dimensions.topPanelPadding)
    ) {
        Text(
            text = uiConfig.texts.showPrependMessages,
            fontSize = uiConfig.dimensions.messageFontSize,
            color = uiConfig.colors.topPanelBackground
        )
    }
}