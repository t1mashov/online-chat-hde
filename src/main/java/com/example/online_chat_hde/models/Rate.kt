package com.example.online_chat_hde.models

import com.example.online_chat_hde.core.ActionTypes
import org.json.JSONObject


class RateResponse(
    val action: String,
    val data: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): RateResponse {
            return RateResponse(
                action = ActionTypes.RATE_SUCCESS,
                data = json.getBoolean("data")
            )
        }
    }
}


class Rate(
    var maxScore: Int,
    var template: String,
    var customRate: Boolean,
) {
    companion object {
        fun fromJson(json: JSONObject): Rate = Rate(
            maxScore = json.getInt("maxScore"),
            template = json.getString("template"),
            customRate = json.getBoolean("customRate")
        )
    }
}


data class UserRate(
    val rate: Int,
    val comment: String
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("rate", rate)
            put("comment", comment)
        }
    }
}




sealed class RateFormat {
    data object None: RateFormat()

    data object Smiles2: RateFormat()
    data object Thumbs: RateFormat()

    data object Smiles3: RateFormat()

    data object Smiles: RateFormat()
    data object Hearts: RateFormat()
    data object Stars: RateFormat()
    data object Numbers: RateFormat()
}

