package com.example.online_chat_hde

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.example.online_chat_hde.core.ChatHDE
import com.example.online_chat_hde.core.ChatViewModel
import com.example.online_chat_hde.core.ChatViewModelFactory


private fun styleFor(color: Int): SystemBarStyle {
    val isLightBg = ColorUtils.calculateLuminance(color) > 0.5
    return if (isLightBg) {
        SystemBarStyle.light(color, color)
    } else {
        SystemBarStyle.dark(color)
    }
}



class ChatActivity : ComponentActivity() {

    private val chatService by lazy { ChatHDE.requireService() }
    private val viewModel by viewModels<ChatViewModel> { ChatViewModelFactory(chatService) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge(
            statusBarStyle = styleFor(ChatHDE.defaultUi.colors.statusBarBackground.toArgb()),
            navigationBarStyle = styleFor(ChatHDE.defaultUi.colors.navigationBarBackground.toArgb())
        )

        setContent {

            Scaffold(
                containerColor = Color.Transparent,
                modifier = Modifier
                    .imePadding()
                    .fillMaxSize()
            ) { paddingValues ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ChatView(viewModel) {
                        finish()
                    }
                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.connect()
    }
}


object ChatRoutes {
    const val CHAT = "chat"
    const val IMAGE = "image"
}