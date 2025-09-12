package com.example.online_chat_hde.core

import com.example.online_chat_hde.models.InitResponse
import com.example.online_chat_hde.models.NewMessageResponse
import com.example.online_chat_hde.models.PrependMessagesResponse
import com.example.online_chat_hde.models.SetStaffResponse
import com.example.online_chat_hde.models.StartChatResponse
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.VisitorMessage

sealed class ChatEvent {
    data object Connected: ChatEvent()
    data class Disconnected(val reasons: List<Any>): ChatEvent()
    data class ConnectError(val reasons: List<Any>): ChatEvent()
    data class ServerResponse(val event: ServerResponseEvent): ChatEvent()
    data class UserMessage(val event: UserMessageEvent): ChatEvent()
}


sealed class ServerResponseEvent {
    data class InitWidget(val data: InitResponse): ServerResponseEvent()
    data class NewMessage(val data: NewMessageResponse): ServerResponseEvent()
    data class PrependMessages(val data: PrependMessagesResponse): ServerResponseEvent()
    data class SetStaff(val data: SetStaffResponse): ServerResponseEvent()
    data class StartChat(val data: StartChatResponse): ServerResponseEvent()
    data object TicketCreated: ServerResponseEvent()
}

sealed class UserMessageEvent {
    data class Message(val data: VisitorMessage): UserMessageEvent()
    data class StartVisitorChat(val data: StartVisitorChatData): UserMessageEvent()
    data class LoadTicket(val ticket: Int): UserMessageEvent()
    data class VisitorIsTyping(val text: String): UserMessageEvent()
}