package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val status: String = "Online",
    val bio: String = "Exploring KevSpace 🚀",
    @ServerTimestamp val lastSeen: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val recipientId: String = "",
    val text: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)

@IgnoreExtraProperties
data class Chat(
    @DocumentId val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    @ServerTimestamp val lastMessageTimestamp: Timestamp? = null,
    val lastSenderId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class CallType {
    AUDIO, VIDEO
}

enum class CallState {
    CONNECTING, RINGING, CONNECTED, ENDED
}

data class ActiveCall(
    val callId: String = "",
    val recipient: UserProfile,
    val callType: CallType = CallType.AUDIO,
    val state: CallState = CallState.CONNECTING,
    val durationSeconds: Int = 0,
    val isMicMuted: Boolean = false,
    val isCameraOff: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isFrontCamera: Boolean = true
)
