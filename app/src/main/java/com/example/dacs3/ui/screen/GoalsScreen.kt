package com.example.dacs3.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dacs3.ui.component.AppBottomBar
import com.example.dacs3.ui.component.AppTopBar
import com.google.firebase.firestore.FirebaseFirestore

// --- MÀU SẮC GỐC ---
private val GreenDeep = Color(0xFF3C7363)
private val GreenMedium = Color(0xFF84D9BA)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)
private val GoldColor = Color(0xFFFFC107)
private val LockedGray = Color(0xFFD1D5DB)

data class RankUser(val rank: Int, val name: String, val xp: Int, val isCurrentUser: Boolean = false)

// --- DATA CLASS CHO BẢN ĐỒ CẤP ĐỘ ---
data class LevelStep(
    val id: Int,
    val title: String,
    val description: String,
    val xpRequired: Int,
    val icon: ImageVector
)

@Composable
fun GoalsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = "user_test_01"

    // Các state gốc của bạn
    var streakDays by remember { mutableIntStateOf(0) }
    var userTotalExp by remember { mutableIntStateOf(0) }

    // State cho bộ lọc thời gian (giữ nguyên gốc)
    val timeFilters = listOf("Tất cả", "1 tháng", "1 tuần", "3 ngày", "1 ngày")
    var selectedTimeFilter by remember { mutableStateOf(timeFilters[0]) }

    // State điều khiển hiển thị bản đồ cấp độ
    var showLevelMap by remember { mutableStateOf(false) }
    var selectedLevelInfo by remember { mutableStateOf<LevelStep?>(null) }

    // Danh sách cấp độ
    val levelPath = listOf(
        LevelStep(1, "Tân Thủ", "Chào mừng bạn! Đây là bước đầu tiên trong hành trình.", 0, Icons.Default.ChildCare),
        LevelStep(2, "Tập Sự", "Bạn bắt đầu quen với các bài học cơ bản.", 50, Icons.Default.MenuBook),
        LevelStep(3, "Kiên Trì", "Hộp quà tri thức đang chờ đón bạn.", 150, Icons.Default.Inventory2),
        LevelStep(4, "Thông Thái", "Kỹ năng của bạn đã được cải thiện đáng kể.", 350, Icons.Default.Psychology),
        LevelStep(5, "Bậc Thầy", "Bạn là một người học xuất sắc!", 700, Icons.Default.Stars)
    )

    // Dữ liệu Leaderboard gốc (cập nhật điểm của bạn bằng userTotalExp)
    val mockLeaderboard = listOf(
        RankUser(1, "Hải Nam", 5420),
        RankUser(2, "Minh Anh", 4300),
        RankUser(3, "Bạn (user_test)", userTotalExp, isCurrentUser = true),
        RankUser(4, "Thảo Vy", 2900)
    ).sortedByDescending { it.xp }
    val yourRank = mockLeaderboard.indexOfFirst { it.isCurrentUser } + 1

    LaunchedEffect(Unit) {
        val userRef = db.collection("users").document(currentUserId)
        userRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                streakDays = (snapshot.getLong("current_streak") ?: 0L).toInt()
                userTotalExp = (snapshot.getLong("total_exp") ?: 0L).toInt()
            }
        }
    }

    val currentLevelNum = levelPath.lastOrNull { userTotalExp >= it.xpRequired }?.id ?: 1

    // Box to lớn nhất để chứa Scaffold (nền) và Level Map (lớp phủ)
    Box(modifier = Modifier.fillMaxSize()) {

        // --- 1. GIAO DIỆN GỐC CỦA BẠN (ĐƯỢC GIỮ NGUYÊN 100%) ---
        Scaffold(
            topBar = { AppTopBar(streakDays = streakDays) },
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
                Spacer(modifier = Modifier.height(15.dp))

                // --- BẢNG XẾP HẠNG THU NHỎ ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBlueBg)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Bảng xếp hạng tuần", color = GreenDeep.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Hạng của bạn: $yourRank", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextBlack)
                            }
                            Surface(modifier = Modifier.size(45.dp), shape = RoundedCornerShape(12.dp), color = Color.White) {
                                Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.padding(10.dp), tint = Color(0xFFF59E0B))
                            }
                        }

                        Spacer(modifier = Modifier.height(15.dp))


                        mockLeaderboard.take(4).forEachIndexed { index, user ->
                            RankItemCard(user.copy(rank = index + 1))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                // --- 2 THẺ THÔNG SỐ ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    AchievementStatCard(
                        label = "Chuỗi ngày", value = "$streakDays ngày",
                        icon = Icons.Default.LocalFireDepartment, color = Color(0xFFFF5722),
                        modifier = Modifier.weight(1f)
                    )
                    AchievementStatCard(
                        label = "Cấp bậc", value = "Cấp $currentLevelNum",
                        icon = Icons.Default.Stars, color = Color(0xFFF59E0B),
                        // THÊM SỰ KIỆN CLICK VÀO ĐÂY ĐỂ MỞ BẢN ĐỒ CẤP ĐỘ
                        modifier = Modifier.weight(1f).clickable { showLevelMap = true }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // --- THỐNG KÊ HỌC TẬP (Giữ nguyên gốc) ---
                Text("Thống kê học tập", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
                Spacer(modifier = Modifier.height(15.dp))

                // Bộ lọc thời gian (Giữ nguyên cấu trúc gốc)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeFilters.forEach { filter ->
                        val isSelected = selectedTimeFilter == filter
                        Surface(
                            modifier = Modifier.clickable { selectedTimeFilter = filter },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) GreenDeep else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) GreenDeep else Color.LightGray)
                        ) {
                            Text(
                                text = filter,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    LearningStatBox("Từ vựng", "1,250", "từ", Icons.Default.Translate, Modifier.weight(1f))
                    LearningStatBox("Thời gian", "15", "giờ", Icons.Default.AccessTime, Modifier.weight(1f))
                }

                // Hàng 2 (2 ô sau - Bạn có thể sửa tên/icon tùy ý)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    LearningStatBox("Bài học", "48", "bài", Icons.Default.MenuBook, Modifier.weight(1f))
                    LearningStatBox("Độ chính xác", "92", "%", Icons.Default.FactCheck, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(30.dp))

                // --- HUY HIỆU NỔI BẬT (Giữ nguyên gốc) ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Huy hiệu nổi bật", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
                    Text("Xem tất cả", color = GreenDeep, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BadgeItem("Chăm chỉ", Icons.Default.WorkspacePremium, Color(0xFFFFC107))
                    BadgeItem("Cú đêm", Icons.Default.DarkMode, Color(0xFF673AB7))
                    BadgeItem("Hoàn hảo", Icons.Default.Verified, Color(0xFF4CAF50))
                    BadgeItem("Tốc độ", Icons.Default.Bolt, Color(0xFFF44336))
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // --- 2. LỚP PHỦ BẢN ĐỒ CẤP ĐỘ ZIGZAG (CHỈ HIỆN KHI BẤM VÀO CẤP BẬC) ---
        if (showLevelMap) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White // Che kín toàn màn hình
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header có nút Back
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showLevelMap = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = GreenDeep)
                        }
                        Text("Lộ trình học tập", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextBlack)
                    }

                    // Vùng cuộn chứa bản đồ Zigzag
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 20.dp, bottom = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        levelPath.forEachIndexed { index, level ->
                            val isUnlocked = userTotalExp >= level.xpRequired
                            val isNext = !isUnlocked && (index == 0 || userTotalExp >= levelPath[index - 1].xpRequired)

                            // Tạo độ lệch Zigzag: Cấp 1 trái, Cấp 2 phải, Cấp 3 trái...
                            val xOffset = when {
                                index % 4 == 1 -> (-80).dp // Lệch trái
                                index % 4 == 3 -> (80).dp  // Lệch phải
                                else -> 0.dp               // Ở giữa
                            }

                            LevelNode(
                                level = level,
                                isUnlocked = isUnlocked,
                                isNext = isNext,
                                modifier = Modifier
                                    .offset(x = xOffset)
                                    .padding(vertical = 20.dp),
                                onClick = { selectedLevelInfo = level }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG GIẢI THÍCH KHI BẤM VÀO TỪNG CẤP ĐỘ ---
    if (selectedLevelInfo != null) {
        val isUnlocked = userTotalExp >= selectedLevelInfo!!.xpRequired
        AlertDialog(
            onDismissRequest = { selectedLevelInfo = null },
            title = { Text(selectedLevelInfo!!.title, fontWeight = FontWeight.Black, color = GreenDeep) },
            text = {
                Column {
                    Text(selectedLevelInfo!!.description, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Yêu cầu: ${selectedLevelInfo!!.xpRequired} EXP",
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) GreenDeep else Color.Red
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLevelInfo = null }) { Text("Đóng", fontWeight = FontWeight.Bold, color = GreenDeep) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

// --- HÀM VẼ TỪNG NÚT CẤP ĐỘ ZIGZAG ---
@Composable
fun LevelNode(level: LevelStep, isUnlocked: Boolean, isNext: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        if (isNext) {
            Surface(color = GreenDeep, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                Text("BẮT ĐẦU", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        Surface(
            modifier = Modifier.size(85.dp).clickable { onClick() },
            shape = CircleShape,
            color = if (isUnlocked) GreenMedium else Color.White,
            border = BorderStroke(4.dp, if (isUnlocked) GreenDeep else if (isNext) GreenDeep.copy(0.5f) else LockedGray),
            shadowElevation = if (isUnlocked) 6.dp else 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    level.icon, null,
                    modifier = Modifier.size(35.dp),
                    tint = if (isUnlocked) Color.White else LockedGray
                )
            }
        }
    }
}

@Composable
fun AchievementStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .heightIn(min = 130.dp) // Dùng heightIn để thẻ có thể tự co giãn nếu chữ lớn
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp) // Thu nhỏ padding để có thêm khoảng trống
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(8.dp)) // Thu nhỏ icon một chút xíu
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 15.sp, textAlign = TextAlign.Center, color = TextBlack, maxLines = 1)
        }
    }
}

@Composable
fun RankItemCard(user: RankUser) {
    val bgColor = if (user.isCurrentUser) Color.White else Color.Transparent
    val textColor = if (user.isCurrentUser) GreenDeep else TextBlack
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#${user.rank}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (user.rank in 1..3) Color(0xFFF59E0B) else Color.Gray,
                modifier = Modifier.width(30.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(user.name, fontWeight = if (user.isCurrentUser) FontWeight.Black else FontWeight.Medium, color = textColor)
        }
        Text("${user.xp} XP", fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun LearningStatBox(title: String, value: String, unit: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = GreenDeep.copy(0.7f), modifier = Modifier.size(20.dp))
                Text(title, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp), color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = TextBlack)
                Text(unit, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp, bottom = 3.dp), color = Color.Gray)
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
            border = BorderStroke(2.dp, color.copy(alpha = 0.3f))
        ) {
            Icon(icon, null, modifier = Modifier.padding(16.dp), tint = color)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(name, fontSize = 12.sp, textAlign = TextAlign.Center, color = TextBlack, fontWeight = FontWeight.Medium)
    }
}