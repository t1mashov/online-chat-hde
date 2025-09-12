package com.example.online_chat_hde.models

import com.example.online_chat_hde.core.ActionTypes
import org.json.JSONObject

class StartChatResponse(
    var action: String,
    var data: Message.User,
) {
    fun toNewMessageResponse(): NewMessageResponse {
        return NewMessageResponse(
            action = ActionTypes.NEW_MESSAGE,
            data = data
        )
    }
    companion object {
        fun fromJson(json: JSONObject): StartChatResponse {
            val data = json.getJSONObject("data")
            return StartChatResponse(
                action = json.getString("action"),
                data = Message.User.fromJson(data)
            )
        }
    }
}