package com.example.online_chat_hde.models

/** Состояние соединения */
sealed class ConnectionState {
    /** Еще ни разу не был вызван connect() */
    data object NeverConnected : ConnectionState()
    /** Ранее было соединение, но сейчас нет */
    data object Disconnected : ConnectionState()
    /** Идет подключение */
    data object Connecting : ConnectionState()
    /** Соединение установлено */
    data object Connected : ConnectionState()
    /** Ошибка попытки подключения */
    data class Error(val cause: Throwable? = null) : ConnectionState()
}