package com.example.online_chat_hde.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.online_chat_hde.models.ChatButton



@Composable
fun ButtonsColumn(
    buttons: List<ChatButton>,
    uiConfig: ChatUIConfig,
    widgetWidth: Dp,
    onChatButtonClick: (ChatButton) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {

        for (button in buttons) {
            Spacer(Modifier.height(uiConfig.dimensions.innerIndent/2))
            ChatMessageButton(
                button,
                uiConfig,
                { onChatButtonClick(button) },
                modifier = Modifier.width(widgetWidth),
            )
        }
    }
}



@Composable
fun ChatMessageButton(
    button: ChatButton,
    uiConfig: ChatUIConfig,
    onChatButtonClick: (ChatButton) -> Unit,
    modifier: Modifier = Modifier,
) {

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(uiConfig.dimensions.buttonCorners)
            .background(uiConfig.colors.buttonBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(
                    color = uiConfig.colors.buttonRipple,
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

