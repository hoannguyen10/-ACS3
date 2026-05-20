package com.example.dacs3.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import com.example.dacs3.model.LevelStep
import com.example.dacs3.ui.component.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

// --- MÀU SẮC CHỦ ĐẠO ---
private val GreenDeep = Color(0xFF3C7363)
private val GoldColor = Color(0xFFF59E0B)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)
private val LockedGray = Color(0xFFE5E7EB)

// --- DATA MODELS NỘI BỘ ---
data class RankUser(val rank: Int, val name: String, val xp: Int, val isCurrentUser: Boolean = false)
data class DayStat(val dayLabel: String, val lessonCount: Int, val quizCount: Int)

@Composable
fun GoalsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = "user_test_01" // Nên lấy từ Firebase Auth thực tế

    var streakDays by remember { mutableIntStateOf(0) }
    var userTotalExp by remember { mutableIntStateOf(0) }

    var rawHistoryLogs by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }
    var rawLessons by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }
    var rawQuizzes by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }

    var weeklyStats by remember { mutableStateOf<List<DayStat>>(emptyList()) }
    var isLoadingStats by remember { mutableStateOf(true) }

    var showLevelMap by remember { mutableStateOf(false) }
    var selectedLevelInfo by remember { mutableStateOf<LevelStep?>(null) }

    // Sử dụng dữ liệu tập trung đã tách
    val levelPath = LevelPathData
    val fullDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dayLabelFormat = remember { SimpleDateFormat("EE", Locale("vi", "VN")) }

    // 1. Lắng nghe dữ liệu người dùng
    LaunchedEffect(Unit) {
        db.collection("users").document(currentUserId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                streakDays = (snapshot.getLong("current_streak") ?: 0L).toInt()
                userTotalExp = (snapshot.getLong("total_exp") ?: 0L).toInt()
            }
        }
    }

    // 2. Truy vấn dữ liệu hoạt động trong 7 ngày
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
        val startTime = Timestamp(calendar.time)

        // Lấy bài học mới
        db.collection("users").document(currentUserId).collection("interactions")
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .addSnapshotListener { s, _ -> rawLessons = s?.documents ?: emptyList() }

        // Lấy lịch sử xem lại
        db.collection("users").document(currentUserId).collection("history_logs")
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .addSnapshotListener { s, _ -> rawHistoryLogs = s?.documents ?: emptyList() }

        // Lấy lịch sử Quiz
        db.collection("users").document(currentUserId).collection("quizz_question_history")
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .addSnapshotListener { s, _ -> rawQuizzes = s?.documents ?: emptyList() }
    }

    // 3. Xử lý dữ liệu biểu đồ
    LaunchedEffect(rawLessons, rawHistoryLogs, rawQuizzes) {
        val lessonMap = mutableMapOf<String, Int>()
        val quizMap = mutableMapOf<String, Int>()

        for (i in 0..6) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = fullDateFormat.format(cal.time)
            lessonMap[key] = 0
            quizMap[key] = 0
        }

        (rawLessons + rawHistoryLogs).forEach { doc ->
            doc.getTimestamp("timestamp")?.let { ts ->
                val key = fullDateFormat.format(ts.toDate())
                if (lessonMap.containsKey(key)) lessonMap[key] = lessonMap[key]!! + 1
            }
        }

        rawQuizzes.forEach { doc ->
            doc.getTimestamp("timestamp")?.let { ts ->
                val key = fullDateFormat.format(ts.toDate())
                if (quizMap.containsKey(key)) quizMap[key] = quizMap[key]!! + 1
            }
        }

        weeklyStats = lessonMap.keys.sorted().map { key ->
            val date = fullDateFormat.parse(key)
            DayStat(
                dayLabel = dayLabelFormat.format(date!!).uppercase(),
                lessonCount = lessonMap[key] ?: 0,
                quizCount = quizMap[key] ?: 0
            )
        }
        isLoadingStats = false
    }

    val currentLevelNum = levelPath.lastOrNull { userTotalExp >= it.xpRequired }?.id ?: 1

    Box(modifier = Modifier.fillMaxSize()) {
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

                RankPreviewCard(userTotalExp)

                Spacer(modifier = Modifier.height(25.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    AchievementStatCard(
                        label = "Chuỗi ngày", value = "$streakDays ngày",
                        icon = Icons.Default.LocalFireDepartment, color = Color(0xFFFF5722),
                        modifier = Modifier.weight(1f)
                    )
                    AchievementStatCard(
                        label = "Cấp bậc", value = "Cấp $currentLevelNum",
                        icon = Icons.Default.Stars, color = GoldColor,
                        modifier = Modifier.weight(1f).clickable { showLevelMap = true }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text("Tiến độ học tập", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(GreenDeep, CircleShape))
                    Text(" Bài học", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(8.dp).background(GoldColor, CircleShape))
                    Text(" Quiz", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(15.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBlueBg.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        if (isLoadingStats) {
                            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GreenDeep)
                            }
                        } else {
                            StackedWeeklyChart(stats = weeklyStats)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

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

        // Overlay lộ trình (đã tách)
        if (showLevelMap) {
            LevelRoadmapOverlay(
                streakDays = streakDays,
                userTotalExp = userTotalExp,
                levelPath = levelPath,
                onBack = { showLevelMap = false },
                onSelect = { selectedLevelInfo = it }
            )
        }
    }

    // Dialog chi tiết (đã tách)
    if (selectedLevelInfo != null) {
        LevelDetailDialog(selectedLevelInfo!!, userTotalExp) { selectedLevelInfo = null }
    }
}

// --- CÁC COMPONENT HỖ TRỢ TRONG MÀN HÌNH ---

@Composable
fun StackedWeeklyChart(stats: List<DayStat>) {
    val maxVal = stats.maxOfOrNull { it.lessonCount + it.quizCount }?.coerceAtLeast(1) ?: 1

    Row(
        modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        stats.forEach { stat ->
            val total = stat.lessonCount + stat.quizCount
            val barHeightPercent = total.toFloat() / maxVal
            val animatedHeight by animateFloatAsState(targetValue = barHeightPercent, animationSpec = tween(1000))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                if (total > 0) {
                    Text(text = total.toString(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    if (total > 0) {
                        Column(
                            modifier = Modifier.fillMaxHeight(animatedHeight.coerceAtLeast(0.05f)).width(24.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        ) {
                            if (stat.quizCount > 0) {
                                Box(modifier = Modifier.weight(stat.quizCount.toFloat()).fillMaxWidth().background(GoldColor), contentAlignment = Alignment.TopCenter) {
                                    Text(text = stat.quizCount.toString(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (stat.lessonCount > 0) {
                                Box(modifier = Modifier.weight(stat.lessonCount.toFloat()).fillMaxWidth().background(GreenDeep), contentAlignment = Alignment.TopCenter) {
                                    Text(text = stat.lessonCount.toString(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.height(6.dp).width(24.dp).background(LockedGray, CircleShape))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val isToday = stat.dayLabel == SimpleDateFormat("EE", Locale("vi", "VN")).format(Date()).uppercase()
                Text(text = stat.dayLabel, fontSize = 11.sp, color = if (isToday) GreenDeep else Color.Gray, fontWeight = if (isToday) FontWeight.Black else FontWeight.Medium)
            }
        }
    }
}

@Composable
fun RankPreviewCard(userTotalExp: Int) {
    val mockLeaderboard = listOf(
        RankUser(1, "Hải Nam", 5420),
        RankUser(2, "Minh Anh", 4300),
        RankUser(3, "Bạn", userTotalExp, isCurrentUser = true),
        RankUser(4, "Thảo Vy", 2900)
    ).sortedByDescending { it.xp }

    val yourRank = mockLeaderboard.indexOfFirst { it.isCurrentUser } + 1

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Bảng xếp hạng tuần", color = GreenDeep.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Hạng của bạn: $yourRank", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextBlack)
                }
                Surface(modifier = Modifier.size(45.dp), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.padding(10.dp), tint = GoldColor)
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            mockLeaderboard.take(4).forEachIndexed { index, user -> RankItemCard(user.copy(rank = index + 1)) }
        }
    }
}

@Composable
fun RankItemCard(user: RankUser) {
    val bgColor = if (user.isCurrentUser) Color.White else Color.Transparent
    val textColor = if (user.isCurrentUser) GreenDeep else TextBlack
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).padding(vertical = 8.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("#${user.rank}", fontWeight = FontWeight.Bold, color = if (user.rank in 1..3) GoldColor else Color.Gray, modifier = Modifier.width(30.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(user.name, fontWeight = if (user.isCurrentUser) FontWeight.Black else FontWeight.Medium, color = textColor)
        }
        Text("${user.xp} XP", fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun AchievementStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier.heightIn(min = 100.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF1F5F9)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) { Icon(icon, null, tint = color, modifier = Modifier.padding(8.dp)) }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextBlack)
        }
    }
}

@Composable
fun BadgeItem(name: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = color.copy(alpha = 0.1f), border = BorderStroke(2.dp, color.copy(alpha = 0.2f))) { Icon(icon, null, modifier = Modifier.padding(16.dp), tint = color) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 11.sp, textAlign = TextAlign.Center, color = TextBlack, fontWeight = FontWeight.Medium)
    }
}