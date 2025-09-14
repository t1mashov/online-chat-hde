package com.example.online_chat_hde.models

import com.example.online_chat_hde.core.ActionTypes
import org.json.JSONObject

class StartChatResponse(
    var data: Message.User,
) {
    fun toNewMessageResponse(): NewMessageResponse {
        return NewMessageResponse(
            data = data
        )
    }
    companion object {
        fun fromJson(json: JSONObject): StartChatResponse {
            val data = json.getJSONObject("data")
            return StartChatResponse(
                data = Message.User.fromJson(data)
            )
        }
    }
}