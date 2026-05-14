package com.example.dacs3.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Thanh nền trắng với bo góc trên cực đại (30dp)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp), // Tăng nhẹ chiều cao để chứa thêm chữ
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 20.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp), // Đẩy nội dung lên một chút
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mục Home
                NavigationItem(
                    icon = Icons.Outlined.Home,
                    label = "Home",
                    isSelected = currentRoute == "home"
                ) {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }

                // Mục Library
                NavigationItem(
                    icon = Icons.Outlined.MenuBook,
                    label = "Library",
                    isSelected = currentRoute == "library"
                ) {
                    navController.navigate("library")
                }

                // Khoảng trống ở giữa cho nút Xúc xắc nổi
                Spacer(modifier = Modifier.width(60.dp))

                // Mục Goals
                NavigationItem(
                    icon = Icons.Outlined.EmojiEvents,
                    label = "Goals",
                    isSelected = currentRoute == "goals"
                ) {
                    navController.navigate("goals")
                }

                // Mục Profile
                NavigationItem(
                    icon = Icons.Outlined.Person,
                    label = "Profile",
                    isSelected = currentRoute == "profile"
                ) {
                    navController.navigate("profile")
                }
            }
        }

        // Nút Xúc xắc (Random) nổi bật ở giữa
        FloatingActionButton(
            onClick = { navController.navigate("random") },
            modifier = Modifier
                .size(68.dp)
                .offset(y = (-35).dp), // Đẩy nút lên trên để tạo hiệu ứng nổi
            shape = CircleShape,
            containerColor = Color(0xFF00ADEF), // Màu xanh Cyan rực rỡ
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = "Random",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun NavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) Color(0xFF00ADEF) else Color(0xFF94A3B8)

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(26.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}