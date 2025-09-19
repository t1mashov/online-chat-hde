package com.example.online_chat_hde.models

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID


sealed class FileData {
    val uuid = UUID.randomUUID().toString()
    var name: String = ""
    var link: String = ""

    companion object {
        fun fromJsonArray(json: JSONArray): List<FileData> = (0 until json.length()).map {
            val item = json.getJSONObject(it)
            if (item.get("thumb") == JSONObject.NULL) {
                return@map Text.fromJson(item)
            }
            else {
                return@map Image.fromJson(item)
            }
        }
    }

    class Text: FileData() {
        companion object {
            fun fromJson(json: JSONObject): Text = Text().apply {
                name = json.getString("name")
                link = json.getString("link")
            }
        }
    }

    class Image(
        var thumb: String,
    ): FileData() {
        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("uuid", uuid)
                put("name", name)
                put("link", link)
                put("thumb", thumb)
            }
        }
        companion object {
            fun fromJson(json: JSONObject): Image {
                return Image(
                    thumb = json.getString("thumb"),
                ).apply {
                    name = json.getString("name")
                    link = json.getString("link")
                }
            }
        }
    }
}

