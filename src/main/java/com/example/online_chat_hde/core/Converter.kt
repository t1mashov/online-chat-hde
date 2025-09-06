package com.example.online_chat_hde.core

import com.example.online_chat_hde.models.Message
import com.example.online_chat_hde.models.VisitorMessage

class Converter {
    companion object {


        fun visitorMessageToUserMessage(message: VisitorMessage) = Message.User().apply {
            uuid = message.uuid
            text = message.text
            visitor = true
//            files = message.files
        }



    }
}