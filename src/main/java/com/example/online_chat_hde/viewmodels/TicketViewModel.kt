package com.example.online_chat_hde.viewmodels

import android.util.Patterns
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.online_chat_hde.core.ChatClient
import com.example.online_chat_hde.models.StartVisitorChatData
import com.example.online_chat_hde.models.TicketOptions
import com.example.online_chat_hde.models.TicketStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class TicketViewModelFactory(
    private val chatClient: ChatClient
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TicketViewModel(chatClient) as T
    }
}


sealed interface TicketEffect {
    data class Submit(val data: StartVisitorChatData) : TicketEffect
}

class TicketViewModel(
    client: ChatClient
): ViewModel() {
    private val _state = MutableStateFlow(
        TicketUiState(
            name = client.userData?.name.orEmpty(),
            email = client.userData?.email.orEmpty()
        )
    )
    val state: StateFlow<TicketUiState> = _state.asStateFlow()


    private val _effects = MutableSharedFlow<TicketEffect>()
    val effects: SharedFlow<TicketEffect> = _effects


    val ticketOptions = client.ticketOptions


    fun onNameChange(v: String) = _state.update {
        it.copy(name = v)
    }
    fun onEmailChange(v: String) = _state.update {
        it.copy(email = v)
    }
    fun onContentChange(v: String) = _state.update {
        it.copy(content = v)
    }
    fun onConsentAgreementChange(v: Boolean) = _state.update { it.copy(isConsented = v) }

    fun onSubmitClicked() {
        val options = ticketOptions
        val s = _state.value

        val needName    = options.showNameField
        val needEmail   = options.showEmailField && options.isEmailRequired
        val needConsent = !options.consentLink.isNullOrEmpty()

        val nameEmpty   = needName  && s.name.isBlank()
        val emailEmpty  = needEmail && s.email.isBlank()
        val emailBad    = needEmail && s.email.isNotBlank() && !s.email.isValidEmail()
        val contentEmpty= s.content.isBlank()
        val consentBad  = needConsent && !s.isConsented

        val hasErrors = nameEmpty || emailEmpty || emailBad || contentEmpty || consentBad

        _state.update {
            it.copy(
                emptyNameError = nameEmpty,
                emptyEmailError = emailEmpty,
                invalidEmailError = emailBad,
                emptyContentError = contentEmpty,
                unacceptedConsentError = consentBad
            )
        }

        if (!hasErrors) {
            val data = StartVisitorChatData(
                name = s.name,
                email = s.email,
                message = s.content,
                policy = s.isConsented
            )
            viewModelScope.launch { _effects.emit(TicketEffect.Submit(data)) }
        }
    }
}


internal fun String.isValidEmail(): Boolean =
    this.trim().let { it.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(it).matches() }



@Immutable
data class TicketUiState(
    val name: String = "",
    val email: String = "",
    val content: String = "",
    val isConsented: Boolean = false,
    val emptyNameError: Boolean = false,
    val emptyEmailError: Boolean = false,
    val invalidEmailError: Boolean = false,
    val emptyContentError: Boolean = false,
    val unacceptedConsentError: Boolean = false,
)