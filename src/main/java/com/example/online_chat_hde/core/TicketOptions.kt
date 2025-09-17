package com.example.online_chat_hde.core

data class TicketOptions(
    val showNameField: Boolean = false,
    val showEmailField: Boolean = false,
    val isEmailRequired: Boolean = false,
    /** Ссылка на согласине на обработку персональных данных */
    val consentLink: String? = null
)

data class TicketOptionsWithStatus(
    val options: TicketOptions,
    val status: TicketStatus
)

enum class TicketStatus {
    STAFF_OFFLINE,
    FIRST_MESSAGE,
    WAIT_FOR_REPLY,
    CHAT_ACTIVE
}