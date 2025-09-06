package com.example.online_chat_hde

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.online_chat_hde.core.ButtonTypes
import com.example.online_chat_hde.core.ChatSdk
import com.example.online_chat_hde.core.ChatViewModel
import com.example.online_chat_hde.core.ChatViewModelFactory
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.ui.ChatView
import com.example.online_chat_hde.ui.ImageFullScreen
import org.json.JSONObject


private fun styleFor(color: Int): SystemBarStyle {
    val isLightBg = ColorUtils.calculateLuminance(color) > 0.5
    return if (isLightBg) {
        SystemBarStyle.light(color, color)
    } else {
        SystemBarStyle.dark(color)
    }
}



class ChatActivity : ComponentActivity() {

    private val chatService by lazy { ChatSdk.requireService() }
    private val viewModel by viewModels<ChatViewModel> { ChatViewModelFactory(chatService) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge(
            statusBarStyle = styleFor(ChatSdk.defaultUi.colors.statusBarBackground.toArgb()),
            navigationBarStyle = styleFor(ChatSdk.defaultUi.colors.navigationBarBackground.toArgb())
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
                    ChatWidget(viewModel, chatService) {
                        finish()
                    }
                }
            }

        }
    }
}


object ChatRoutes {
    const val CHAT = "chat"
    const val IMAGE = "image"
}