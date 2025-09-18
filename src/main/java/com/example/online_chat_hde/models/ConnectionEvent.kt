package com.example.online_chat_hde.models


/** События соединения */
sealed class ConnectionEvent {
    data object Connected: ConnectionEvent()
    data class Disconnected(val reasons: List<Any>): ConnectionEvent()
    data class ConnectError(val reasons: List<Any>): ConnectionEvent()
    data object ReconnectionAttempt: ConnectionEvent()
}
