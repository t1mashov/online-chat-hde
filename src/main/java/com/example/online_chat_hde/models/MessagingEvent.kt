package com.example.online_chat_hde.models


sealed class MessagingEvent {
    /** Ответы со стороны сервера */
    sealed class Server: MessagingEvent() {
        /** Инициирующее сообщение, содержит информацию о чате, пользователе и др. */
        data class InitWidget(val response: InitResponse): Server()
        /** Новое сообщение пользователя или агента */
        data class NewMessage(val response: NewMessageResponse): Server()
        /** Предыдущие сообщения */
        data class PrependMessages(val response: PrependMessagesResponse): Server()
        /** Агент был назначен (или снят) на чат */
        data class SetStaff(val response: SetStaffResponse): Server()
        /** Был начат новый чат (после ввода данных пользователя в тикете и нажатия кнопки отправки) */
        data class StartChat(val response: StartChatResponse): Server()
        /** Тикет был создан */
        data object TicketCreated: Server()

        data class RateSuccess(val data: Boolean): Server()

        data object CloseChat: Server()
    }

    /** События инициируемые клиентом */
    sealed class User: MessagingEvent() {
        /** Старт нового чата (после ввода данных пользователя в тикете и нажатия кнопки отправки) */
        data class StartVisitorChat(val data: StartVisitorChatData): User()
        /** Пользователь запросил загрузку предыдущих сообщений тикета */
        data class LoadTicket(val ticket: Int): User()
        /** Пользователь вводит текст сообщения */
        data class VisitorIsTyping(val text: String): User()
        /** Сообщение отправлено но еще не подтверждено сервером */
        data class LoadingMessage(val data: VisitorMessage): User()
        /** Пользователь оценил чат */
        data class RateChat(val data: UserRate): User()
    }
}
