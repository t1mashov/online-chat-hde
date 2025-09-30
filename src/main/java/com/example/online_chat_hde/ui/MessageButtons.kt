package com.example.online_chat_hde.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.models.ChatButton



@Composable
fun ChatUIScope.ChatMessageButton(
    button: ChatButton,
    onChatButtonClick: (ChatButton) -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(uiConfig.dimensions.buttonCorners)
            .background(uiConfig.colors.buttonBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    color = uiConfig.colors.buttonRipple
                )
            ) {
                onChatButtonClick(button)
            }
            .padding(uiConfig.dimensions.buttonPadding)
    ) {

        Text(
            text = button.text,
            fontSize = uiConfig.dimensions.messageFontSize,
            color = uiConfig.colors.buttonText,
            textAlign = TextAlign.Center
        )

    }
}

