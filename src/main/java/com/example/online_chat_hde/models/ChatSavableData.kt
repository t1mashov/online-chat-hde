package com.example.online_chat_hde.models

import org.json.JSONObject

data class ChatSavableData(
    val userData: UserData?,
    val staffData: Staff?,
    val chatButtons: List<ChatButton>,
    val messagesQueue: List<VisitorMessage>,
    val startChatDatta: StartVisitorChatData?
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("userData", userData?.toJson())
            put("staffData", staffData?.toJson())
            put("chatButtons", ChatButton.toJsonArray(chatButtons))
            put("messagesQueue", VisitorMessage.toJsonArray(messagesQueue))
            put("startChatDatta", startChatDatta?.toJson())
        }
    }
    companion object {
        fun fromJson(json: JSONObject): ChatSavableData {
            println("[ChatSavableData json] >>> $json")
            return ChatSavableData(
                userData = if (!json.has("userData") || json.opt("userData") == JSONObject.NULL) null
                           else UserData.fromJson(json.getJSONObject("userData")),
                staffData = if (!json.has("staffData") || json.opt("staffData") == JSONObject.NULL) null
                            else Staff.fromJson(json.getJSONObject("staffData")),
                chatButtons = ChatButton.fromJsonArray(json.getJSONArray("chatButtons")),
                messagesQueue = VisitorMessage.fromJsonArray(json.getJSONArray("messagesQueue")),
                startChatDatta = if (!json.has("startChatDatta") || json.opt("startChatDatta") == JSONObject.NULL) null
                                 else StartVisitorChatData.fromJson(json.getJSONObject("startChatDatta"))
            )
        }
    }
}