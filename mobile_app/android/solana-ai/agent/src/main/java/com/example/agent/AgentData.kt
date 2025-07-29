package com.example.agent

data class MessageShortcut(
    val firstMessage: String,
    val id: String
)

data class ChatResponse(
    val response: String,
    val id: String
)