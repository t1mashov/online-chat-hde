package com.example.online_chat_hde.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class ReconnectManager(
    private val service: ChatService
) {

    private val reconnecting = java.util.concurrent.atomic.AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var reconnectAttempts = 0

    private val connection = service.connection

    // Вызовет reconnect() если это нужно
    fun triggerReconnect(): ReconnectState {
        // Реконнект уже идет
        if (reconnecting.get()) return ReconnectState.Unnecessary
        // Отключение было намеренно
        if (service.manualDisconnect) return ReconnectState.Unnecessary
        // реконнект нужен только если соединение было и пропало
        if ( !(connection.value is ConnectionState.Disconnected || connection.value is ConnectionState.Error)) return ReconnectState.Unnecessary
        // Уже есть подключение
        if (service.isConnected()) return ReconnectState.Unnecessary
        // Максимальное кол-во переподключений
        if (reconnectAttempts > service.serverOptions.maxReconnectAttempts) return ReconnectState.ExceededMaxAttempts

        reconnecting.set(true)

        scope.launch {
            reconnectWithBackoff()
            delay(3000)
            reconnecting.set(false)
            reconnectAttempts += 1
        }
        return ReconnectState.Attempt
    }


    private suspend fun reconnectWithBackoff() = mutex.withLock {
        println("[reconnect with backoff]")
        service.userData?.let {
            service.buildSocket(Payload.Auth.fromVisitorData(it))
            service.attachListeners()
        }
        service.connect()
    }


    fun resetAttempts() {
        reconnectAttempts = 0
    }
}


internal sealed class ReconnectState {
    data object Unnecessary: ReconnectState()
    data object ExceededMaxAttempts: ReconnectState()
    data object Attempt: ReconnectState()
}