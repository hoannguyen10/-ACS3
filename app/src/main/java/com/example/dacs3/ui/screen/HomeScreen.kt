package com.example.dacs3.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dacs3.ui.component.AppBottomBar
import com.example.dacs3.ui.component.AppTopBar

@Composable
fun HomeScreen(
    navController: NavHostController,
    onLessonClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                streakDays = 7,
                onNotificationClick = { /* Điều hướng đến màn thông báo */ }
            )
        },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = Color(0xFFF8FAFF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Chào mừng & Thông tin cấp độ
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Chào mừng bạn trở lại,", color = Color.Gray, fontSize = 15.sp)
                    Text("Người học thông thái", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cấp độ", color = Color.Gray, fontSize = 14.sp)
                    Text("8", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A73E8))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Thẻ mục tiêu hàng ngày
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Mục tiêu hôm nay", fontWeight = FontWeight.Medium, color = Color.DarkGray)
                        Text("4/5 bài học", fontWeight = FontWeight.Bold, color = Color(0xFF00ADEF))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = 0.8f,
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = Color(0xFF00ADEF),
                        trackColor = Color(0xFFE3F2FD)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Chỉ còn 1 bài học nữa là hoàn thành mục tiêu!", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. VÒNG QUAY MAY MẮN (Dành cho kiến thức ngẫu nhiên)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { navController.navigate("random") },
                shape = RoundedCornerShape(30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(Icons.Default.Casino, null, modifier = Modifier.padding(10.dp), tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        Text("VÒNG QUAY KIẾN THỨC", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("Khám phá kiến thức ngẫu nhiên ngay", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Thống kê nhanh
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.spacedBy(15.dp)) {
                SmallStatCard("🍴 25", "Bài đã học", Modifier.weight(1f))
                SmallStatCard("📈 3", "Lên cấp", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(25.dp))

            // 6. Mục tiếp tục học tập
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Tiếp tục học tập", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Xem tất cả", color = Color(0xFF00ADEF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(15.dp))

            // Bài học đang dang dở ví dụ
            ContinueLessonCard("Khoa học", "Vật lý lượng tử 101", "Khám phá thế giới của các hạt dưới nguyên tử.")

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SmallStatCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun ContinueLessonCard(tag: String, title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp)) {
                Text(tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF1A73E8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(desc, color = Color.Gray, fontSize = 13.sp)
        }
    }
}