package com.example.online_chat_hde.models


import org.json.JSONArray
import org.json.JSONObject

import java.io.File
import java.security.MessageDigest
import java.util.UUID

// данные сообщения, для отправки на сервер
class VisitorMessage(
    var text: String,
    var files: List<VisitorFile> = listOf()
) {
    var uuid: String = UUID.randomUUID().toString()

    fun toJson() = JSONObject().apply {
        put("text", text)
        put("files", JSONArray(files.map { it.toJson() }))
    }

    companion object {
        fun fromJsonArray(json: JSONArray): List<VisitorMessage> {
            return (0 until json.length()).map {
                val item = json.getJSONObject(it)
                return@map fromJson(item)
            }
        }
        fun fromJson(json: JSONObject): VisitorMessage {
            return VisitorMessage(
                text = json.getString("text"),
                files = VisitorFile.fromJsonArray(json.getJSONArray("files"))
            )
        }
        fun toJsonArrayString(messages: List<VisitorMessage>): String {
            return """[${messages.joinToString(", ") { it.toJson().toString() }}]"""
        }
    }
}


class VisitorFile(
    var fileName: String,
    var tempFileName: String,
    var uid: Long,
) {
    fun toJson() = JSONObject().apply {
        put("fileName", fileName)
        put("tempFileName", tempFileName)
        put("uid", uid)
    }
    companion object {
        fun fromJson(json: JSONObject): VisitorFile {
            return VisitorFile(
                fileName = json.getString("fileName"),
                tempFileName = json.getString("tempFileName"),
                uid = json.getLong("uid"),
            )
        }
        fun fromJsonArray(json: JSONArray): List<VisitorFile> {
            return (0 until json.length()).map {
                val item = json.getJSONObject(it)
                fromJson(item)
            }
        }
    }
}