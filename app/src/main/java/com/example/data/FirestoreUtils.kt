package com.example.data

import android.util.Log

enum class OperationType {
    CREATE, GET, LIST, UPDATE, DELETE, TRANSACTION, WRITE_BATCH
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String
) {
    val escapedMessage = exception.message?.replace("\"", "\\\"") ?: "Unknown error"
    val json = """{"error":"$escapedMessage","operation":"$operationType","path":"$path"}"""
    Log.e("FirestoreError", json)
}
