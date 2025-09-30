package com.example.online_chat_hde.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.models.Staff


interface ChatTopPanelFrame {
    val logo: @Composable () -> Unit
    val text: @Composable () -> Unit
    val closeButton: @Composable () -> Unit
}

interface ChatTopPanelScope: ChatUIScope {
    val staff: Staff?
    val closeChat: () -> Unit
}

@Composable
fun ChatTopPanelScope.ChatHeader(
    panelBackground: Color = uiConfig.colors.topPanelBackground,
    panelPadding: Dp = uiConfig.dimensions.topPanelPadding,

    logo: @Composable ChatTopPanelScope.() -> Unit = {
        Logo()
    },
    text: @Composable ChatTopPanelScope.() -> Unit = {
        UnassignedText()
    },

    closeButton: @Composable ChatTopPanelScope.() -> Unit = {
        CloseButton()
    },

    frame: @Composable ChatTopPanelScope.(
        ChatTopPanelFrame
            ) -> Unit = {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .background(panelBackground)
        ) {
            it.logo()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = panelPadding)
                    .heightIn(min = uiConfig.dimensions.logoSize)
            ) {
                it.text()
            }
            it.closeButton()
        }
    }
) {

    val layout = object : ChatTopPanelFrame {
        override val logo: @Composable () -> Unit = { with(this@ChatHeader) {logo()} }
        override val text: @Composable () -> Unit = { with(this@ChatHeader) {text()} }
        override val closeButton: @Composable () -> Unit = { with(this@ChatHeader) {closeButton()} }
    }

    frame(layout)
}


@Composable
fun ChatTopPanelScope.UnassignedText(
    defaultText: String = uiConfig.texts.unassigned,
    color: Color = uiConfig.colors.topPanelText,
    fontSize: TextUnit = uiConfig.dimensions.messageFontSize
) {
    if (staff != null) {
        Text(
            text = staff!!.name,
            color = color,
            fontSize = fontSize,
        )
    }
    else {
        Text(
            text = defaultText,
            color = color,
            fontSize = fontSize,
        )
    }
}


@Composable
fun ChatTopPanelScope.Logo() {
    if (staff != null) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data("https:" + staff!!.image)
                .crossfade(true)
                .build()
        )
        val state by painter.state.collectAsState()
        when (state) {
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = null,
                    alignment = Alignment.TopStart,
                    modifier = Modifier
                        .padding(uiConfig.dimensions.topPanelPadding)
                        .size(uiConfig.dimensions.logoSize)
                        .clip(uiConfig.dimensions.staffIconCorners)
                )
            }

            else -> {
                Image(
                    imageVector = ImageVector.vectorResource(uiConfig.media.noStaffLogo),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(uiConfig.colors.topPanelText),
                    alignment = Alignment.TopStart,
                    modifier = Modifier
                        .padding(uiConfig.dimensions.topPanelPadding)
                        .size(uiConfig.dimensions.logoSize)
                )
            }
        }
    }
    else {
        Image(
            imageVector = ImageVector.vectorResource(uiConfig.media.noStaffLogo),
            contentDescription = null,
            colorFilter = ColorFilter.tint(uiConfig.colors.topPanelText),
            alignment = Alignment.TopStart,
            modifier = Modifier
                .padding(uiConfig.dimensions.topPanelPadding)
                .size(uiConfig.dimensions.logoSize)
        )
    }
}


@Composable
fun ChatTopPanelScope.CloseButton(
    background: Color = uiConfig.colors.closeChatButtonBackground,
    ripple: Color = uiConfig.colors.userRipple,
    innerPadding: Dp = uiConfig.dimensions.topPanelPadding,
    closeLogo: Int = uiConfig.media.closeChatLogo,
    closeLogoColor: Color = uiConfig.colors.closeChatButtonIcon,
    logoSize: Dp = uiConfig.dimensions.logoSize
) {
    Box(
        modifier = Modifier
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    color = ripple
                )
            ) {
                closeChat()
            }
            .padding(innerPadding)
    ) {
        Image(
            imageVector = ImageVector.vectorResource(closeLogo),
            contentDescription = null,
            colorFilter = ColorFilter.tint(closeLogoColor),
            modifier = Modifier.size(logoSize)
        )
    }
}