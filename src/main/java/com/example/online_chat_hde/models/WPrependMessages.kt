package com.example.online_chat_hde.models

import org.json.JSONObject

class WPrependMessages (
    val action: String,
    val data: Messages?
) {
    companion object {
        fun fromJson(json: JSONObject): WPrependMessages = WPrependMessages(
            action = json.getString("action"),
            data = if (json.opt("data") == false) null
                   else Messages.fromJson(json.getJSONObject("data"))
        )
    }
}

class Messages (
    val messages: List<Message>
) {
    companion object {
        fun fromJson(json: JSONObject): Messages = Messages(
            messages = Message.fromJsonArray(json.getJSONArray("messages"))
        )
    }
}