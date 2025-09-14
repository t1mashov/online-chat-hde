package com.example.online_chat_hde.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatEngine(
    private val chatService: ChatService,
    private val scope: CoroutineScope
) {
    private var runJob: Job? = null

    fun start() {
        if (runJob?.isActive == true) return
        runJob = scope.launch {
            // ваш кастомный реконнект/поддержка соединения
            chatService.initConnect()
            // при желании — слушатели состояния/очередей и т.п.
        }
    }

    fun stop() {
        runJob?.cancel()
        chatService.disconnect()
    }

    val incoming: Flow<ConnectionEvent> get() = chatService.connectionEvents
    val connection: StateFlow<ConnectionState> get() = chatService.connectionState
}