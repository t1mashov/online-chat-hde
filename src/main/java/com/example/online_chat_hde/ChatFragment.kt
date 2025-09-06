package com.example.online_chat_hde

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.online_chat_hde.core.ChatSdk
import com.example.online_chat_hde.core.ChatViewModel
import com.example.online_chat_hde.core.ChatViewModelFactory
import androidx.fragment.app.viewModels

class ChatFragment: Fragment() {

    companion object {
        const val RESULT_KEY = "chat_result"
        const val RESULT_CLOSE = "chat_close"

        fun newInstance(): ChatFragment = ChatFragment()
    }

    private val chatService by lazy { ChatSdk.requireService() }
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(chatService)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ChatWidget(
                    viewModel = viewModel,
                    chatService = chatService,
                    onClose = {
                        parentFragmentManager.setFragmentResult(
                            RESULT_KEY,
                            bundleOf("action" to RESULT_CLOSE)
                        )
                    }
                )
            }
        }
    }
}