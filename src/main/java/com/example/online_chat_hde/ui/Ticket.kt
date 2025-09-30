package com.example.online_chat_hde.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.online_chat_hde.ChatUIScope
import com.example.online_chat_hde.models.ChatOptions
import com.example.online_chat_hde.core.ChatClient
import com.example.online_chat_hde.viewmodels.ChatViewModel
import com.example.online_chat_hde.core.ServerOptions
import com.example.online_chat_hde.models.TicketOptions
import com.example.online_chat_hde.models.TicketStatus
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.viewmodels.TicketEffect
import com.example.online_chat_hde.viewmodels.TicketViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import com.example.online_chat_hde.models.Staff
import com.example.online_chat_hde.viewmodels.ChatState


@Stable
interface TicketScope: ChatUIScope {
    val isGlobalLoading: Boolean
    val ticketStatus: TicketStatus
    val options: TicketOptions

    val headerScope: ChatTopPanelScope
    val nameInputScope: NameInputScope
    val emailInputScope: EmailInputScope
    val contentInputScope: ContentInputScope
    val policyConsentScope: PolicyConsentScope
    val ticketSendButtonScope: TicketSendButtonScope
}




@Composable
fun ChatUIScope.TicketHost(
    ticketViewModel: TicketViewModel,
    chatState: ChatState,
    onSubmit: (StartVisitorChatData) -> Unit,
    onClose: () -> Unit,
    ticket: @Composable TicketScope.() -> Unit
) {

    val ticketState by ticketViewModel.state.collectAsStateWithLifecycle()

    // ловим эффект и пробрасываем наружу
    LaunchedEffect(Unit) {
        ticketViewModel.effects.collect { eff ->
            when (eff) {
                is TicketEffect.Submit -> onSubmit(eff.data)
            }
        }
    }

    val headerFacet = remember(chatState.staff) {
        object : ChatTopPanelScope {
            override val staff = chatState.staff
            override val closeChat = onClose
            override val uiConfig = this@TicketHost.uiConfig
        }
    }

    val nameInputFacet =
        object : NameInputScope {
            override val name = ticketState.name
            override val onNameChange = ticketViewModel::onNameChange
            override val showNameError = ticketState.emptyNameError
            override val uiConfig = this@TicketHost.uiConfig
        }


    val emailInputFacet = remember(ticketState.email, ticketState.emptyEmailError, ticketState.invalidEmailError) {
        object : EmailInputScope {
            override val email = ticketState.email
            override val onEmailChange = ticketViewModel::onEmailChange
            override val showEmptyEmailError = ticketState.emptyEmailError
            override val showIncorrectEmailError = ticketState.invalidEmailError
            override val uiConfig = this@TicketHost.uiConfig
        }
    }

    val contentInputFacet = remember(ticketState.content, ticketState.emptyContentError) {
        object : ContentInputScope {
            override val content = ticketState.content
            override val onContentChange = ticketViewModel::onContentChange
            override val showEmptyContentError = ticketState.emptyContentError
            override val uiConfig = this@TicketHost.uiConfig
        }
    }

    val policyConsentFacet = remember(ticketState.unacceptedConsentError, ticketState.isConsented) {
        object : PolicyConsentScope {
            override val options = ticketViewModel.ticketOptions
            override val unacceptedConsentError = ticketState.unacceptedConsentError
            override val isConsented = ticketState.isConsented
            override val onConsentAgreementChange = ticketViewModel::onConsentAgreementChange
            override val uiConfig = this@TicketHost.uiConfig
        }
    }

    val ticketSendButtonFacet = remember {
        object : TicketSendButtonScope {
            override val onSubmitClicked = ticketViewModel::onSubmitClicked
            override val uiConfig = this@TicketHost.uiConfig
        }
    }

    val ticketScope = object : TicketScope {
        override val uiConfig = this@TicketHost.uiConfig
        override val options = ticketViewModel.ticketOptions
        override val isGlobalLoading = chatState.isGlobalLoading
        override val ticketStatus = chatState.ticketStatus

        override val headerScope = headerFacet
        override val nameInputScope = nameInputFacet
        override val emailInputScope = emailInputFacet
        override val contentInputScope = contentInputFacet
        override val policyConsentScope = policyConsentFacet
        override val ticketSendButtonScope = ticketSendButtonFacet
    }


    with (ticketScope) {
        ticket()
    }

}


@Stable
interface TicketFrameArgs {
    val header:        @Composable () -> Unit
    val globalLoading: @Composable () -> Unit
    val waitForReply:  @Composable () -> Unit
    val staffOffline:  @Composable () -> Unit
    val nameInput:     @Composable () -> Unit
    val emailInput:    @Composable () -> Unit
    val contentInput:  @Composable () -> Unit
    val policyConsent: @Composable () -> Unit
    val sendButton:    @Composable () -> Unit
}

@Composable
fun TicketScope.Ticket(
    header:        @Composable ChatTopPanelScope.() -> Unit = { ChatHeader() },
    globalLoading: @Composable ChatUIScope.() -> Unit = { GlobalLoading() },
    waitForReply:  @Composable ChatUIScope.() -> Unit = { WaitForReply() },
    staffOffline:  @Composable ChatUIScope.() -> Unit = { StaffOffline() },
    nameInput:     @Composable NameInputScope.() -> Unit = { NameInput() },
    emailInput:    @Composable EmailInputScope.() -> Unit = { EmailInput() },
    contentInput:  @Composable ContentInputScope.() -> Unit = { ContentInput() },
    policyConsent: @Composable PolicyConsentScope.() -> Unit = { PolicyConsent() },
    sendButton:    @Composable TicketSendButtonScope.() -> Unit = { TicketSendButton() },

    frame: @Composable TicketScope.(TicketFrameArgs) -> Unit = {
        layout ->
        val innerIndent = uiConfig.dimensions.innerIndent
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(uiConfig.colors.background)
        ) {
            layout.header()
            if (isGlobalLoading) {
                layout.globalLoading()
            }
            else {
                if (ticketStatus == TicketStatus.WAIT_FOR_REPLY) {
                    layout.waitForReply()
                }
                else {
                    Column(
                        modifier = Modifier.padding(vertical = innerIndent*2)
                    ) {
                        if (ticketStatus == TicketStatus.STAFF_OFFLINE) {
                            layout.staffOffline()
                        }
                        if (options.showNameField) {
                            layout.nameInput()
                        }
                        if (options.showEmailField) {
                            layout.emailInput()
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            layout.contentInput()
                        }
                        if (!options.consentLink.isNullOrEmpty()) {
                            layout.policyConsent()
                        }
                        Box(modifier = Modifier.padding(horizontal = innerIndent)) {
                            layout.sendButton()
                        }
                    }
                }
            }
        }
    }
) {

    val params = remember(
        headerScope, this, nameInputScope, emailInputScope,
        contentInputScope, policyConsentScope, ticketSendButtonScope
    ) {
        object : TicketFrameArgs {
            override val header: @Composable () -> Unit = { with(headerScope) { header() } }
            override val globalLoading: @Composable () -> Unit = { with(this@Ticket) { globalLoading() } }
            override val waitForReply: @Composable () -> Unit = { with(this@Ticket) { waitForReply() } }
            override val staffOffline: @Composable () -> Unit = { with(this@Ticket) { staffOffline() } }
            override val nameInput: @Composable () -> Unit = { with(nameInputScope) { nameInput() } }
            override val emailInput: @Composable () -> Unit = { with(emailInputScope) { emailInput() } }
            override val contentInput: @Composable () -> Unit = { with(contentInputScope) { contentInput() } }
            override val policyConsent: @Composable () -> Unit = { with(policyConsentScope) { policyConsent() } }
            override val sendButton: @Composable () -> Unit = { with(ticketSendButtonScope) { sendButton() } }
        }
    }

    frame(params)

}



@Composable
fun ChatUIScope.StaffOffline() {
    val pad = uiConfig.dimensions.ticketFieldsIndent
    Text(
        modifier = Modifier.padding(pad),
        text = uiConfig.texts.ticketOffline,
        fontSize = uiConfig.dimensions.ticketSubFontSize,
        color = uiConfig.colors.ticketSubText
    )
}

@Composable
fun ChatUIScope.WaitForReply() {
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



interface PolicyConsentFrameArgs {
    val checkbox: @Composable () -> Unit
    val ticketPolicy: @Composable () -> Unit
    val ticketPolicyLink: @Composable () -> Unit
    val errorMessage: @Composable () -> Unit
}

interface PolicyConsentScope: TicketCheckboxScope {
    val options: TicketOptions
    val unacceptedConsentError: Boolean
}
@Composable
fun PolicyConsentScope.PolicyConsent(
    innerIndent: Dp = uiConfig.dimensions.innerIndent,
    checkbox: @Composable PolicyConsentScope.() -> Unit = {
        TicketCheckbox()
    },
    ticketPolicy: @Composable PolicyConsentScope.() -> Unit = {
        TicketPolicy()
    },
    ticketPolicyLink: @Composable PolicyConsentScope.() -> Unit = {
        TicketPolicyLink()
    },
    errorMessage: @Composable PolicyConsentScope.() -> Unit = {
        ErrorMessage(unacceptedConsentError, uiConfig.texts.errorUnacceptedConsent)
    },
    frame: @Composable PolicyConsentScope.(PolicyConsentFrameArgs) -> Unit = { layout ->
        Column(modifier = Modifier.padding(horizontal = innerIndent)) {
            Row(verticalAlignment = Alignment.CenterVertically,) {
                layout.checkbox()
                Spacer(Modifier.width(innerIndent))
                layout.ticketPolicy()
                Spacer(Modifier.width(innerIndent))
                layout.ticketPolicyLink()
            }
            layout.errorMessage()
        }
    }
) {

    val layout = remember(isConsented, unacceptedConsentError) {
        object : PolicyConsentFrameArgs {
            override val checkbox: @Composable () -> Unit = { with(this@PolicyConsent) { checkbox() } }
            override val ticketPolicy: @Composable () -> Unit = { with(this@PolicyConsent) { ticketPolicy() } }
            override val ticketPolicyLink: @Composable () -> Unit = { with(this@PolicyConsent) { ticketPolicyLink() } }
            override val errorMessage: @Composable () -> Unit = { with(this@PolicyConsent) { errorMessage() } }
        }
    }

    frame(layout)
}

@Composable
fun PolicyConsentScope.TicketPolicy() {
    Text(text = uiConfig.texts.ticketPolicy,
        fontSize = uiConfig.dimensions.ticketSubFontSize,
        color = uiConfig.colors.ticketSubText)
}

@Composable
fun PolicyConsentScope.TicketPolicyLink(
    text: String = uiConfig.texts.ticketPolicyLink,
    fontSize: TextUnit = uiConfig.dimensions.ticketSubFontSize,
    color: Color = uiConfig.colors.ticketSubText,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val ctx = LocalContext.current
    Text(text = text,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
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




interface TicketSendButtonScope: ChatUIScope {
    val onSubmitClicked: () -> Unit
}

@Composable
fun TicketSendButtonScope.TicketSendButton(
    corners: RoundedCornerShape = uiConfig.dimensions.buttonCorners,
    background: Color = uiConfig.colors.buttonBackground,
    ripple: Color = uiConfig.colors.userRipple,
    text: String = uiConfig.texts.ticketSend,
    fontSize: TextUnit = uiConfig.dimensions.messageFontSize,
    textColor: Color = uiConfig.colors.userMessageText,
    innerPadding: Dp = uiConfig.dimensions.innerIndent
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(corners)
            .fillMaxWidth()
            .background(background)
            .clickable(
                indication = ripple(color = ripple),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onSubmitClicked
            )
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = textColor,
            modifier = Modifier.padding(innerPadding)
        )
    }
}



interface InputFrameArgs {
    val textField: @Composable () -> Unit
    val error: @Composable () -> Unit
}

interface NameInputScope: ChatUIScope {
    val name: String
    val onNameChange: (String) -> Unit
    val showNameError: Boolean
}

@Composable
fun NameInputScope.NameInput(
    outerPadding: Dp = uiConfig.dimensions.innerIndent,
    innerPadding: Dp = 2.dp,
    logo: Int? = uiConfig.media.ticketNameLogo,
    placeholder: String = uiConfig.texts.ticketNamePlaceholder,

    textField: @Composable NameInputScope.() -> Unit = {
        TicketTextField(name, onNameChange, placeholder, logo, showNameError)
    },
    error: @Composable NameInputScope.() -> Unit = {
        ErrorMessage(showNameError, uiConfig.texts.errorEmptyName)
    },

    frame: @Composable NameInputScope.(InputFrameArgs) -> Unit = { layout ->
        Column(Modifier.padding(horizontal = outerPadding)) {
            layout.textField()
            Box(modifier = Modifier.padding(vertical = innerPadding)) {
                layout.error()
            }
        }
    }
) {

    val layout = remember(name, showNameError) {
        object : InputFrameArgs {
            override val textField: @Composable () -> Unit = { with(this@NameInput) { textField() } }
            override val error: @Composable () -> Unit = { with(this@NameInput) { error() } }
        }
    }

    frame(layout)
}




interface EmailInputScope: ChatUIScope {
    val email: String
    val onEmailChange: (String) -> Unit
    val showEmptyEmailError: Boolean
    val showIncorrectEmailError: Boolean
}

@Composable
fun EmailInputScope.EmailInput(
    outerPadding: Dp = uiConfig.dimensions.innerIndent,
    innerPadding: Dp = 2.dp,
    logo: Int? = uiConfig.media.ticketEmailLogo,
    placeholder: String = uiConfig.texts.ticketEmailPlaceholder,

    textField: @Composable EmailInputScope.() -> Unit = { ->
        TicketTextField(email, onEmailChange, placeholder, logo, showEmptyEmailError || showIncorrectEmailError)
    },
    error: @Composable EmailInputScope.() -> Unit = {
        ErrorMessage(
            showEmptyEmailError || showIncorrectEmailError,
            if (showEmptyEmailError) uiConfig.texts.errorEmptyEmail
            else uiConfig.texts.errorInvalidEmail
        )
    },

    frame: @Composable EmailInputScope.(InputFrameArgs) -> Unit = { layout ->
        Column(Modifier.padding(horizontal = outerPadding)) {
            layout.textField()
            Box(modifier = Modifier.padding(vertical = innerPadding)) {
                layout.error()
            }
        }
    }
) {
    val layout = remember(email, showEmptyEmailError, showIncorrectEmailError) {
        object : InputFrameArgs {
            override val textField: @Composable () -> Unit = { with(this@EmailInput) { textField() } }
            override val error: @Composable () -> Unit = { with(this@EmailInput) { error() } }
        }
    }

    frame(
        layout
    )
}


interface ContentInputScope: ChatUIScope {
    val content: String
    val onContentChange: (String) -> Unit
    val showEmptyContentError: Boolean
}

@Composable
fun ContentInputScope.ContentInput(
    outerPadding: Dp = uiConfig.dimensions.innerIndent,
    innerPadding: Dp = 2.dp,
    placeholder: String = uiConfig.texts.ticketEmailPlaceholder,

    textField: @Composable ContentInputScope.() -> Unit = { ->
        TicketTextField(
            content, onContentChange, placeholder, null, showEmptyContentError,
            alignment = Alignment.Top, maxHeight = true
        )
    },
    error: @Composable ContentInputScope.() -> Unit = {
        ErrorMessage(showEmptyContentError, uiConfig.texts.errorEmptyContent)
    },

    frame: @Composable ContentInputScope.(InputFrameArgs) -> Unit = { layout ->
        Column(Modifier.padding(horizontal = outerPadding)) {
            Box(modifier = Modifier.weight(1f)) {
                layout.textField()
            }
            Box(modifier = Modifier.padding(vertical = innerPadding)) {
                layout.error()
            }
        }
    }
) {
    val layout = remember(content, showEmptyContentError) {
        object : InputFrameArgs {
            override val textField: @Composable () -> Unit = { with(this@ContentInput) { textField() } }
            override val error: @Composable () -> Unit = { with(this@ContentInput) { error() } }
        }
    }

    frame(
        layout
    )
}


@Composable
fun ChatUIScope.ErrorMessage(
    showError: Boolean,
    errorText: String,
    color: Color = uiConfig.colors.errorPrimary,
    fontSize: TextUnit = uiConfig.dimensions.ticketSubFontSize,
) {
    Text(
        text = if (showError) errorText else "",
        color = color,
        fontSize = fontSize,
    )
}


@Composable
fun ChatUIScope.TicketTextField(
    text: String,
    onChange: (String) -> Unit,
    placeholder: String,
    icon: Int? = null,
    showError: Boolean,
    maxLines: Int = 1,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    maxHeight: Boolean = false,

    shape: RoundedCornerShape = uiConfig.dimensions.ticketFieldsCorners,
    borderWidth: Dp = 1.dp,
    borderErrorColor: Color = uiConfig.colors.errorPrimary,
    borderDefaultColor: Color = uiConfig.colors.ticketFieldDisabled,
    backgroundErrorColor: Color = uiConfig.colors.inputFieldWithErrorBackground,
    backgroundDefaultColor: Color = uiConfig.colors.inputFieldBackground,
    borderFocusedColor: Color = uiConfig.colors.userMessageBackground,
    placeholderColor: Color = uiConfig.colors.ticketFieldDisabled,
    subTextColor: Color = uiConfig.colors.ticketSubText,
    fontSize: TextUnit = uiConfig.dimensions.messageFontSize,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
) {

    val interaction = remember { MutableInteractionSource() }
    val isFocused by interaction.collectIsFocusedAsState()

    // Цвета обводки как в исходном OutlinedTextField
    val borderColor =
        if (showError) borderErrorColor
        else if (isFocused) borderFocusedColor
        else borderDefaultColor

    val modifier = if (maxHeight) Modifier.fillMaxHeight() else Modifier

    BasicTextField(
        value = text,
        onValueChange = onChange,
        singleLine = maxLines == 1,
        textStyle = LocalTextStyle.current.copy(
            fontSize = fontSize,
            color = subTextColor
        ),
        cursorBrush = SolidColor(borderDefaultColor),
        interactionSource = interaction,
        maxLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(BorderStroke(borderWidth, borderColor), shape)
            .background(if (showError) backgroundErrorColor else backgroundDefaultColor)
            .padding(contentPadding),
        decorationBox = { inner ->
            Row(
                verticalAlignment = alignment,
                modifier = Modifier.fillMaxWidth()
            ) {
                icon?.let {
                    Image(
                        imageVector = ImageVector.vectorResource(it),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        colorFilter = ColorFilter.tint(placeholderColor),
                        modifier = Modifier
                            .padding(start = 4.dp, end = 8.dp)
                            .size(20.dp, 26.dp)
                    )
                }

                // Текстовое поле + плейсхолдер
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = placeholderColor,
                            fontSize = fontSize
                        )
                    }
                    inner()
                }
            }
        }
    )

}



interface TicketCheckboxScope: ChatUIScope {
    val isConsented: Boolean
    val onConsentAgreementChange: (Boolean) -> Unit
}

@Composable
fun TicketCheckboxScope.TicketCheckbox(
    size: Dp = 20.dp,
    cornerRadius: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    containerColor: Color = uiConfig.colors.inputFieldBackground,
    borderColor: Color = uiConfig.colors.ticketFieldDisabled,
    checkmarkColor: Color = uiConfig.colors.userMessageBackground
) {
    val toggleable = if (isConsented) ToggleableState.On else ToggleableState.Off

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(containerColor, shape)
            .border(borderWidth, if (isConsented) checkmarkColor else borderColor, shape)
            .toggleable(
                value = isConsented,
                onValueChange = onConsentAgreementChange,
                enabled = true,
                role = Role.Checkbox,
            )
            .semantics {
                toggleableState = toggleable
            },
        contentAlignment = Alignment.Center
    ) {
        if (isConsented) {
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
                    color = checkmarkColor,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}





@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
internal fun TicketPreview() {
    val service = ChatClient(
        serverOptions = ServerOptions.fromDomain(""),
        chatOptions = ChatOptions(
            welcomeMessage = "Hello user, chose an option",
            botName = "Support bot",
        ),
        ticketOptions = TicketOptions(
            showNameField = true,
            showEmailField = true,
            consentLink = "qwert"
        ),
        context = LocalContext.current
    )
    val vm = ChatViewModel(service)
    vm.setGlobalLoading(false)

    val scope = object : TicketScope {
        override val isGlobalLoading: Boolean = false
        override val ticketStatus: TicketStatus = TicketStatus.STAFF_OFFLINE
        override val options: TicketOptions = service.ticketOptions
        override val headerScope: ChatTopPanelScope = object : ChatTopPanelScope {
            override val staff: Staff? = vm.state.collectAsState().value.staff
            override val closeChat = {}
            override val uiConfig: ChatUIConfig = ChatUIConfigDefault
        }
        override val nameInputScope: NameInputScope = object : NameInputScope {
            override val name: String = ""
            override val onNameChange: (String) -> Unit = {}
            override val showNameError: Boolean = false
            override val uiConfig: ChatUIConfig = ChatUIConfigDefault
        }
        override val emailInputScope: EmailInputScope = object : EmailInputScope {
            override val email: String = ""
            override val onEmailChange: (String) -> Unit = {}
            override val showEmptyEmailError: Boolean = false
            override val showIncorrectEmailError: Boolean = false
            override val uiConfig: ChatUIConfig = ChatUIConfigDefault
        }
        override val contentInputScope: ContentInputScope = object : ContentInputScope {
            override val content: String = ""
            override val onContentChange: (String) -> Unit = {}
            override val showEmptyContentError: Boolean = false
            override val uiConfig: ChatUIConfig = ChatUIConfigDefault
        }
        override val policyConsentScope: PolicyConsentScope = object : PolicyConsentScope {
            override val options: TicketOptions = vm.getTicketOptions()
            override val unacceptedConsentError: Boolean = false
            override val isConsented: Boolean = true
            override val onConsentAgreementChange: (Boolean) -> Unit = {}
            override val uiConfig: ChatUIConfig = ChatUIConfigDefault
        }
        override val ticketSendButtonScope: TicketSendButtonScope = object : TicketSendButtonScope {
            override val onSubmitClicked: () -> Unit = {}
            override val uiConfig: ChatUIConfig = ChatUIConfigDefault
        }
        override val uiConfig: ChatUIConfig = ChatUIConfigDefault
    }

    with (scope) {
        Ticket()
    }
}