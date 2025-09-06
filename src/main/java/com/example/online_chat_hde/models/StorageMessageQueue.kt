package com.example.online_chat_hde.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_queue",
    indices = [Index("id")]
)
data class MessageQueueData (
    @PrimaryKey(autoGenerate = true) val id: Int,

)