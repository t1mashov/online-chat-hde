package com.example.online_chat_hde.core

import org.json.JSONObject
import java.util.Base64

class ServerOptions(

    /** URL сокета в формате wss://domain.com */
    val socketUrl: String,

    /** исходный URL системы */
    val originUrl: String,

    /** URL для загрузки файлов */
    val uploadUrl: String
) {
    var EIO: Int = 4
    var type: String = "web"
    var transport: String = "websocket"
}


sealed class Payload {

    var visitMeta: VisitMeta = VisitMeta()
    var iframe: Boolean = true
    var widgetIsVisible: Boolean = true

    open fun toJson(): JSONObject = JSONObject()
    open fun encode(): String = ""

    class Auth(
        var visitorId: String,
        var visitorName: String,
        var visitorEmail: String,
    ) : Payload() {
        override fun toJson(): JSONObject {
            return JSONObject().apply {
                put("visitMeta", visitMeta.toJson())
                put("visitorId", visitorId)
                put("visitorName", visitorName)
                put("visitorEmail", visitorEmail)
                put("iframe", iframe)
                put("widgetIsVisible", widgetIsVisible)
            }
        }
        override fun encode(): String {
            val json = this.toJson().toString()
            return Base64.getEncoder().encodeToString(json.toByteArray())
        }
    }


    class NewUser : Payload() {
        override fun toJson(): JSONObject {
            return JSONObject().apply {
                put("visitMeta", visitMeta.toJson())
                put("visitorId", JSONObject.NULL)
                put("iframe", iframe)
                put("widgetIsVisible", widgetIsVisible)
            }
        }
        override fun encode(): String {
            val json = toJson().toString()
            return Base64.getEncoder().encodeToString(json.toByteArray())
        }
    }
}


class VisitMeta (
    var page: Page = Page()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("page", page.toJson())
    }
}


class Page (
    var title: String = "",
    var domain: String = "",
    var port: String = "",
    var protocol: String = "",
    var path: String = "/",
    var referrer: String = "",
    var search: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("title", title)
        put("domain", domain)
        put("port", port)
        put("protocol", protocol)
        put("path", path)
        put("referrer", referrer)
        put("search", search)

    }
}