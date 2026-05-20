package com.example.dacs3.ui.screen

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dacs3.ui.component.AppBottomBar
import com.example.dacs3.ui.component.AppTopBar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// Bảng màu đồng bộ từ RegisterScreen
private val GreenDeep = Color(0xFF3C7363)
private val GreenMedium = Color(0xFF84D9BA)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomScreen(
    onBack: () -> Unit,
    navController: NavController,
    onResultClick: (String) -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val topics = listOf("Khoa học", "Khác", "Lịch Sử", "Sinh học", "Văn hóa", "Văn Hóa", "Địa lý")
    val currentUserId = "user_test_01"

    // State quản lý số ngày chuỗi để hiển thị
    var streakDays by remember { mutableIntStateOf(0) }

    var randomDocument by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var rotated by remember { mutableStateOf(false) }
    var currentTopicName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "rotation"
    )

    // Lấy chuỗi hiện tại khi màn hình vừa mở
    LaunchedEffect(Unit) {
        val userRef = db.collection("users").document(currentUserId)
        userRef.get().addOnSuccessListener { snapshot ->
            streakDays = (snapshot.getLong("current_streak") ?: 0L).toInt()
        }
    }

    // Hàm duy trì/tăng chuỗi
    val maintainStreak = {
        val userRef = db.collection("users").document(currentUserId)
        val todayEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

        userRef.get().addOnSuccessListener { snapshot ->
            val lastCheckIn = snapshot.getLong("last_check_in_day") ?: 0L
            val currentStreak = snapshot.getLong("current_streak") ?: 0L

            when {
                todayEpochDay == lastCheckIn -> {
                    streakDays = currentStreak.toInt()
                }
                todayEpochDay == lastCheckIn + 1 -> {
                    val newStreak = currentStreak + 1
                    userRef.update(
                        mapOf(
                            "last_check_in_day" to todayEpochDay,
                            "current_streak" to newStreak
                        )
                    )
                    streakDays = newStreak.toInt()
                }
                else -> {
                    userRef.set(
                        mapOf(
                            "last_check_in_day" to todayEpochDay,
                            "current_streak" to 1L
                        ),
                        SetOptions.merge()
                    )
                    streakDays = 1
                }
            }
        }
    }

    fun syncUserInteraction(originId: String, topic: String, title: String) {
        val userRef = db.collection("users").document(currentUserId)
        val interactionRef = userRef.collection("interactions").document(originId)

        interactionRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // --- TRƯỜNG HỢP 1: THẺ ĐÃ TỒN TẠI (THẺ CŨ) ---
                // Cập nhật trạng thái hiển thị nút lưu
                isSaved = snapshot.getBoolean("is_saved") ?: false

                // Chỉ khi thẻ đã tồn tại mới ghi nhật ký vào history_logs
                val logData = hashMapOf(
                    "origin_id" to originId,
                    "topic" to topic,
                    "title" to title,
                    "action" to "view",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                userRef.collection("history_logs").add(logData)
                    .addOnSuccessListener {
                        android.util.Log.d("Firestore", "Thẻ cũ - Đã thêm vào history_logs: $originId")
                    }
            } else {
                // --- TRƯỜNG HỢP 2: THẺ MỚI HOÀN TOÀN ---
                // Chỉ lưu vào interactions, KHÔNG lưu vào history_logs
                val interactionData = hashMapOf(
                    "origin_id" to originId,
                    "topic" to topic,
                    "title" to title,
                    "is_seen" to true,
                    "is_saved" to false,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                interactionRef.set(interactionData)
                isSaved = false
                android.util.Log.d("Firestore", "Thẻ mới - Đã lưu vào interactions: $originId")
            }
        }
    }

    // Random thẻ mới
    val pickRandomFromSubCollection: () -> Unit = {
        isLoading = true
        rotated = false
        val lastDocId = randomDocument?.id
        isSaved = false


        // KÍCH HOẠT DUY TRÌ CHUỖI KHI BẤM RANDOM
        maintainStreak()

        val randomTopic = topics.random()
        currentTopicName = randomTopic

        db.collection("knowledge_snacks")
            .document("Kiến thức")
            .collection(randomTopic)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val filteredDocs = docs.documents.filter { it.id != lastDocId }
                    val selected = if (filteredDocs.isNotEmpty()) filteredDocs.random() else docs.documents.random()
                    randomDocument = selected

                    // Gọi hàm đồng bộ tương tác với user
                    val title = selected.getString("title") ?: "Chưa có tiêu đề"
                    syncUserInteraction(selected.id, randomTopic, title)
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Lỗi kết nối Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    // Gom chung chức năng Lưu và Bỏ lưu vào 1 hàm Toggle
    val toggleSaveKnowledge = {
        val doc = randomDocument
        if (doc != null && !isSaving) {
            isSaving = true
            val targetState = !isSaved

            db.collection("users").document(currentUserId)
                .collection("interactions").document(doc.id)
                .update("is_saved", targetState)
                .addOnSuccessListener {
                    isSaved = targetState
                    isSaving = false
                    Toast.makeText(context, if (isSaved) "Đã lưu" else "Đã bỏ lưu", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    isSaving = false
                    Toast.makeText(context, "Lỗi khi lưu", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = { AppTopBar(streakDays = streakDays) },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (randomDocument == null) {
                NewInitialView(isLoading = isLoading, onBack = onBack, onRandom = pickRandomFromSubCollection)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        FlashcardView(
                            rotation = rotation,
                            topic = currentTopicName,
                            title = randomDocument?.getString("title") ?: "Chưa có tiêu đề",
                            content = randomDocument?.getString("content") ?: "",
                            detail = randomDocument?.getString("detail") ?: "",
                            isSaved = isSaved,
                            onFlip = { rotated = !rotated }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionButton(
                            text = if (isSaved) "Đã lưu" else "Lưu thẻ",
                            icon = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.StarBorder,
                            containerColor = if (isSaved) Color(0xFFE8F5E9) else LightBlueBg,
                            contentColor = if (isSaved) Color(0xFF4CAF50) else GreenDeep,
                            modifier = Modifier.weight(1f),
                            onClick = { toggleSaveKnowledge() }
                        )

                        Button(
                            onClick = { pickRandomFromSubCollection() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenDeep),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TIẾP THEO", fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
fun FlashcardView(
    rotation: Float,
    topic: String,
    title: String,
    content: String,
    detail: String,
    isSaved: Boolean,
    onFlip: () -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .clickable { onFlip() }
            .shadow(20.dp, RoundedCornerShape(32.dp))
            .background(Color.White, RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp).fillMaxSize()
            ) {
                Text(
                    text = topic.uppercase(),
                    fontSize = 13.sp,
                    color = GreenDeep,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = TextBlack,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = content,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(Icons.Default.TouchApp, contentDescription = null, tint = GreenMedium)
                Text("Chạm để xem giải thích", fontSize = 12.sp, color = Color.Gray)
            }

            if (isSaved) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).size(28.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("GIẢI THÍCH CHI TIẾT", fontSize = 14.sp, color = GreenDeep, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = detail,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = TextBlack,
                    lineHeight = 30.sp
                )
            }
        }
    }
}

@Composable
fun NewInitialView(isLoading: Boolean, onBack: () -> Unit, onRandom: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "scale")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start).padding(16.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = GreenDeep)
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(180.dp).scale(scale).background(LightBlueBg, CircleShape))
            Surface(modifier = Modifier.size(120.dp).shadow(15.dp, CircleShape), color = Color.White, shape = CircleShape) {
                Icon(Icons.Default.Redeem, contentDescription = null, modifier = Modifier.padding(30.dp).fillMaxSize(), tint = GreenDeep)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text("Kiến thức bí ẩn", fontSize = 26.sp, color = TextBlack, fontWeight = FontWeight.Black)
        Text("Khám phá ngay điều mới lạ cùng MindSnack", color = Color.Gray, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(60.dp))

        if (isLoading) {
            CircularProgressIndicator(color = GreenDeep)
        } else {
            Button(
                onClick = onRandom,
                modifier = Modifier.fillMaxWidth(0.7f).height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenDeep),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("MỞ QUÀ NGẪU NHIÊN", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(modifier = Modifier.weight(1.2f))
    }
}

@Composable
fun StatusChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextBlack)
        }
    }
}

@Composable
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color, contentColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}