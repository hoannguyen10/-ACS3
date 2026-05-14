package com.example.dacs3.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dacs3.ui.component.AppBottomBar
import com.example.dacs3.ui.component.AppTopBar
import com.example.dacs3.ui.theme.TealMain

@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        topBar = { AppTopBar(streakDays = 7) }, // Gắn TopBar ở đây
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = Color(0xFFF8FAFB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Ảnh Đại Diện và Tên
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = Color.LightGray,
                    border = BorderStroke(3.dp, Color.White)
                ) {
                    // Thay bằng Image thực tế của bạn
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(20.dp),
                        tint = Color.Gray
                    )
                }
                // Nút Edit ảnh
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = TealMain,
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.padding(6.dp), tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
            Text("Nguyễn Thị Thúy Hoan", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("hoanntt.24it@vku.udn.vn", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(30.dp))

            // 2. Thống kê học tập (Stats)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                StatCard("12", "Bài học", Modifier.weight(1f))
                StatCard("450", "Exp", Modifier.weight(1f))
                StatCard("5", "Ngày học", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 3. Danh sách cài đặt (Settings List)
            Text(
                "Cài đặt tài khoản",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
            ) {
                ProfileMenuItem(Icons.Default.PersonOutline, "Thông tin cá nhân")
                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                ProfileMenuItem(Icons.Default.History, "Lịch sử học tập")
                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                ProfileMenuItem(Icons.Default.Settings, "Cài đặt ứng dụng")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Nút Đăng xuất
            Button(
                onClick = { /* Xử lý Logout */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TealMain)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Icon(icon, null, modifier = Modifier.padding(8.dp), tint = Color(0xFF64748B))
        }
        Spacer(modifier = Modifier.width(15.dp))
        Text(title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}