package com.example.online_chat_hde.models

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
    var userData: UserData = UserData(id = "")

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
        var widgetChat: WidgetChat
    ): InitWidgetData() {
        companion object {
            fun fromJson(json: JSONObject): Progress {
                return Progress(
                    widgetChat = WidgetChat.fromJson(json.getJSONObject("widgetChat"))
                ).apply {
                    val rateJson = json.opt("rate")
                    val buttons = json.opt("initialChatButtons")
                    showChat = json.getBoolean("showChat")
                    ticketForm = json.getBoolean("ticketForm")
                    userData = UserData.fromJson(json.getJSONObject("visitorData"))
                    rate = if (rateJson == JSONObject.NULL) null
                           else Rate.fromJson(rateJson as JSONObject)
                    initialChatButtons = if (buttons == JSONObject.NULL) null
                                         else ChatButton.fromJsonArray(buttons as JSONArray)
                }
            }
        }
    }

    class First(
        var widgetChat: Boolean,
    ): InitWidgetData() {
        companion object {
            fun fromJson(json: JSONObject): First {
                val buttons = if (json.get("initialChatButtons") == JSONObject.NULL) null
                              else ChatButton.fromJsonArray(json.getJSONArray("initialChatButtons"))
                return First(
                    widgetChat = false
                ).apply {
                    val rateJson = json.opt("rate")
                    showChat = json.getBoolean("showChat")
                    ticketForm = json.getBoolean("ticketForm")
                    userData = UserData.fromJson(json.getJSONObject("visitorData"))
                    rate = if (rateJson == JSONObject.NULL) null
                           else Rate.fromJson(rateJson as JSONObject)
                    initialChatButtons = buttons
                }
            }
        }
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

