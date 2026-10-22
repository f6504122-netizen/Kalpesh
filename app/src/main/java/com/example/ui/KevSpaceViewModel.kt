package com.example.ui

import android.app.Application
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.KevSpaceRepository
import com.example.model.ActiveCall
import com.example.model.CallState
import com.example.model.CallType
import com.example.model.ChatMessage
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class KevSpaceUiState(
    val currentUser: FirebaseUser? = null,
    val selectedUser: UserProfile? = null,
    val users: List<UserProfile> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val activeCall: ActiveCall? = null,
    val searchQuery: String = "",
    val isLoadingUsers: Boolean = true,
    val isLoadingMessages: Boolean = false,
    val error: String? = null
)

class KevSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KevSpaceRepository.create(application)
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow(KevSpaceUiState(currentUser = auth.currentUser))
    val uiState: StateFlow<KevSpaceUiState> = _uiState.asStateFlow()

    private var messagesJob: Job? = null
    private var usersJob: Job? = null
    private var callJob: Job? = null

    init {
        // Observe auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _uiState.update { it.copy(currentUser = user) }
            if (user != null) {
                observeUsersDirectory()
            } else {
                usersJob?.cancel()
                messagesJob?.cancel()
                callJob?.cancel()
                _uiState.update {
                    it.copy(
                        selectedUser = null,
                        users = emptyList(),
                        messages = emptyList(),
                        activeCall = null
                    )
                }
            }
        }
    }

    private fun observeUsersDirectory() {
        usersJob?.cancel()
        _uiState.update { it.copy(isLoadingUsers = true) }
        usersJob = viewModelScope.launch {
            repository.observeUsers()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoadingUsers = false,
                            error = exception.message ?: "Failed to load active users"
                        )
                    }
                }
                .collect { userList ->
                    _uiState.update {
                        it.copy(
                            users = userList,
                            isLoadingUsers = false
                        )
                    }
                }
        }
    }

    fun selectUser(recipient: UserProfile?) {
        _uiState.update { it.copy(selectedUser = recipient) }
        messagesJob?.cancel()

        if (recipient != null) {
            val currentUid = auth.currentUser?.uid ?: return
            val chatId = listOf(currentUid, recipient.uid).sorted().joinToString("_")

            _uiState.update { it.copy(isLoadingMessages = true, messages = emptyList()) }
            messagesJob = viewModelScope.launch {
                repository.observeMessages(chatId)
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoadingMessages = false,
                                error = exception.message ?: "Failed to sync messages"
                            )
                        }
                    }
                    .collect { messageList ->
                        _uiState.update {
                            it.copy(
                                messages = messageList,
                                isLoadingMessages = false
                            )
                        }
                    }
            }
        } else {
            _uiState.update { it.copy(messages = emptyList()) }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateUserStatus(status: String) {
        viewModelScope.launch {
            try {
                repository.updateUserStatus(status)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun sendMessage(text: String) {
        val currentUid = auth.currentUser?.uid ?: return
        val recipient = _uiState.value.selectedUser ?: return
        if (text.isBlank()) return

        val chatId = listOf(currentUid, recipient.uid).sorted().joinToString("_")

        viewModelScope.launch {
            try {
                repository.sendMessage(
                    chatId = chatId,
                    recipientId = recipient.uid,
                    text = text.trim()
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "Failed to send message") }
            }
        }
    }

    // --- Audio and Video Calling Features ---

    fun startCall(recipient: UserProfile, callType: CallType) {
        callJob?.cancel()
        val initialCall = ActiveCall(
            callId = UUID.randomUUID().toString(),
            recipient = recipient,
            callType = callType,
            state = CallState.CONNECTING
        )
        _uiState.update { it.copy(activeCall = initialCall) }

        callJob = viewModelScope.launch {
            // Ringing stage
            delay(1200)
            _uiState.update {
                it.activeCall?.let { call ->
                    it.copy(activeCall = call.copy(state = CallState.RINGING))
                } ?: it
            }

            // Connected stage
            delay(2000)
            _uiState.update {
                it.activeCall?.let { call ->
                    it.copy(activeCall = call.copy(state = CallState.CONNECTED))
                } ?: it
            }

            // Duration timer ticker
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    val call = state.activeCall
                    if (call != null && call.state == CallState.CONNECTED) {
                        state.copy(activeCall = call.copy(durationSeconds = call.durationSeconds + 1))
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun toggleMic() {
        _uiState.update { state ->
            state.activeCall?.let { call ->
                state.copy(activeCall = call.copy(isMicMuted = !call.isMicMuted))
            } ?: state
        }
    }

    fun toggleCamera() {
        _uiState.update { state ->
            state.activeCall?.let { call ->
                state.copy(activeCall = call.copy(isCameraOff = !call.isCameraOff))
            } ?: state
        }
    }

    fun toggleSpeaker() {
        _uiState.update { state ->
            state.activeCall?.let { call ->
                state.copy(activeCall = call.copy(isSpeakerOn = !call.isSpeakerOn))
            } ?: state
        }
    }

    fun toggleCameraFlip() {
        _uiState.update { state ->
            state.activeCall?.let { call ->
                state.copy(activeCall = call.copy(isFrontCamera = !call.isFrontCamera))
            } ?: state
        }
    }

    fun endCall() {
        val call = _uiState.value.activeCall ?: return
        callJob?.cancel()

        // Transition to ended state
        _uiState.update {
            it.copy(activeCall = call.copy(state = CallState.ENDED))
        }

        // Post call summary message to the chat
        val durationFormatted = String.format(
            "%02d:%02d",
            call.durationSeconds / 60,
            call.durationSeconds % 60
        )
        val icon = if (call.callType == CallType.VIDEO) "📹" else "📞"
        val callTypeName = if (call.callType == CallType.VIDEO) "Video Call" else "Audio Call"
        val callLog = "$icon $callTypeName ended • $durationFormatted"

        viewModelScope.launch {
            val currentUid = auth.currentUser?.uid
            if (currentUid != null) {
                val chatId = listOf(currentUid, call.recipient.uid).sorted().joinToString("_")
                try {
                    repository.sendMessage(
                        chatId = chatId,
                        recipientId = call.recipient.uid,
                        text = callLog
                    )
                } catch (_: Exception) {}
            }
            delay(800)
            _uiState.update { it.copy(activeCall = null) }
        }
    }

    fun signOut(context: Context) {
        auth.signOut()
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
            }
        }
    }
}
