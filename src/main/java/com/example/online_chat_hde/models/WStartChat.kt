package com.example.online_chat_hde.models

import com.example.online_chat_hde.core.ActionTypes
import org.json.JSONObject

class WStartChat(
    var action: String,
    var data: Message.User,
) {
    fun toWNewMessage(): WNewMessage {
        return WNewMessage(
            action = ActionTypes.NEW_MESSAGE,
            data = data
        )
    }
    companion object {
        fun fromJson(json: JSONObject): WStartChat {
            val data = json.getJSONObject("data")
            return WStartChat(
                action = json.getString("action"),
                data = Message.User.fromJson(data)
            )
        }
    }
}