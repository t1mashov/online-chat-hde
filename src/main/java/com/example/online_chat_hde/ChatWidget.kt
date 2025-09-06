package com.example.online_chat_hde

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.online_chat_hde.core.ButtonTypes
import com.example.online_chat_hde.core.ChatSdk
import com.example.online_chat_hde.core.ChatService
import com.example.online_chat_hde.core.ChatViewModel
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.ui.ChatView
import com.example.online_chat_hde.ui.ImageFullScreen
import org.json.JSONObject

@Composable
fun ChatWidget(
    viewModel: ChatViewModel,
    chatService: ChatService,
    onClose: () -> Unit
) {

    val ctx = LocalContext.current

    val nav = rememberNavController()

    // стандартный обработчик нажатия кнопок в сообщениях чата
    if (ChatSdk.onClickChatButton == null) {
        ChatSdk.onClickChatButton = {
            when (it.type) {
                ButtonTypes.TEXT -> {
                    chatService.sendMessage(
                        VisitorMessage(text = it.text, files = listOf())
                    )
                }
                ButtonTypes.URL, ButtonTypes.HASH -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.value)).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                    }
                    try {
                        ctx.startActivity(intent)
                    } catch (e: Exception) {
                        println("[Open browser error] >>> $e")
                    }
                }
                else -> {
                    println("[Undefined type of button]")
                }
            }
        }
    }

    // стандартный обработчик нажатия на файлы
    if (ChatSdk.onClickFile == null) {
        ChatSdk.onClickFile = {
            val fullLink = if (it.link.contains("://")) it.link
            else viewModel.service.serverOptions.originUrl + it.link
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullLink)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            try {
                ctx.startActivity(intent)
            } catch (e: Exception) {

            }
        }
    }

    // Привязываем дефолтный обработчик к текущему nav и чистим его при dispose
    DisposableEffect(nav) {
        // стандартный обработчик нажатия картинок
        if (ChatSdk.onClickImage == null) {
            val handler: (FileData.Image) -> Unit = { image ->
                // сохраняем аргумент в текущем entry
                nav.currentBackStackEntry?.savedStateHandle?.set("img", image.toJson().toString())

                // защита от повторной навигации на тот же экран
                if (nav.currentDestination?.route != ChatRoutes.IMAGE) {
                    nav.navigate(ChatRoutes.IMAGE) {
                        launchSingleTop = true
                    }
                }
            }
            ChatSdk.onClickImage = handler

            onDispose {
                // очищаем только если это именно наш хендлер (чтобы не трогать пользовательский)
                if (ChatSdk.onClickImage === handler) ChatSdk.onClickImage = null
            }
        } else {
            onDispose { /* чужой обработчик — не трогаем */ }
        }
    }

    BackHandler {
        viewModel.clickCloseChat()
        onClose()
    }

    NavHost(
        navController = nav,
        startDestination = ChatRoutes.CHAT
    ) {
        composable(ChatRoutes.CHAT) {
            ChatView(
                viewModel = viewModel,
                uiConfig = ChatSdk.defaultUi,
                modifier = Modifier
                    .fillMaxSize(),
                onClickClose = {
                    ChatSdk.onClickClose()
                    onClose()
                },
                onClickSend = ChatSdk.onClickSend,
                onMessageTyping = ChatSdk.onMessageTyping,
                onClickLoadDocument = ChatSdk.onClickLoadDocument,
                onClickImage = { ChatSdk.onClickImage?.invoke(it) },
                onClickFile = { ChatSdk.onClickFile?.invoke(it) },
                onClickChatButton = { ChatSdk.onClickChatButton?.invoke(it) }
            )
        }
        composable(ChatRoutes.IMAGE) {
            val json = remember {
                nav.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>("img")
                    .also { nav.previousBackStackEntry?.savedStateHandle?.remove<String>("img") }
            }

            val image = remember(json) { FileData.Image.fromJson(JSONObject(json)) }

            ImageFullScreen(
                image = image,
                baseURL = chatService.serverOptions.originUrl,
                uiConfig = ChatSdk.defaultUi
            )

        }
    }

}