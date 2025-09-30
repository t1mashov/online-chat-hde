package com.example.online_chat_hde.ui.message

import androidx.compose.runtime.Composable

interface MessageFrame {
    val textMessage: @Composable () -> Unit
    val fileMessage: @Composable () -> Unit
    val imageMessage: @Composable () -> Unit
}