package com.example.online_chat_hde.models

import org.json.JSONObject

data class StartVisitorChatData(
    val name: String,
    val email: String,
    val message: String,
    val policy: Boolean
) {
    fun toJsonString(): String {
        return """{"name":"$name", "email":"$email", "message":"$message", "policy":$policy}"""
    }
    companion object {
        fun fromJson(json: JSONObject): StartVisitorChatData {
            return StartVisitorChatData(
                name = json.getString("name"),
                email = json.getString("email"),
                message = json.getString("message"),
                policy = json.getBoolean("policy")
            )
        }
    }
}