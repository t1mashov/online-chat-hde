package com.example.online_chat_hde.core

import android.content.Context
import com.example.online_chat_hde.models.ChatButton
import com.example.online_chat_hde.models.Staff
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.UserData
import com.example.online_chat_hde.models.VisitorMessage
import org.json.JSONArray
import org.json.JSONObject

class SharedPrefs(private val context: Context) {

    fun saveUser(data: UserData?) {
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        if (data == null) {
            prefs.edit()
                .putString(StorageKeys.VISITOR_DATA, null)
                .apply()
        }
        else {
            val txt = data.toJsonString()
            prefs.edit()
                .putString(StorageKeys.VISITOR_DATA, txt)
                .apply()
        }
    }
    fun getUser(): UserData? {
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(StorageKeys.VISITOR_DATA, null)
        return if (jsonString != null) UserData.fromJson(JSONObject(jsonString))
               else null
    }


    fun setStartChatMessage(data: StartVisitorChatData?) {
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        if (data == null) {
            prefs.edit()
                .putString(StorageKeys.START_CHAT_DATA, null)
                .apply()
        }
        else {
            val dataTxt = data.toJson().toString()
            prefs.edit()
                .putString(StorageKeys.START_CHAT_DATA, dataTxt)
                .apply()
        }
    }
    fun getStartChatMessage(): StartVisitorChatData? {
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(
            StorageKeys.START_CHAT_DATA,
            null
        )
        return if (jsonString == null) null
               else StartVisitorChatData.fromJson(JSONObject(jsonString))
    }


    fun addMessageToQueue(message: VisitorMessage) {
        val queue = getMessagesQueue().toMutableList()
        queue.add(message)
        val messageTxt = VisitorMessage.toJsonArrayString(queue)
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(StorageKeys.MESSAGE_QUEUE, messageTxt)
            .apply()
    }
    private fun setMessagesQueue(messages: List<VisitorMessage>) {
        val messageTxt = VisitorMessage.toJsonArrayString(messages)
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(StorageKeys.MESSAGE_QUEUE, messageTxt)
            .commit()
    }
    fun getMessagesQueue(): List<VisitorMessage> {
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(StorageKeys.MESSAGE_QUEUE, "[]")
        return VisitorMessage.fromJsonArray(JSONArray(jsonString))
    }
    fun removeMessageByText(text: String) {
        val messages = getMessagesQueue().toMutableList()
        for (i in messages.size-1 downTo 0) {
            if (messages[i].text == text) {
                messages.removeAt(i)
                break
            }
        }
        setMessagesQueue(messages)
    }



    fun saveChatButtons(buttons: List<ChatButton>) {
        val buttonsTxt = ChatButton.toJsonArrayString(buttons)
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(StorageKeys.CHAT_BUTTONS, buttonsTxt)
            .apply()
    }
    fun getChatButtons(): List<ChatButton> {
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(StorageKeys.CHAT_BUTTONS, "[]")
        return ChatButton.fromJsonArray(JSONArray(jsonString))
    }

    fun saveStaff(staff: Staff) {
        val staffText = staff.toJsonString()
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(StorageKeys.STAFF_DATA, staffText)
            .apply()
    }
    fun getStaff(): Staff? {
        val prefs = context.getSharedPreferences(StorageKeys.STORAGE_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(StorageKeys.STAFF_DATA, null)
        return if (jsonString != null) Staff.fromJson(JSONObject(jsonString))
               else null
    }

}