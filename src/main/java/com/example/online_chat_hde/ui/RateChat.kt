package com.example.online_chat_hde.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.R
import com.example.online_chat_hde.core.RateTemplates
import com.example.online_chat_hde.models.ChatButton


interface RateChatTextField: ChatUIScope {
    val text: String
    val indicator: String
    val onTextChange: (String) -> Unit
}


interface RateChatScope: ChatUIScope {
    val info: String
    val maxRate: Int
    val onSubmit: (Int, String) -> Unit
}

interface RateButtonsScope: RateChatScope {
    val rate: Int
    val onRateChange: (Int) -> Unit
}

interface RateSubmitScope: ChatUIScope {
    val rate: Int
    val onSubmit: () -> Unit
    val comment: String
}


interface RateChatFrame {
    val title: @Composable () -> Unit
    val rateButtons: @Composable () -> Unit
    val leaveCommentText: @Composable () -> Unit
    val commentField: @Composable () -> Unit
    val submitButton: @Composable () -> Unit
}

@Composable
fun RateChatScope.RateChat(
    title: @Composable ChatUIScope.() -> Unit = {
        Text(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            text = uiConfig.texts.rateChat,
            fontSize = uiConfig.dimensions.messageFontSize,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    },
    rateButtons: @Composable RateButtonsScope.() -> Unit = {
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            RateButtons()
        }
    },
    leaveCommentText: @Composable ChatUIScope.() -> Unit = {
        Text(
            text = uiConfig.texts.leaveComment,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    },
    commentField: @Composable RateChatTextField.() -> Unit = {
        Column(Modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.End) {
            Box(modifier = Modifier.height(150.dp)) {
                RateCommentTextField(text, onTextChange)
            }
            Text(indicator)
        }
    },
    submitButton: @Composable RateSubmitScope.(Int, String) -> Unit = {rate, comment ->
        ChatMessageButton(ChatButton(text = uiConfig.texts.leaveFeedback)) {
            onSubmit()
        }
    },
    frame: @Composable RateChatScope.(RateChatFrame) -> Unit = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(uiConfig.colors.background)
                .padding(16.dp)
        ) {
            Column {
                it.title()
                it.rateButtons()
                it.leaveCommentText()
                it.commentField()
                it.submitButton()
            }
        }
    }
) {

    val rate = remember { mutableIntStateOf(0) }
    val onRateChange: (Int) -> Unit = {
        rate.intValue = it
    }
    val text = remember { mutableStateOf("") }
    val textIndicator = remember { mutableStateOf("0/3000") }
    val onTextChange: (String) -> Unit = {
        if (it.length > 3000) {
            text.value = it.substring(0, 3000)
        }
        else {
            text.value = it
        }
        textIndicator.value = "${text.value.length}/3000"
    }

    val submitScope = remember(text.value, rate.intValue) {
        object : RateSubmitScope, ChatUIScope by this@RateChat {
            override val rate: Int = rate.intValue
            override val comment: String = text.value
            override val onSubmit: () -> Unit = { this@RateChat.onSubmit(rate.intValue, text.value) }
        }
    }

    val buttonsScope = remember(rate.intValue) {
        object : RateButtonsScope, RateChatScope by this@RateChat {
            override val rate: Int = rate.intValue
            override val onRateChange: (Int) -> Unit = onRateChange
        }
    }

    val textScope = remember(text.value) {
        object : RateChatTextField, ChatUIScope by this@RateChat {
            override val text: String = text.value
            override val indicator: String = textIndicator.value
            override val onTextChange: (String) -> Unit = onTextChange
        }
    }

    val item = remember(textScope, buttonsScope, submitScope) {
        object : RateChatFrame {
            override val title: @Composable () -> Unit = { with(this@RateChat){ title() } }
            override val rateButtons: @Composable () -> Unit = { with(buttonsScope){ rateButtons() } }
            override val leaveCommentText: @Composable () -> Unit = { with(this@RateChat){ leaveCommentText() } }
            override val commentField: @Composable () -> Unit = { with(textScope){ commentField() } }
            override val submitButton: @Composable () -> Unit = { with(submitScope){ submitButton(rate.intValue, text.value) } }
        }
    }

    frame(item)
}



@Composable
fun RateButtonsScope.RateButtons() {

    when (info) {
        RateTemplates.SMILES2, RateTemplates.THUMBS -> {
            PositiveNegativeRate(info, rate, onRateChange)
        }
        RateTemplates.SMILES3 -> {
            PositiveNeutralNegativeRate(rate, onRateChange)
        }
        RateTemplates.SMILES, RateTemplates.HEARTS, RateTemplates.NUMBERS, RateTemplates.STARS -> {
            PointRate(info, rate, maxRate, onRateChange)
        }
    }
}


@Composable
fun ChatUIScope.PositiveNegativeRate(
    info: String,
    value: Int,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        for (item in listOf(1, 2)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color =
                    if (value == item)
                        if (item == 1) Color.Red
                        else Color.Green
                    else Color.Gray,
                modifier = Modifier
                    .padding(4.dp)
                    .size(42.dp)
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onChange(item)
                    }
            ) {
                val vecRes = when (info) {
                    RateTemplates.THUMBS -> {
                        when (item) {
                            2 -> R.drawable.like
                            else -> R.drawable.dislike
                        }
                    }
                    else -> {
                        when (item) {
                            2 -> R.drawable.smile_good
                            else -> R.drawable.smile_bad
                        }
                    }
                }
                Image(
                    imageVector = ImageVector.vectorResource(vecRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        if (value == item) Color.White
                        else Color.Black
                    )
                )
            }
        }

    }
}



@Composable
fun PositiveNeutralNegativeRate(
    value: Int,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        for (item in listOf(1, 2, 3)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color =
                if (value == item)
                    when (item) {
                        1 -> Color.Red
                        2 -> Color.Green
                        3 -> Color.Gray
                        else -> Color.Gray
                    }
                else Color.LightGray,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onChange(item)
                    }
            ) {
                val vecRes = when (item) {
                    3 -> R.drawable.smile_good
                    2 -> R.drawable.smile_neutral
                    else -> R.drawable.smile_bad
                }

                Image(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(42.dp),
                    imageVector = ImageVector.vectorResource(vecRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        if (value == item) Color.White
                        else Color.Black
                    )
                )
            }
        }

    }
}


@Composable
fun ChatUIScope.PointRate(
    info: String,
    value: Int,
    maxScore: Int,
    onChange: (Int) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        for (i in (1..maxScore)) {
            when (info) {
                RateTemplates.NUMBERS -> {
                    Box(modifier = Modifier
                        .padding(4.dp)
                        .weight(1f)
                        .clickable {
                            onChange(i)
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (value == i) uiConfig.colors.userMessageBackground
                                    else Color.LightGray
                                )
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "$i",
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    val vecRes: Int
                    val color: Color

                    when (info) {
                        RateTemplates.HEARTS -> {
                            vecRes = R.drawable.heart
                            color = if (i <= value) Color(0xFFFF0000) else Color(0xFF999999)
                        }
                        RateTemplates.SMILES -> {
                            vecRes = R.drawable.smile_good
                            color = if (i <= value) Color(0xFF08FF00) else Color(0xFF999999)
                        }
                        else -> {
                            vecRes = R.drawable.star
                            color = if (i <= value) Color(0xFFFFBF00) else Color(0xFF999999)
                        }
                    }


                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onChange(i)
                            },
                        imageVector = ImageVector.vectorResource(vecRes),
                        colorFilter = ColorFilter.tint(color),
                        contentScale = ContentScale.Fit,
                        contentDescription = null
                    )
                }
            }
        }

    }
}




@Composable
fun ChatUIScope.RateCommentTextField(
    text: String,
    onChange: (String) -> Unit,

    textColor: Color = uiConfig.colors.ticketSubText,
    shape: RoundedCornerShape = uiConfig.dimensions.ticketFieldsCorners,
    borderWidth: Dp = 1.dp,
    borderDefaultColor: Color = uiConfig.colors.ticketFieldDisabled,
    backgroundDefaultColor: Color = uiConfig.colors.inputFieldBackground,
    borderFocusedColor: Color = uiConfig.colors.userMessageBackground,
    fontSize: TextUnit = uiConfig.dimensions.messageFontSize,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
) {

    val interaction = remember { MutableInteractionSource() }
    val isFocused by interaction.collectIsFocusedAsState()

    // Цвета обводки как в исходном OutlinedTextField
    val borderColor =
        if (isFocused) borderFocusedColor
        else borderDefaultColor

    val modifier = Modifier.fillMaxHeight()

    BasicTextField(
        value = text,
        onValueChange = onChange,
        singleLine = false,
        textStyle = LocalTextStyle.current.copy(
            fontSize = fontSize,
            color = textColor
        ),
        cursorBrush = SolidColor(borderDefaultColor),
        interactionSource = interaction,
        maxLines = Int.MAX_VALUE,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(BorderStroke(borderWidth, borderColor), shape)
            .background(backgroundDefaultColor)
            .padding(contentPadding),
        decorationBox = { inner ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    inner()
                }
            }
        }
    )

}



@Preview
@Composable
fun RateChatPreview() {
    val scope = object : RateChatScope {
        override val info: String = RateTemplates.HEARTS
        override val maxRate: Int = 5
        override val onSubmit: (Int, String) -> Unit = {_, _ ->}
        override val uiConfig: ChatUIConfig = ChatUIConfigDefault
    }

    with (scope) {
        RateChat()
    }
}