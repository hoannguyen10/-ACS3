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

@Composable
fun GoalsScreen(navController: NavController) {
    Scaffold(
        topBar = { AppTopBar(streakDays = 7) },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Mục tiêu hàng ngày (Daily Goals)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mục tiêu hàng ngày", color = Color.Gray, fontSize = 14.sp)
                            Text("Học 5 kiến thức mới", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Surface(
                            modifier = Modifier.size(45.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.padding(10.dp), tint = Color(0xFF00ADEF))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Tiến độ", fontSize = 13.sp)
                        Text("4/5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = 0.8f,
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = Color(0xFF00ADEF),
                        trackColor = Color(0xFFF1F5F9)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Còn 1 kiến thức nữa để hoàn thành mục tiêu!", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Thẻ thống kê Chuỗi ngày & Cấp độ
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                AchievementStatCard("Chuỗi ngày", "7 ngày liên tiếp", Icons.Default.LocalFireDepartment, Color(0xFFFFB74D), Modifier.weight(1f))
                AchievementStatCard("Danh hiệu", "Người học thông thái", Icons.Default.EmojiEvents, Color(0xFF4DB6AC), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 3. Mở khóa kiến thức (Course Progress)
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Tiến độ học tập", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Xem tất cả", color = Color(0xFF00ADEF), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(15.dp))
            CourseProgressItem("Hán tự N5", "15/20 bài học", 0.75f, Icons.Default.Lock)
            CourseProgressItem("Sự thật thú vị", "42/50 bài học", 0.84f, Icons.Default.Star)

            Spacer(modifier = Modifier.height(30.dp))

            // 4. Huy hiệu thành tích (Badges)
            Text("Huy hiệu đạt được", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(15.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BadgeItem("Ham Học Hỏi", Icons.Default.MenuBook, Color(0xFF90CAF9))
                BadgeItem("Siêu Tốc Độ", Icons.Default.ElectricBolt, Color(0xFF81C784))
                BadgeItem("Bậc Thầy Chuỗi", Icons.Default.Stars, Color(0xFFFFD54F))
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(label, fontSize = 13.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun CourseProgressItem(title: String, progressText: String, progress: Float, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(45.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFF1F5F9)) {
                Icon(icon, null, modifier = Modifier.padding(10.dp), tint = Color.LightGray)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(progressText, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color(0xFF00ADEF),
                    trackColor = Color(0xFFF1F5F9)
                )
            }
        }
    }
}

@Composable
fun BadgeItem(name: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.2f)
        ) {
            Icon(icon, null, modifier = Modifier.padding(15.dp), tint = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 14.sp)
    }
}