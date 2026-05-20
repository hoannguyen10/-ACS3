package com.example.dacs3.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dacs3.R // Đảm bảo đúng thư mục R dự án của bạn
import com.example.dacs3.ui.component.AppBottomBar
import com.example.dacs3.ui.component.AppTopBar
import com.google.firebase.firestore.FirebaseFirestore

// Bảng màu đồng bộ
private val GreenDeep = Color(0xFF3C7363)
private val GreenMedium = Color(0xFF84D9BA)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)

@Composable
fun HomeScreen(
    navController: NavHostController,
    onLessonClick: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = "user_test_01"

    var totalExp by remember { mutableIntStateOf(0) }
    var streakDays by remember { mutableIntStateOf(0) }
    var unlockedCount by remember { mutableIntStateOf(0) }


    LaunchedEffect(Unit) {
        // 1. Lấy Tổng EXP và Streak từ Firebase
        db.collection("users").document(currentUserId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                totalExp = (snapshot.getLong("total_exp") ?: 0L).toInt()
                streakDays = (snapshot.getLong("current_streak") ?: 0L).toInt()
            }
        }

        // 2. Lấy số lượng thẻ đã khám phá
        db.collection("users").document(currentUserId).collection("interactions")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    unlockedCount = snapshot.documents.count { it.getBoolean("is_seen") == true }
                }
            }
    }

    // --- CẬP NHẬT LẠI LOGIC TÍNH CẤP ĐỘ VÀ TÊN BẬC ---
    val currentLevel = when {
        totalExp >= 700 -> 5
        totalExp >= 350 -> 4
        totalExp >= 150 -> 3
        totalExp >= 50 -> 2
        else -> 1
    }

    val rankName = when {
        totalExp >= 700 -> "Bậc Thầy"
        totalExp >= 350 -> "Thông Thái"
        totalExp >= 150 -> "Kiên Trì"
        totalExp >= 50 -> "Tập Sự"
        else -> "Tân Thủ"
    }

    Scaffold(
        topBar = {
            AppTopBar(
                streakDays = streakDays,
                onNotificationClick = { /* Điều hướng đến thông báo */ }
            )
        },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Chào mừng & Thông tin cấp độ (Sử dụng cấp độ và tên bậc động)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Chào mừng bạn trở lại,", color = Color.Gray, fontSize = 15.sp)
                    // CẬP NHẬT BIẾN rankName VÀO ĐÂY
                    Text(rankName, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cấp độ", color = Color.Gray, fontSize = 14.sp)
                    Text("$currentLevel", fontWeight = FontWeight.Black, fontSize = 18.sp, color = GreenDeep)
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // ... (Các phần code phía dưới giữ nguyên)

            // 2. VÒNG QUAY MAY MẮN
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { navController.navigate("random") },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Background Vòng quay",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        GreenMedium.copy(alpha = 0.6f),
                                        GreenDeep.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(Icons.Default.Casino, null, modifier = Modifier.padding(10.dp), tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        Text("HỘP QUÀ TRI THỨC", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Chạm để mở khóa một điều thú vị hôm nay!", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. THỐNG KÊ (Thay Chuỗi ngày bằng Tổng EXP)
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.spacedBy(15.dp)) {
                SmallStatCard("💡 $unlockedCount", "Đã khám phá", Modifier.weight(1f))
                SmallStatCard("⭐ $totalExp", "Tổng EXP", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(25.dp))

            // 4. MINI QUIZ
            MiniQuizCard(
                onClick = {
                    navController.navigate("miniquiz")
                }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SmallStatCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlueBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = GreenDeep)
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun MiniQuizCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.quiz),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GreenMedium.copy(alpha = 0.5f),
                                GreenDeep.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "MINI QUIZ",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = GreenDeep
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Thử thách trí não",
                    color = GreenDeep.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}