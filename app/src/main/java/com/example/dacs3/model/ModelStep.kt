package com.example.dacs3.model

import androidx.compose.ui.graphics.vector.ImageVector

data class LevelStep(
    val id: Int,
    val title: String,
    val description: String,
    val xpRequired: Int,
    val icon: ImageVector
)