package com.example.online_chat_hde.core

import com.example.online_chat_hde.models.InitResponse
import com.example.online_chat_hde.models.NewMessageResponse
import com.example.online_chat_hde.models.PrependMessagesResponse
import com.example.online_chat_hde.models.SetStaffResponse
import com.example.online_chat_hde.models.StartChatResponse
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.VisitorMessage


sealed class MessagingEvent {
    /** Ответы со стороны сервера */
    sealed class Server: MessagingEvent() {
        data class InitWidget(val data: InitResponse): Server()
        data class NewMessage(val data: NewMessageResponse): Server()
        data class PrependMessages(val data: PrependMessagesResponse): Server()
        data class SetStaff(val data: SetStaffResponse): Server()
        data class StartChat(val data: StartChatResponse): Server()
        data object TicketCreated: Server()
    }

    /** События инициируемые клиентом */
    sealed class User: MessagingEvent() {
        /**  */
        data class StartVisitorChat(val data: StartVisitorChatData): User()
        /**  */
        data class LoadTicket(val ticket: Int): User()
        /**  */
        data class VisitorIsTyping(val text: String): User()
        /** Сообщение отправлено но еще не подтверждено сервером */
        data class LoadingMessage(val data: VisitorMessage): User()
    }
}
