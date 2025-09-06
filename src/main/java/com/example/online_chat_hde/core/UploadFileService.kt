package com.example.online_chat_hde.core

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.online_chat_hde.models.VisitorFile
import com.example.online_chat_hde.models.VisitorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal class UploadFileService(
    val service: ChatService,
    val context: Context
) {

    private val _errorFlow = MutableSharedFlow<String>()
    internal val errorFlow: SharedFlow<String> = _errorFlow

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun getFileFromUri(uri: Uri): File? {
        val fileName = getFileName(context, uri) ?: return null
        val tempFile = File(context.cacheDir, fileName)
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return tempFile
    }


    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }


    fun uploadFile(fileUri: Uri) {

        val file = getFileFromUri(fileUri) ?: return

        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(service.serverOptions.uploadUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                scope.launch {
                    _errorFlow.emit("undefined error")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val serverResponse = JSONObject(response.body!!.string())
                    if (serverResponse.has("error")) {
                        scope.launch {
                            _errorFlow.emit(serverResponse.getString("error"))
                        }
                        return
                    }
                    val message = VisitorMessage(
                        text = "",
                        files = listOf(
                            VisitorFile(
                                fileName = file.name,
                                tempFileName = serverResponse.getString("fileName"),
                                uid = System.currentTimeMillis()
                            )
                        )
                    )
                    service.sendMessage(message)
                }
            }
        })
    }

}