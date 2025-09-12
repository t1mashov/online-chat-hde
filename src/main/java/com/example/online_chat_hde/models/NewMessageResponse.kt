package com.example.online_chat_hde.models

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

// данные нового сообщения клиента ПОЛУЧЕННЫЕ с сервера сразу после отправки
class NewMessageResponse(
    var action: String,
    var data: Message,
) {
    companion object {
        fun fromJson(json: JSONObject): NewMessageResponse {
            val data = json.getJSONObject("data")
            return NewMessageResponse(
                action = json.getString("action"),
                data = if (data.getBoolean("visitor")) Message.User.fromJson(data)
                       else Message.Server.fromJson(data)
            )
        }

    }
}



// данные истории переписки, полученные от сервера (при загрузке страницы)
sealed class Message {
    var uuid = UUID.randomUUID().toString()
    var isPrepend: Boolean = false

    var text: String = ""
    var time: String = ""
    var visitor: Boolean = true
    var dates: MessageDate? = null
    var files: List<FileData>? = null
    var chatButtons: List<ChatButton>? = null
    var isVirtual: Boolean = false

    companion object {
        fun fromJsonArray(json: JSONArray): List<Message> = (0 until json.length()).map {
            val item = json.getJSONObject(it)
            if (item.getBoolean("visitor")) {
                // UserMessage
                return@map User.fromJson(item)
            }
            else {
                // ServerMessage
                return@map Server.fromJson(item)
            }
        }
    }

    class User: Message() {
        companion object {
            fun fromJson(json: JSONObject): User {
                val filesJson = json.opt("files")

                return User().apply {
                    text = json.getString("text")
                    time = json.getString("time")
                    visitor = json.getBoolean("visitor")
                    dates = if (json.has("dates")) MessageDate.fromJson(json.getJSONObject("dates"))
                            else null
                    files = if (filesJson == JSONObject.NULL) null
                            else FileData.fromJsonArray(filesJson as JSONArray)
                }
            }
        }
    }

    class Server(
        var name: String,
    ): Message() {
        companion object {
            fun fromJson(json: JSONObject): Server {
                val filesJson = json.opt("files")
                return Server(
                    name = json.getString("name")
                ).apply {
                    text = json.getString("text")
                    time = json.getString("time")
                    visitor = json.getBoolean("visitor")
                    dates = if (json.has("dates")) MessageDate.fromJson(json.getJSONObject("dates"))
                            else null
                    files = if (filesJson == JSONObject.NULL) null
                            else FileData.fromJsonArray(filesJson as JSONArray)
                    chatButtons = if (json.has("chatButtons")) ChatButton.fromJsonArray(json.getJSONArray("chatButtons"))
                                  else listOf()
                }
            }
        }
    }

}

class MessageDate(
    var createdAtDate: String?,
    var createdAtName: String,
) {
    companion object {
        fun fromJson(json: JSONObject): MessageDate = MessageDate(
            createdAtDate = if (json.has("createdAtDate")) json.getString("createdAtDate")
                            else null,
            createdAtName = json.getString("createdAtName")
        )
    }
}




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

    class Text(
        var thumb: String? = null,
        var preview: Boolean? = null,
    ): FileData() {
        companion object {
            fun fromJson(json: JSONObject): Text = Text(
                thumb = json.optString("thumb").takeIf { it.isNotEmpty() },
                preview = if (json.has("preview")) json.getBoolean("preview") else null
            ).apply {
                name = json.getString("name")
                link = json.getString("link")
            }
        }
    }

    class Image(
        var thumb: String,
        var preview: String?,
    ): FileData() {
        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("uuid", uuid)
                put("name", name)
                put("link", link)
                put("thumb", thumb)
                put("preview", preview)
            }
        }
        companion object {
            fun fromJson(json: JSONObject): Image = Image(
                thumb = json.getString("thumb"),
                preview = if (json.has("preview")) json.getString("preview")
                          else null
            ).apply {
                name = json.getString("name")
                link = json.getString("link")
            }
        }
    }
}

