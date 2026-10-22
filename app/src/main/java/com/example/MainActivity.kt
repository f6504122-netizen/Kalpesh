package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CallType
import com.example.model.UserProfile
import com.example.ui.CallScreen
import com.example.ui.ChatWindowScreen
import com.example.ui.KevSpaceUiState
import com.example.ui.KevSpaceViewModel
import com.example.ui.LoginScreen
import com.example.ui.SidebarScreen
import com.example.ui.theme.KevSpaceDarkBg
import com.example.ui.theme.KevSpaceTheme

class MainActivity : ComponentActivity() {

    private val viewModel: KevSpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KevSpaceTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KevSpaceDarkBg
                ) {
                    KevSpaceApp(
                        state = state,
                        onSelectUser = { viewModel.selectUser(it) },
                        onBackToDirectory = { viewModel.selectUser(null) },
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onSendMessage = { viewModel.sendMessage(it) },
                        onUpdateStatus = { viewModel.updateUserStatus(it) },
                        onStartAudioCall = { recipient -> viewModel.startCall(recipient, CallType.AUDIO) },
                        onStartVideoCall = { recipient -> viewModel.startCall(recipient, CallType.VIDEO) },
                        onToggleMic = { viewModel.toggleMic() },
                        onToggleCamera = { viewModel.toggleCamera() },
                        onToggleSpeaker = { viewModel.toggleSpeaker() },
                        onToggleCameraFlip = { viewModel.toggleCameraFlip() },
                        onEndCall = { viewModel.endCall() },
                        onSignOut = { viewModel.signOut(context) }
                    )
                }
            }
        }
    }
}

@Composable
fun KevSpaceApp(
    state: KevSpaceUiState,
    onSelectUser: (UserProfile) -> Unit,
    onBackToDirectory: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onStartAudioCall: (UserProfile) -> Unit,
    onStartVideoCall: (UserProfile) -> Unit,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleCameraFlip: () -> Unit,
    onEndCall: () -> Unit,
    onSignOut: () -> Unit
) {
    val currentUser = state.currentUser

    // 1. Unauthenticated Gate -> Login Screen
    if (currentUser == null) {
        LoginScreen(
            onAuthSuccess = { /* Automatically triggered by FirebaseAuth.AuthStateListener */ }
        )
    } else {
        // 2. If active call in progress, show full screen call UI
        val call = state.activeCall
        if (call != null) {
            CallScreen(
                call = call,
                onToggleMic = onToggleMic,
                onToggleCamera = onToggleCamera,
                onToggleSpeaker = onToggleSpeaker,
                onToggleCameraFlip = onToggleCameraFlip,
                onEndCall = onEndCall
            )
            return
        }

        // 3. Authenticated Session -> KevSpace Chat Hub
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpanded = maxWidth >= 700.dp

            if (isExpanded) {
                // Tablet / Large Screen Dual-Pane Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    SidebarScreen(
                        currentUser = currentUser,
                        users = state.users,
                        isLoadingUsers = state.isLoadingUsers,
                        searchQuery = state.searchQuery,
                        onSearchChange = onSearchChange,
                        onSelectUser = onSelectUser,
                        onSignOut = onSignOut,
                        onUpdateStatus = onUpdateStatus,
                        modifier = Modifier.width(360.dp)
                    )

                    val activeRecipient = state.selectedUser ?: state.users.firstOrNull { it.uid != currentUser.uid }
                    if (activeRecipient != null) {
                        ChatWindowScreen(
                            currentUser = currentUser,
                            recipient = activeRecipient,
                            messages = state.messages,
                            isLoadingMessages = state.isLoadingMessages,
                            onSendMessage = onSendMessage,
                            onBack = { /* Retain pane view on large screens */ },
                            onStartAudioCall = { onStartAudioCall(activeRecipient) },
                            onStartVideoCall = { onStartVideoCall(activeRecipient) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Mobile Single-Pane Navigation
                if (state.selectedUser == null) {
                    SidebarScreen(
                        currentUser = currentUser,
                        users = state.users,
                        isLoadingUsers = state.isLoadingUsers,
                        searchQuery = state.searchQuery,
                        onSearchChange = onSearchChange,
                        onSelectUser = onSelectUser,
                        onSignOut = onSignOut,
                        onUpdateStatus = onUpdateStatus,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ChatWindowScreen(
                        currentUser = currentUser,
                        recipient = state.selectedUser,
                        messages = state.messages,
                        isLoadingMessages = state.isLoadingMessages,
                        onSendMessage = onSendMessage,
                        onBack = onBackToDirectory,
                        onStartAudioCall = { onStartAudioCall(state.selectedUser) },
                        onStartVideoCall = { onStartVideoCall(state.selectedUser) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
