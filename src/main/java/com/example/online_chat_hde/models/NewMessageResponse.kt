package com.example.online_chat_hde.models

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

// данные нового сообщения клиента ПОЛУЧЕННЫЕ с сервера сразу после отправки
class NewMessageResponse(
    var data: Message,
) {
    companion object {
        fun fromJson(json: JSONObject): NewMessageResponse {
            val data = json.getJSONObject("data")
            return NewMessageResponse(
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


