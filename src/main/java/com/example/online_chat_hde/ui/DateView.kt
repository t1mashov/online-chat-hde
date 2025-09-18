package com.example.online_chat_hde.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.online_chat_hde.models.MessageDate

@Composable
internal fun DateView(
    date: MessageDate,
    uiConfig: ChatUIConfig
) {

    val dateTxt = "${if (date.createdAtDate != null) date.createdAtDate+" " else ""}${uiConfig.texts.months.getOrDefault(date.createdAtName, date.createdAtName)}"

    Text(
        text = dateTxt,
        fontSize = uiConfig.dimensions.messageFontSize,
        color = uiConfig.colors.dateText,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(uiConfig.dimensions.innerIndent)
    )
}