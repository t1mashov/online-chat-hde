package com.example.online_chat_hde.models

import com.example.online_chat_hde.core.Payload
import org.json.JSONObject

class UserData(
    var id: String,
    var name: String = "",
    var email: String = "",
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("email", email)
        }
    }
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
