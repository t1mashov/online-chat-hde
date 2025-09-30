package com.example.online_chat_hde.ui.message

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.models.MessageDate



@Composable
fun ChatUIScope.StartDate(date: String) {
    Text(
        text = date,
        fontSize = uiConfig.dimensions.messageFontSize,
        color = uiConfig.colors.dateText,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(uiConfig.dimensions.innerIndent)
    )
}