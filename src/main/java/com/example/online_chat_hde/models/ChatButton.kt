package com.example.online_chat_hde.models

import com.example.online_chat_hde.core.ButtonTypes
import org.json.JSONArray
import org.json.JSONObject

class ChatButton(
    var text: String,
    var type: String = ButtonTypes.TEXT,
    var value: String? = null,
    var hideButtons: Boolean = false,
) {

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("type", type)
            put("value", value)
            put("text", text)
            put("hideButtons", hideButtons)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): ChatButton{
            return ChatButton(
                type = if (json.has("type")) json.getString("type") else ButtonTypes.TEXT,
                value = if (json.has("value")) json.getString("value") else null,
                text = json.getString("text"),
                hideButtons = if (json.has("hideButtons")) json.getBoolean("hideButtons") else false,
            )
        }

        fun fromJsonArray(json: JSONArray): List<ChatButton> = (0 until json.length()).map {
            val item = json.getJSONObject(it)
            return@map fromJson(item)
        }

        fun toJsonArray(buttons: List<ChatButton>): JSONArray {
            return JSONArray(buttons.map { it.toJson() })
        }
    }
}
