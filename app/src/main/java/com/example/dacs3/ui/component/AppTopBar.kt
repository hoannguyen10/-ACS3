package com.example.dacs3.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    streakDays: Int = 7,
    onNotificationClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            // Logo và Tên App bên trái
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(35.dp),
                    shape = CircleShape,
                    color = Color(0xFFE0E0E0)
                ) {}
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "MindSnack",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A73E8)
                )
            }
        },
        actions = {
            // Cụm bên phải: Streak + Chuông
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // Thẻ Streak
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF4E5),
                ) {
                    Text(
                        "🔥 $streakDays Days",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFFE67E22),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Chuông thông báo
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF5F6368),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFF8FAFF) // Màu nền khớp với HomeScreen
        )
    )
}