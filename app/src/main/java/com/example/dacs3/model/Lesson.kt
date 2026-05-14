package com.example.dacs3.model

data class Lesson(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val duration: String = "",
    val rating: Double = 0.0,
    val content: String = "",
    val explanation: String = "" // Dùng cho mặt sau của thẻ Random
)