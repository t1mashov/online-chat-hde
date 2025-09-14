package com.example.online_chat_hde.core

import android.content.Context
import com.example.online_chat_hde.models.NewMessageResponse

internal class MessagesQueueService(
    private val service: ChatService,
    private val sharedPrefs: SharedPrefs
) {

    private var isQueueFree = true

    fun checkNextQueueMessage(message: NewMessageResponse) {
        sharedPrefs.removeMessageByText(message.data.text)
        sendFirstMessageFromQueue()
    }

    fun disableQueue() {
        isQueueFree = true
    }

    // Запуск отправки сообщений из очереди
    fun startSendMessageQueue() {
        println("[startSendMessageQueue], ${service.isConnected()}, $isQueueFree")
        if (service.isConnected() && isQueueFree) {
            sendFirstMessageFromQueue()
        }
    }

    // Отправляем 1 сообщение из очереди
    private fun sendFirstMessageFromQueue() {
        println("[sendFirstMessageFromQueue]")
        val messages = sharedPrefs.getMessagesQueue()
        val startChat = sharedPrefs.getStartChatMessage()
        if (messages.isNotEmpty()) {
            isQueueFree = false
            service.sendMessageToServer(messages[0])
        }
        else if (startChat != null) {
            println("[sendFirstMessageFromQueue startChat]")
            isQueueFree = false
            service.sendStartChatMessageToServer(startChat)
        }
        else {
            println("[sendFirstMessageFromQueue free queue]")
            isQueueFree = true
        }
    }

}




internal class StartChatQueueService(
    private val service: ChatService,
    private val sharedPrefs: SharedPrefs
) {

    private var isQueueFree = true

    fun freeQueue() {
        isQueueFree = true
    }

    // Запуск отправки сообщений из очереди
    fun startSend() {
        println("[startSend], ${service.isConnected()}, $isQueueFree")
        if (service.isConnected() && isQueueFree) {
            sendFromQueue()
        }
    }

    // Отправляем 1 сообщение из очереди
    private fun sendFromQueue() {
        println("[sendFromQueue]")
        val startChat = sharedPrefs.getStartChatMessage()
        if (startChat != null) {
            println("[sendFromQueue startChat]")
            isQueueFree = false
            service.sendStartChatMessageToServer(startChat)
        }
    }


}