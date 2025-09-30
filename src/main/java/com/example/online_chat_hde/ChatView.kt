package com.example.online_chat_hde

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.online_chat_hde.core.ButtonTypes
import com.example.online_chat_hde.core.ChatHDE
import com.example.online_chat_hde.viewmodels.ChatViewModel
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.VisitorMessage
import com.example.online_chat_hde.ui.ChatPage
import com.example.online_chat_hde.ui.ChatRoute
import com.example.online_chat_hde.ui.ChatScope
import com.example.online_chat_hde.ui.ChatUIConfig
import com.example.online_chat_hde.ui.ImageFullScreen
import com.example.online_chat_hde.ui.Ticket
import com.example.online_chat_hde.ui.TicketScope
import com.example.online_chat_hde.viewmodels.TicketViewModel
import org.json.JSONObject


interface ChatUIScope {
    val uiConfig: ChatUIConfig
}



@Composable
fun ChatView(
    onClose: () -> Unit,
    ticket: @Composable TicketScope.() -> Unit = {
        Ticket()
    },
    chat: @Composable ChatScope.() -> Unit = {
        ChatPage()
    }
) {
    val ctx = LocalContext.current

    val chatVM: ChatViewModel = viewModel(factory = ChatHDE.chatViewModelFactory())
    val ticketVM: TicketViewModel = viewModel(factory = ChatHDE.ticketViewModelFactory())
    val chatService = chatVM.client

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
        chatVM.sendMessage(it)
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

    val uiScope = object : ChatUIScope {
        override val uiConfig: ChatUIConfig = ChatHDE.defaultUi
    }


    BackHandler {
        onClose()
    }

    NavHost(
        navController = nav,
        startDestination = ChatRoutes.CHAT
    ) {
        composable(ChatRoutes.CHAT) {
            with (uiScope) {
                ChatRoute(
                    chatViewModel = chatVM,
                    ticketViewModel = ticketVM,
                    onClickClose = {
                        onClose()
                    },
                    onClickSend = { (ChatHDE.clickSendAction ?: ChatHDE.clickSendActionDefault).invoke(it) },
                    onMessageTyping = { ChatHDE.onMessageTyping?.invoke(it) },
                    onClickImage = { (ChatHDE.clickImageAction ?: ChatHDE.clickImageActionDefault).invoke(it) },
                    onClickFile = { (ChatHDE.clickFileAction ?: ChatHDE.clickFileActionDefault).invoke(it) },
                    onClickChatButton = { (ChatHDE.clickChatButtonAction ?: ChatHDE.clickChatButtonActionDefault).invoke(it) },

                    ticket = ticket,
                    chat = chat
                )
            }
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