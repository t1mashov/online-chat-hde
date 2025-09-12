package com.example.online_chat_hde.models

import com.example.online_chat_hde.core.ButtonTypes
import com.example.online_chat_hde.core.Payload
import org.json.JSONArray
import org.json.JSONObject



class InitResponse(
    var action: String,
    var data: InitWidgetData,
) {
    companion object {
        fun fromJson(json: JSONObject): InitResponse = InitResponse(
            action = json.getString("action"),
            data = InitWidgetData.fromJson(json.getJSONObject("data"))
        )
    }
}


sealed class InitWidgetData {

    var showChat: Boolean = true
    var ticketForm: Boolean = false
    var rate: Rate? = null
    var initialChatButtons: List<ChatButton>? = null

    companion object {
        fun fromJson(json: JSONObject): InitWidgetData {
            val widgetChat = json.opt("widgetChat")
            return if (widgetChat is Boolean) {
                First.fromJson(json)
            } else {
                Progress.fromJson(json)
            }
        }
    }

    class Progress(
        var visitorData: UserData,
        var widgetChat: WidgetChat
    ): InitWidgetData() {
        companion object {
            fun fromJson(json: JSONObject): Progress {
                return Progress(
                    visitorData = UserData.fromJson(json.getJSONObject("visitorData")),
                    widgetChat = WidgetChat.fromJson(json.getJSONObject("widgetChat"))
                ).apply {
                    val rateJson = json.opt("rate")
                    val buttons = json.opt("initialChatButtons")
                    showChat = json.getBoolean("showChat")
                    ticketForm = json.getBoolean("ticketForm")
                    rate = if (rateJson == JSONObject.NULL) null
                           else Rate.fromJson(rateJson as JSONObject)
                    initialChatButtons = if (buttons == JSONObject.NULL) null
                                         else ChatButton.fromJsonArray(buttons as JSONArray)
                }
            }
        }
    }

    class First(
        var userData: UserData,
        var widgetChat: Boolean,
    ): InitWidgetData() {
        companion object {
            fun fromJson(json: JSONObject): First {
                val buttons = if (json.get("initialChatButtons") == JSONObject.NULL) null
                              else ChatButton.fromJsonArray(json.getJSONArray("initialChatButtons"))
                return First(
                    userData = UserData.fromJson(json.getJSONObject("visitorData")),
                    widgetChat = false
                ).apply {
                    val rateJson = json.opt("rate")
                    showChat = json.getBoolean("showChat")
                    ticketForm = json.getBoolean("ticketForm")
                    rate = if (rateJson == JSONObject.NULL) null
                           else Rate.fromJson(rateJson as JSONObject)
                    initialChatButtons = buttons
                }
            }
        }
    }
}


class UserData(
    var id: String,
    var name: String = "",
    var email: String = "",
) {
    fun toJsonString(): String = """{"id":"$id", "name":"$name", "email":"$email"}"""
    companion object {
        fun fromJson(json: JSONObject): UserData = UserData(
            id = json.getString("id"),
            name = json.getString("name"),
            email = json.getString("email")
        )
        fun fromPayloadAuthUser(payload: Payload.Auth) = UserData(
            id = payload.visitorId,
            name = payload.visitorName,
            email = payload.visitorEmail
        )
    }
}

class Rate(
    var maxScore: Int,
    var template: String,
    var customRate: Boolean,
) {
    companion object {
        fun fromJson(json: JSONObject): Rate = Rate(
            maxScore = json.getInt("maxScore"),
            template = json.getString("template"),
            customRate = json.getBoolean("customRate")
        )
    }
}

class ChatButton(
    var type: String = ButtonTypes.TEXT,
    var name: String? = null,
    var value: String? = null,
    var text: String,
    var hideButtons: Boolean = false,
) {

    fun toJsonString(): String = """{"type":"$type", "name":"$name", "value":"$value", "text":"$text", "hideButtons":$hideButtons}"""

    companion object {
        fun fromJson(json: JSONObject): ChatButton{
            return ChatButton(
                type = if (json.has("type")) json.getString("type") else ButtonTypes.TEXT,
                name = if (json.has("name")) json.getString("name") else null,
                value = if (json.has("value")) json.getString("value") else null,
                text = json.getString("text"),
                hideButtons = if (json.has("hideButtons")) json.getBoolean("hideButtons") else false,
            )
        }

        fun fromJsonArray(json: JSONArray): List<ChatButton> = (0 until json.length()).map {
            val item = json.getJSONObject(it)
            return@map fromJson(item)
        }

        fun toJsonArrayString(buttons: List<ChatButton>): String {
            return """[${buttons.map { it.toJsonString() }.joinToString(", ")}]"""
        }
    }
}