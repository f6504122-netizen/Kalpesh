package com.example.data

import android.content.Context
import com.example.R
import com.example.model.ChatMessage
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class KevSpaceRepository(
    private val db: FirebaseFirestore
) {
    companion object {
        fun create(context: Context): KevSpaceRepository {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            return KevSpaceRepository(FirebaseFirestore.getInstance(dbId))
        }
    }

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before performing Firestore operations.")
    }

    suspend fun saveUserProfile(user: FirebaseUser) {
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        val payload = mapOf(
            "uid" to uid,
            "displayName" to (user.displayName?.takeIf { it.isNotBlank() } ?: "KevSpace Explorer"),
            "email" to (user.email ?: ""),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "status" to "Online",
            "lastSeen" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateUserStatus(status: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = mapOf(
            "status" to status,
            "lastSeen" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeUsers(): Flow<List<UserProfile>> = callbackFlow {
        val collectionRef = db.collection("users").orderBy("displayName", Query.Direction.ASCENDING)
        val registration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, "users")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val users = snapshot.toObjects(UserProfile::class.java)
                trySend(users)
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val path = "chats/$chatId/messages"
        val query = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val messages = snapshot.toObjects(ChatMessage::class.java)
                trySend(messages)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun sendMessage(
        chatId: String,
        recipientId: String,
        text: String
    ) {
        val sender = auth.currentUser ?: throw IllegalStateException("Not authenticated")
        val senderId = sender.uid

        // 1. Ensure chat document exists and participants are set
        val chatRef = db.collection("chats").document(chatId)
        val chatPayload = mapOf(
            "chatId" to chatId,
            "participants" to listOf(senderId, recipientId).sorted(),
            "lastMessage" to text.trim(),
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "lastSenderId" to senderId
        )
        try {
            chatRef.set(chatPayload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, chatRef.path)
            throw e
        }

        // 2. Append message
        val messagesRef = chatRef.collection("messages")
        val messagePayload = mapOf(
            "chatId" to chatId,
            "senderId" to senderId,
            "senderName" to (sender.displayName ?: "User"),
            "senderPhoto" to (sender.photoUrl?.toString() ?: ""),
            "recipientId" to recipientId,
            "text" to text.trim(),
            "timestamp" to FieldValue.serverTimestamp()
        )
        try {
            messagesRef.add(messagePayload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, messagesRef.path)
            throw e
        }
    }
}
