package com.example.online_chat_hde.models

import com.example.online_chat_hde.core.ActionTypes
import org.json.JSONObject

class SetStaffResponse(
    val action: String,
    val data: SetStaffData
) {
    companion object {
        fun fromJson(json: JSONObject): SetStaffResponse {
            return SetStaffResponse(
                action = ActionTypes.SET_STAFF,
                data = SetStaffData.fromJson(json.getJSONObject("data"))
            )
        }
        fun fromStaff(staff: Staff): SetStaffResponse {
            return SetStaffResponse(
                action = ActionTypes.SET_STAFF,
                data = SetStaffData(
                    staff = staff
                )
            )
        }
    }
}

class SetStaffData(
    val staff: Staff
) {
    companion object {
        fun fromJson(json: JSONObject): SetStaffData {
            return SetStaffData(
                Staff.fromJson(json.getJSONObject("staff"))
            )
        }
    }
}