package com.example.online_chat_hde

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.online_chat_hde.core.ButtonTypes
import com.example.online_chat_hde.core.ChatHDE
import com.example.online_chat_hde.core.ChatViewModel
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.ui.ChatMain
import com.example.online_chat_hde.ui.ImageFullScreen
import org.json.JSONObject

@Composable
fun ChatView(
    onClose: () -> Unit
) {
    val ctx = LocalContext.current

    val vm: ChatViewModel = viewModel(factory = ChatHDE.chatViewModelFactory())
    val chatService = vm.service

    val nav = rememberNavController()

    // стандартный обработчик нажатия кнопок в сообщениях чата
    ChatHDE.clickChatButtonActionDefault = {
        when (it.type) {
            ButtonTypes.TEXT -> {
                chatService.sendMessage(
                    VisitorMessage(text = it.text, files = listOf())
                )
            }
            ButtonTypes.URL, ButtonTypes.HASH -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.value)).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                    }
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


    // стандартный обработчик нажатия на файлы
    ChatHDE.clickFileActionDefault = {
        val fullLink = if (it.link.contains("://")) it.link
        else chatService.serverOptions.originUrl + it.link
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullLink)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {

        }
    }


    // Стандартный обработчик отправки сообщений
    ChatHDE.clickSendActionDefault = {
        vm.sendMessage(it)
    }


    // Привязываем дефолтный обработчик к текущему nav и чистим его при dispose
    DisposableEffect(nav) {
        // стандартный обработчик нажатия картинок
        ChatHDE.clickImageActionDefault = { image ->
            // сохраняем аргумент в текущем entry
            nav.currentBackStackEntry?.savedStateHandle?.set("img", image.toJson().toString())

            // защита от повторной навигации на тот же экран
            if (nav.currentDestination?.route != ChatRoutes.IMAGE) {
                nav.navigate(ChatRoutes.IMAGE) {
                    launchSingleTop = true
                }
            }
        }

        onDispose {
            ChatHDE.clickImageActionDefault = {}
        }
    }

    BackHandler {
        onClose()
        vm.closeChat()
    }

    NavHost(
        navController = nav,
        startDestination = ChatRoutes.CHAT
    ) {
        composable(ChatRoutes.CHAT) {
            ChatMain(
                viewModel = vm,
                uiConfig = ChatHDE.defaultUi,
                modifier = Modifier
                    .fillMaxSize(),
                onClickClose = {
                    onClose()
                },
                onClickSend = { (ChatHDE.clickSendAction ?: ChatHDE.clickSendActionDefault).invoke(it) },
                onMessageTyping = { ChatHDE.onMessageTyping?.invoke(it) },
                onClickLoadDocument = { (ChatHDE.clickLoadDocumentAction ?: ChatHDE.clickLoadDocumentActionDefault).invoke() },
                onClickImage = { vm.saveScroll(); (ChatHDE.clickImageAction ?: ChatHDE.clickImageActionDefault).invoke(it) },
                onClickFile = { (ChatHDE.clickFileAction ?: ChatHDE.clickFileActionDefault).invoke(it) },
                onClickChatButton = { (ChatHDE.clickChatButtonAction ?: ChatHDE.clickChatButtonActionDefault).invoke(it) }
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

            BackHandler {
                vm.restoreScroll()
                nav.popBackStack()
            }

            ImageFullScreen(
                image = image,
                baseURL = chatService.serverOptions.originUrl,
                uiConfig = ChatHDE.defaultUi
            )

        }
    }

}