package com.example.online_chat_hde.models


import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID


class WidgetChat(
    var staff: Staff?,
    var messages: List<Message>,
    var totalTickets: Int,
    var chatOpened: Boolean,
) {
    companion object {
        fun fromJson(json: JSONObject): WidgetChat {
            val staff = json.opt("staff")
            return WidgetChat(
                staff = if (staff == JSONObject.NULL) null
                        else Staff.fromJson(staff as JSONObject),
                messages = Message.fromJsonArray(json.getJSONArray("messages")),
                totalTickets = json.getInt("totalTickets"),
                chatOpened = json.getBoolean("chatOpened")
            )
        }
    }
}


class Staff (
    var name: String,
    var image: String?,
) {
    fun toJsonString(): String {
        return """{"name":"$name", "image":${if (image == null) "null" else "\"$image\""}}"""
    }
    companion object {
        fun fromJson(json: JSONObject): Staff {
            val res = Staff(
                name = json.getString("name"),
                image = if (json.opt("image") == JSONObject.NULL) null
                        else json.getString("image")
            )
            return res
        }
    }
}
