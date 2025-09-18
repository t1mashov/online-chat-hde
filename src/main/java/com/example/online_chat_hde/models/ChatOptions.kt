package com.example.online_chat_hde.models

data class ChatOptions(
    val welcomeMessage: String,
    val botName: String,
    val saveUserAfterConnection: Boolean = true,
    val maxUploadFileSizeMB: Int = 20
)