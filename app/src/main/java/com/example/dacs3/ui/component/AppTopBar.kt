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

// Bảng màu đồng bộ
private val GreenDeep = Color(0xFF3C7363)
private val GreenMedium = Color(0xFF84D9BA)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    streakDays: Int = 7,
    onNotificationClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Logo placeholder - sử dụng màu GreenMedium
                Surface(
                    modifier = Modifier.size(35.dp),
                    shape = CircleShape,
                    color = GreenMedium.copy(alpha = 0.3f)
                ) {
                    // Bạn có thể thêm Icon logo vào đây nếu muốn
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "MindSnack",
                    fontWeight = FontWeight.Black, // Đổi sang Black cho đồng bộ
                    fontSize = 20.sp,
                    color = GreenDeep // Đổi từ Blue sang GreenDeep
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // Thẻ Streak với màu cam rực rỡ (giữ nguyên để nổi bật hoặc dùng tông Green)
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

                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = GreenDeep, // Đổi màu chuông
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White // Nền trắng đồng bộ với Scaffold
        )
    )
}