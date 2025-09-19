package com.example.online_chat_hde.core

object SocketEvents {
    const val VISITOR_MESSAGE = "visitor-message"
    const val SERVER_RESPONSE = "server-response"
    const val VISITOR_IS_TYPING = "visitor-is-typing"
    const val LOAD_TICKET = "load-ticket"
    const val START_VISITOR_CHAT = "start-visitor-chat"
}

object ActionTypes {
    const val INIT_WIDGET = "widget/initWidget"
    const val NEW_MESSAGE = "widget/newMessage"
    const val PREPEND_MESSAGES = "widget/prependMessages"
    const val SET_STAFF = "widget/setStaff"
    const val TICKET_CREATED = "widget/ticketCreated"
    const val START_CHAT = "widget/startChat"
}

object StorageKeys {
    const val STORAGE_NAME = "chatData"

    const val VISITOR_DATA = "visitorData"
    const val CHAT_BUTTONS = "chatButtons"
    const val MESSAGE_QUEUE = "messageQueue"
    const val START_CHAT_DATA = "startChatDatta"
    const val STAFF_DATA = "staffData"
}

object ButtonTypes {
    const val TEXT = "text"
    const val URL = "href"
    const val HASH = "hash"
}

object DateKeys {

    const val TODAY = "today"

    const val MONTHS_JANUARY = "months.january"
    const val MONTHS_FEBRUARY = "months.february"
    const val MONTHS_MARCH = "months.march"
    const val MONTHS_APRIL = "months.april"
    const val MONTHS_MAY = "months.may"
    const val MONTHS_JUNE = "months.june"
    const val MONTHS_JULY = "months.july"
    const val MONTHS_AUGUST = "months.august"
    const val MONTHS_SEPTEMBER = "months.september"
    const val MONTHS_OCTOBER = "months.october"
    const val MONTHS_NOVEMBER = "months.november"
    const val MONTHS_DECEMBER = "months.december"

    const val MONTHS_PLURAL_JANUARY = "months-plural.january"
    const val MONTHS_PLURAL_FEBRUARY = "months-plural.february"
    const val MONTHS_PLURAL_MARCH = "months-plural.march"
    const val MONTHS_PLURAL_APRIL = "months-plural.april"
    const val MONTHS_PLURAL_MAY = "months-plural.may"
    const val MONTHS_PLURAL_JUNE = "months-plural.june"
    const val MONTHS_PLURAL_JULY = "months-plural.july"
    const val MONTHS_PLURAL_AUGUST = "months-plural.august"
    const val MONTHS_PLURAL_SEPTEMBER = "months-plural.september"
    const val MONTHS_PLURAL_OCTOBER = "months-plural.october"
    const val MONTHS_PLURAL_NOVEMBER = "months-plural.november"
    const val MONTHS_PLURAL_DECEMBER = "months-plural.december"
}


object ErrorKeys {
    const val FILE_MAX_SIZE = "omnichannel.chat-widget.chat-form.max-size"
}