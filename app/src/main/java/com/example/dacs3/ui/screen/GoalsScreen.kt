package com.example.dacs3.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dacs3.ui.component.AppBottomBar
import com.example.dacs3.ui.component.AppTopBar

// Bảng màu đồng bộ từ RegisterScreen
private val GreenDeep = Color(0xFF3C7363)
private val GreenMedium = Color(0xFF84D9BA)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)

@Composable
fun GoalsScreen(navController: NavController) {
    Scaffold(
        topBar = { AppTopBar(streakDays = 7) },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = Color.White // Đồng bộ nền trắng
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            // 1. Mục tiêu hàng ngày (Daily Goals)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LightBlueBg), // Dùng LightBlueBg làm nền card
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mục tiêu hàng ngày", color = GreenDeep.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Học 5 kiến thức mới", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextBlack)
                        }
                        Surface(
                            modifier = Modifier.size(45.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.padding(10.dp), tint = GreenDeep)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Tiến độ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
                        Text("4/5", fontWeight = FontWeight.Black, fontSize = 13.sp, color = GreenDeep)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = 0.8f,
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = GreenDeep,
                        trackColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = GreenDeep.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Còn 1 kiến thức nữa để hoàn thành mục tiêu!", fontSize = 12.sp, color = GreenDeep.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // 2. Thẻ thống kê Chuỗi ngày & Cấp độ
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                AchievementStatCard("Chuỗi ngày", "7 ngày liên tiếp", Icons.Default.LocalFireDepartment, Color(0xFFFF5722), Modifier.weight(1f))
                AchievementStatCard("Danh hiệu", "Người học thông thái", Icons.Default.EmojiEvents, GreenDeep, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 3. Tiến độ học tập
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Tiến độ học tập", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
                Text("Xem tất cả", color = GreenDeep, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(15.dp))
            CourseProgressItem("Hán tự N5", "15/20 bài học", 0.75f, Icons.Default.MenuBook)
            CourseProgressItem("Sự thật thú vị", "42/50 bài học", 0.84f, Icons.Default.Star)

            Spacer(modifier = Modifier.height(30.dp))

            // 4. Huy hiệu thành tích (Badges)
            Text("Huy hiệu đạt được", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
            Spacer(modifier = Modifier.height(15.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BadgeItem("Ham Học Hỏi", Icons.Default.AutoStories, GreenMedium)
                BadgeItem("Siêu Tốc Độ", Icons.Default.ElectricBolt, GreenDeep)
                BadgeItem("Bậc Thầy Chuỗi", Icons.Default.Stars, Color(0xFFFFC107))
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun AchievementStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(150.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(value, fontWeight = FontWeight.Black, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = TextBlack)
        }
    }
}

@Composable
fun CourseProgressItem(title: String, progressText: String, progress: Float, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(45.dp), shape = RoundedCornerShape(12.dp), color = LightBlueBg) {
                Icon(icon, null, modifier = Modifier.padding(10.dp), tint = GreenDeep)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = TextBlack)
                    Text(progressText, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = GreenDeep,
                    trackColor = LightBlueBg
                )
            }
        }
    }
}

@Composable
fun BadgeItem(name: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(85.dp)) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.3f))
        ) {
            Icon(icon, null, modifier = Modifier.padding(16.dp), tint = color)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp,
            color = TextBlack
        )
    }
}