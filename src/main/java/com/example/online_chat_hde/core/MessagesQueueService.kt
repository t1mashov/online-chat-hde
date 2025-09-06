package com.example.online_chat_hde.core

import android.content.Context
import com.example.online_chat_hde.models.WNewMessage

internal class MessagesQueueService(
    private val service: ChatService,
    context: Context
) {

    private val sharedPrefs = SharedPrefs(context)

    private var isQueueActive = false

    fun checkNextQueueMessage(message: WNewMessage) {
        sharedPrefs.removeMessageByText(message.data.text)
        sendFirstMessageFromQueue()
    }


    fun disableQueue() {
        isQueueActive = false
    }

    // Запуск отправки сообщений из очереди
    fun startSendMessageQueue() {
        if (service.isConnected() && !isQueueActive) {
            sendFirstMessageFromQueue()
        }
    }

    // Отправляем 1 сообщение из очереди
    private fun sendFirstMessageFromQueue() {
        val messages = sharedPrefs.getMessagesQueue()
        val startChat = sharedPrefs.getStartChatMessage()
        if (messages.isNotEmpty()) {
            isQueueActive = true
            service.sendMessageToServer(messages[0])
        }
        else if (startChat != null) {
            isQueueActive = true
            service.sendStartChatMessageToServer(startChat)
        }
        else {
            isQueueActive = false
        }
    }


}