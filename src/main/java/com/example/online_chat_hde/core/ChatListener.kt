package com.example.online_chat_hde.core

import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.FileData
import com.example.online_chat_hde.models.VisitorData

interface ChatListener {
    fun onSendMessage() {}
    fun onConnect() {}
    fun onServerResponse(response: Array<Any>) {}
    fun onDisconnect() {}
    fun onConnectError() {}
    fun onDetectVisitor(visitorData: VisitorData) {}
}