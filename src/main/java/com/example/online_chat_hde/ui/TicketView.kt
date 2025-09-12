package com.example.online_chat_hde.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.online_chat_hde.core.ChatOptions
import com.example.online_chat_hde.core.ChatService
import com.example.online_chat_hde.core.ChatViewModel
import com.example.online_chat_hde.core.ServerOptions
import com.example.online_chat_hde.core.TicketOptions
import com.example.online_chat_hde.core.TicketStatus
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.UserData

@Composable
fun TicketView(
    viewModel: ChatViewModel,
    uiConfig: ChatUIConfig,
    options: TicketOptions,
    userData: UserData?,
    ticketStatus: TicketStatus = TicketStatus.DISABLED,
    onClickClose: () -> Unit = {},
    onSubmit: (StartVisitorChatData) -> Unit = {}
) {

    val emptyNameError = remember { mutableStateOf(false) }
    val emptyEmailError = remember { mutableStateOf(false) }
    val invalidEmailError = remember { mutableStateOf(false) }
    val emptyContentError = remember { mutableStateOf(false) }
    val unacceptedConsentError = remember { mutableStateOf(false) }

    val nameText = remember { mutableStateOf(userData?.name ?: "") }
    val emailText = remember { mutableStateOf(userData?.email ?: "") }
    val contentText = remember { mutableStateOf("") }

    val isConsented = remember { mutableStateOf(false) }

    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(uiConfig.colors.background)
    ) {

        // Верхняя панель
        ChatTopPanel(viewModel, uiConfig, onClickClose)

        if (viewModel.isGlobalLoading.value) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(uiConfig.colors.loadingBackground)
            ) {
                MessageLoading(
                    uiConfig.colors.userMessageBackground,
                    uiConfig.dimensions.loadingLogoSize
                )
            }
        }
        else {
            if (ticketStatus ==TicketStatus.WAIT_FOR_REPLY) {
                // Спасибо за сообщение, ждите ответа
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiConfig.texts.waitForReply,
                        fontSize = uiConfig.dimensions.messageFontSize,
                        color = uiConfig.colors.ticketSubText,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else {
                // Тикет
                Box(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column {
                        if (ticketStatus == TicketStatus.STAFF_OFFLINE) {
                            Text(
                                text = uiConfig.texts.ticketOffline,
                                fontSize = uiConfig.dimensions.ticketSubText,
                                color = uiConfig.colors.ticketSubText
                            )
                            Spacer(modifier = Modifier.height(uiConfig.dimensions.ticketFieldsIndent))
                        }

                        if (options.showNameField) {
                            TicketTextField(
                                value = nameText,
                                placeholder = uiConfig.texts.ticketNamePlaceholder,
                                icon = uiConfig.media.ticketNameLogo,
                                uiConfig = uiConfig
                            )
                            Text(
                                text = if (emptyNameError.value) uiConfig.texts.emptyNameError else "",
                                color = uiConfig.colors.errorPrimary,
                                fontSize = uiConfig.dimensions.ticketSubText,
                                modifier = Modifier.padding(bottom = 4.dp, top = 2.dp)
                            )

                        }

                        if (options.showEmailField) {
                            TicketTextField(
                                value = emailText,
                                placeholder = uiConfig.texts.ticketEmailPlaceholder,
                                icon = uiConfig.media.ticketEmailLogo,
                                uiConfig = uiConfig
                            )
                            Text(
                                text = if (emptyEmailError.value) uiConfig.texts.emptyEmailError else if (invalidEmailError.value) uiConfig.texts.invalidEmailError else "",
                                color = uiConfig.colors.errorPrimary,
                                fontSize = uiConfig.dimensions.ticketSubText,
                                modifier = Modifier.padding(bottom = 4.dp, top = 2.dp)
                            )
                        }

                        TicketTextField(
                            value = contentText,
                            placeholder = uiConfig.texts.ticketFormPlaceholder,
                            icon = null,
                            uiConfig = uiConfig,
                            verticalAlignment = Alignment.Top,
                            maxLines = Int.MAX_VALUE,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        )
                        Text(
                            text = if (emptyContentError.value) uiConfig.texts.emptyContentError else "",
                            color = uiConfig.colors.errorPrimary,
                            fontSize = uiConfig.dimensions.ticketSubText,
                            modifier = Modifier.padding(bottom = 4.dp, top = 2.dp)
                        )

                        if (!options.consentLink.isNullOrEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TicketCheckbox(
                                    checked = isConsented.value,
                                    onCheckedChange = {isConsented.value = it},
                                    uiConfig = uiConfig
                                )
                                Spacer(Modifier.width(uiConfig.dimensions.innerIndent))
                                Text(text = uiConfig.texts.ticketPolicy,
                                    fontSize = uiConfig.dimensions.ticketSubText,
                                    color = uiConfig.colors.ticketSubText)
                                Spacer(Modifier.width(uiConfig.dimensions.innerIndent))
                                Text(text = uiConfig.texts.ticketPolicyLink,
                                    fontSize = uiConfig.dimensions.ticketSubText,
                                    color = uiConfig.colors.ticketSubText,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(options.consentLink)).apply {
                                            addCategory(Intent.CATEGORY_BROWSABLE)
                                        }
                                        try {
                                            ctx.startActivity(intent)
                                        } catch (ex: Exception) {}
                                    }
                                )
                            }
                            Text(
                                text = if (unacceptedConsentError.value) uiConfig.texts.unacceptedConsentError else "",
                                color = uiConfig.colors.errorPrimary,
                                fontSize = uiConfig.dimensions.ticketSubText,
                                modifier = Modifier.padding(bottom = 4.dp, top = 2.dp)
                            )
                        }

                        TicketSendButton(
                            onClick = {
                                emptyNameError.value = options.showNameField && options.isNameRequired && nameText.value.isEmpty()
                                emptyEmailError.value = options.showEmailField && options.isEmailRequired && emailText.value.isEmpty()
                                invalidEmailError.value = options.showEmailField && options.isEmailRequired && !emailText.value.isValidEmail()
                                emptyContentError.value = contentText.value.isEmpty()
                                unacceptedConsentError.value = !isConsented.value && !options.consentLink.isNullOrEmpty()

                                if (!(emptyNameError.value || emptyEmailError.value || emptyContentError.value || unacceptedConsentError.value || invalidEmailError.value)) {
                                    onSubmit(StartVisitorChatData(
                                        name = nameText.value,
                                        email = emailText.value,
                                        message = contentText.value,
                                        policy = isConsented.value
                                    ))
                                }
                            },
                            uiConfig = uiConfig
                        )

                    }
                }
            }
        }



    }

}



@Composable
fun TicketSendButton(
    onClick: () -> Unit,
    uiConfig: ChatUIConfig
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(uiConfig.dimensions.buttonCorners)
            .fillMaxWidth()
            .background(uiConfig.colors.buttonBackground)
            .clickable(
                indication = rememberRipple(color = uiConfig.colors.userRipple),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
    ) {
        Text(
            text = uiConfig.texts.ticketSend,
            fontSize = uiConfig.dimensions.messageFontSize,
            color = uiConfig.colors.userMessageText,
            modifier = Modifier.padding(uiConfig.dimensions.innerIndent)
        )
    }
}


@Composable
fun TicketTextField(
    value: MutableState<String>,
    placeholder: String,
    icon: Int? = null,
    uiConfig: ChatUIConfig,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    maxLines: Int = 1,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {

    val interaction = remember { MutableInteractionSource() }
    val isFocused by interaction.collectIsFocusedAsState()
    val shape = uiConfig.dimensions.ticketFieldsCorners

    // Цвета обводки как в исходном OutlinedTextField
    val borderColor =
        if (isFocused) uiConfig.colors.userMessageBackground
        else uiConfig.colors.ticketFieldDisabled

    // Компактные внутренние отступы
    val contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)

    BasicTextField(
        value = value.value,
        onValueChange = { value.value = it },
        singleLine = maxLines == 1,
        textStyle = LocalTextStyle.current.copy(
            fontSize = uiConfig.dimensions.messageFontSize,
            color = uiConfig.colors.ticketSubText
        ),
        cursorBrush = SolidColor(uiConfig.colors.userMessageBackground),
        interactionSource = interaction,
        maxLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(BorderStroke(1.dp, borderColor), shape)
            .padding(contentPadding),
        decorationBox = { inner ->
            Row(
                verticalAlignment = verticalAlignment,
                modifier = Modifier.fillMaxWidth()
            ) {
                icon?.let {
                    Image(
                        imageVector = ImageVector.vectorResource(it),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(uiConfig.colors.ticketFieldDisabled),
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 12.dp)
                    )
                }

                // Текстовое поле + плейсхолдер
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = uiConfig.colors.ticketFieldDisabled,
                            fontSize = uiConfig.dimensions.messageFontSize
                        )
                    }
                    inner()
                }
            }
        }
    )

}



@Composable
fun TicketCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 20.dp,
    cornerRadius: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    uiConfig: ChatUIConfig
) {
    val state = if (checked) ToggleableState.On else ToggleableState.Off

    // Цвета из Material3-цветов чекбокса
    val container = uiConfig.colors.background
    val border = uiConfig.colors.ticketFieldDisabled
    val checkmark = uiConfig.colors.userMessageBackground

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(container, shape)
            .border(borderWidth, if (checked) checkmark else border, shape)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Checkbox,
            )
            .semantics {
                toggleableState = state
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            // рисуем «галочку» сами, чтобы не тянуть иконки
            val strokePx = with(LocalDensity.current) { 3.dp.toPx() }
            Canvas(modifier = Modifier.size(size * 0.9f)) {
                val w = size.toPx()
                val h = size.toPx()
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.5f)
                    lineTo(w * 0.4f, h * 0.7f)
                    lineTo(w * 0.75f, h * 0.2f)
                }
                drawPath(
                    path = path,
                    color = checkmark,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}



fun String.isValidEmail(): Boolean =
    this.trim().let { it.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(it).matches() }




@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun TicketPreview() {
    val service = ChatService(
        serverOptions = ServerOptions(
            socketUrl = "wss://domain.com",
            originUrl = "https://domain.com",
            uploadUrl = "https://domain.com/upload",
        ),
        chatOptions = ChatOptions(
            welcomeMessage = "Hello user, chose an option",
            botName = "Support bot",
            saveUserAfterConnection = false
        ),
        context = LocalContext.current
    )
    val vm = ChatViewModel(service)

    vm.isGlobalLoading.value = false

    TicketView(
        viewModel = vm,
        uiConfig = ChatUIConfigDefault,
        options = TicketOptions(
            showNameField = true,
            showEmailField = true,
            isEmailRequired = true,
            consentLink = "https://google.com"
        ),
        userData = UserData(
            id = "", name = "Oleg99", email = "oleg99v1@gmail.com"
        ),
        ticketStatus = TicketStatus.STAFF_OFFLINE
    )
}