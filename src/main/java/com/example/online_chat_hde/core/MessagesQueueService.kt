package com.example.online_chat_hde.core

import com.example.online_chat_hde.models.NewMessageResponse

internal class MessagesQueueService(
    private val service: ChatClient,
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
        println("SDK[startSendMessageQueue], ${service.isConnected()}, $isQueueFree")
        if (service.isConnected() && isQueueFree) {
            sendFirstMessageFromQueue()
        }
    }

    // Отправляем 1 сообщение из очереди
    private fun sendFirstMessageFromQueue() {
        println("SDK[sendFirstMessageFromQueue]")
        val messages = sharedPrefs.getMessagesQueue()
        val startChat = sharedPrefs.getStartChatMessage()
        if (messages.isNotEmpty()) {
            isQueueFree = false
            service.sendMessageToServer(messages[0])
        }
        else if (startChat != null) {
            println("SDK[sendFirstMessageFromQueue startChat]")
            isQueueFree = false
            service.sendStartChatMessageToServer(startChat)
        }
        else {
            println("SDK[sendFirstMessageFromQueue free queue]")
            isQueueFree = true
        }
    }

}




internal class StartChatQueueService(
    private val service: ChatClient,
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