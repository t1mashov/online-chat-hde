package com.example.online_chat_hde.models

import org.json.JSONObject

data class StartVisitorChatData(
    val name: String,
    val email: String,
    val message: String,
    val policy: Boolean
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("email", email)
            put("message", message)
            put("policy", policy)
        }
    }
    companion object {
        fun fromJson(json: JSONObject): StartVisitorChatData {
            println("[StartVisitorChatData json] >>> $json")
            return StartVisitorChatData(
                name = json.getString("name"),
                email = json.getString("email"),
                message = json.getString("message"),
                policy = json.getBoolean("policy")
            )
        }
    }
}