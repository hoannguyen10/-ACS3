package com.example.dacs3.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
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
import com.example.dacs3.ui.theme.TealMain
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // --- STATE QUẢN LÝ DỮ LIỆU ĐỘNG ---
    val loggedInUid = auth.currentUser?.uid ?: ""
    var actualDocId by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("Đang tải...") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var streakDays by remember { mutableIntStateOf(0) }
    var totalExp by remember { mutableIntStateOf(0) }
    var lessonCount by remember { mutableIntStateOf(0) }

    // State cho phần Cài đặt ứng dụng (Thực tế nên lưu/đọc từ DataStore hoặc SharedPreferences)
    var isDarkMode by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }

    // 1. Tìm Document ID thực tế dựa trên field uid
    LaunchedEffect(loggedInUid) {
        if (loggedInUid.isNotEmpty()) {
            db.collection("users")
                .whereEqualTo("uid", loggedInUid)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val doc = snapshot.documents.firstOrNull()
                    if (doc != null) actualDocId = doc.id
                }
        }
    }

    // 2. Lấy dữ liệu người dùng khi đã có actualDocId
    LaunchedEffect(actualDocId) {
        if (actualDocId.isNotEmpty()) {
            db.collection("users").document(actualDocId).addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    fullName = snapshot.getString("full_name") ?: "Người dùng"
                    streakDays = (snapshot.getLong("current_streak") ?: 0L).toInt()
                    totalExp = (snapshot.getLong("total_exp") ?: 0L).toInt()
                    val docEmail = snapshot.getString("email")
                    if (!docEmail.isNullOrEmpty()) email = docEmail
                }
            }
            db.collection("users").document(actualDocId).collection("interactions")
                .whereEqualTo("is_seen", true)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) lessonCount = snapshot.documents.size
                }
        }
    }

    Scaffold(
        topBar = { AppTopBar(streakDays = streakDays) },
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
            // --- 1. ẢNH ĐẠI DIỆN VÀ TÊN ---
            Spacer(modifier = Modifier.height(20.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = Color.LightGray,
                    border = BorderStroke(3.dp, Color.White)
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.padding(20.dp), tint = Color.Gray)
                }
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = TealMain,
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.padding(6.dp), tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
            Text(fullName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(email, color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(30.dp))

            // --- 2. THỐNG KÊ HỌC TẬP ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                StatCard(lessonCount.toString(), "Bài học", Modifier.weight(1f))
                StatCard(totalExp.toString(), "Exp", Modifier.weight(1f))
                StatCard(streakDays.toString(), "Ngày học", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- 3. MENU CÁC TÍNH NĂNG ---

            // NHÓM: TÀI KHOẢN
            SettingLabel("Cài đặt tài khoản")
            SettingsGroup {
                // Bên trong ProfileScreen.kt
                ProfileMenuItem(Icons.Default.PersonOutline, "Thông tin cá nhân") {
                    navController.navigate("personal_info")
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                ProfileMenuItem(Icons.Default.LockReset, "Thông tin tài khoản") {
                    navController.navigate("account_info")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // NHÓM: CÀI ĐẶT ỨNG DỤNG (Đã thay thế phần Tiện ích học tập)
            SettingLabel("Cài đặt ứng dụng")
            SettingsGroup {
                ProfileToggleMenuItem(
                    icon = Icons.Default.DarkMode,
                    title = "Giao diện tối (Dark Mode)",
                    isChecked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                ProfileToggleMenuItem(
                    icon = Icons.Default.NotificationsActive,
                    title = "Nhận thông báo",
                    isChecked = isNotificationsEnabled,
                    onCheckedChange = { isNotificationsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // NHÓM: HỖ TRỢ & PHÁP LÝ
            SettingLabel("Hỗ trợ & Bảo mật")
            SettingsGroup {
                ProfileMenuItem(Icons.Default.BugReport, "Báo lỗi & Góp ý") {
                    // TODO: Email Intent
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                ProfileMenuItem(Icons.Default.PrivacyTip, "Chính sách bảo mật") {
                    // TODO: Open URL
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                ProfileMenuItem(Icons.Default.NoAccounts, "Xóa tài khoản", tint = Color.Red) {
                    // TODO: Show Confirm Dialog
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- 4. NÚT ĐĂNG XUẤT ---
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Icon(Icons.Outlined.Logout, null, tint = Color.Red)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- CÁC COMPONENT HỖ TRỢ ---

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
fun SettingLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(start = 5.dp, bottom = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color(0xFF64748B)
    )
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White),
        content = content
    )
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    tint: Color = Color(0xFF64748B),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = if (tint == Color.Red) Color(0xFFFFEBEE) else Color(0xFFF1F5F9)
        ) {
            Icon(icon, null, modifier = Modifier.padding(10.dp), tint = tint)
        }
        Spacer(modifier = Modifier.width(15.dp))
        Text(
            title,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = if (tint == Color.Red) Color.Red else Color.Unspecified
        )
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
fun ProfileToggleMenuItem(
    icon: ImageVector,
    title: String,
    tint: Color = Color(0xFF64748B),
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Icon(icon, null, modifier = Modifier.padding(10.dp), tint = tint)
        }
        Spacer(modifier = Modifier.width(15.dp))
        Text(
            title,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = Color.Unspecified
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TealMain,
                checkedTrackColor = TealMain.copy(alpha = 0.5f)
            )
        )
    }
}