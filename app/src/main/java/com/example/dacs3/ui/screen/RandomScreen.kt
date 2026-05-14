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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomScreen(
    onBack: () -> Unit,
    navController: NavController,
    onResultClick: (String) -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    // Danh sách chủ đề khớp với Admin Panel
    val topics = listOf("Lịch Sử", "Khoa học", "Văn Hóa")
    val currentUserId = "user_test_01"

    var randomDocument by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var rotated by remember { mutableStateOf(false) }
    var currentTopicName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    var savedDocIdInDb by remember { mutableStateOf<String?>(null) }

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "rotation"
    )

    // Kiểm tra bài này đã được người dùng lưu chưa
    fun checkIsSaved(originId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("saved_knowledge")
            .whereEqualTo("origin_id", originId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    isSaved = true
                    savedDocIdInDb = snapshot.documents[0].id
                } else {
                    isSaved = false
                    savedDocIdInDb = null
                }
            }
    }

    val pickRandomFromSubCollection: () -> Unit = {
        isLoading = true
        rotated = false
        val lastDocId = randomDocument?.id
        isSaved = false
        savedDocIdInDb = null

        val randomTopic = topics.random()
        currentTopicName = randomTopic

        db.collection("knowledge_snacks")
            .document("Kiến thức")
            .collection(randomTopic)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    // Tránh lấy trùng bài vừa xem
                    val filteredDocs = docs.documents.filter { it.id != lastDocId }
                    val selected = if (filteredDocs.isNotEmpty()) filteredDocs.random() else docs.documents.random()
                    randomDocument = selected
                    checkIsSaved(selected.id)
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, "Lỗi kết nối Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    val saveKnowledge = {
        val doc = randomDocument
        if (doc != null && !isSaving) {
            isSaving = true
            val data = hashMapOf(
                "title" to (doc.getString("title") ?: ""),
                "content" to (doc.getString("content") ?: ""),
                "detail" to (doc.getString("detail") ?: ""),
                "topic" to currentTopicName,
                "origin_id" to doc.id,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            db.collection("users").document(currentUserId).collection("saved_knowledge")
                .add(data)
                .addOnSuccessListener {
                    savedDocIdInDb = it.id
                    isSaving = false
                    isSaved = true
                    Toast.makeText(context, "Đã lưu thành công", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val unSaveKnowledge = {
        val docId = savedDocIdInDb
        if (docId != null && !isSaving) {
            isSaving = true
            db.collection("users").document(currentUserId).collection("saved_knowledge")
                .document(docId).delete()
                .addOnSuccessListener {
                    isSaving = false
                    isSaved = false
                    savedDocIdInDb = null
                    Toast.makeText(context, "Đã bỏ lưu", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = { AppTopBar(streakDays = 12) },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (randomDocument == null) {
                NewInitialView(isLoading = isLoading, onBack = onBack, onRandom = pickRandomFromSubCollection)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(icon = Icons.Default.LocalFireDepartment, text = "12 Day Streak", color = Color(0xFFFF5722))
                        StatusChip(icon = Icons.AutoMirrored.Filled.MenuBook, text = currentTopicName, color = Color(0xFF4A90E2))
                    }

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
                            containerColor = if (isSaved) Color(0xFFE8F5E9) else Color(0xFFFFF9C4),
                            contentColor = if (isSaved) Color(0xFF4CAF50) else Color(0xFFFBC02D),
                            modifier = Modifier.weight(1f),
                            onClick = { if (isSaved) unSaveKnowledge() else saveKnowledge() }
                        )

                        Button(
                            onClick = { pickRandomFromSubCollection() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tiếp theo", fontWeight = FontWeight.Bold)
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
            .shadow(25.dp, RoundedCornerShape(32.dp))
            .background(Color.White, RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // --- MẶT TRƯỚC ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp).fillMaxSize()
            ) {
                Text(
                    text = topic.uppercase(),
                    fontSize = 13.sp,
                    color = Color(0xFF4A90E2),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = content,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color.LightGray)
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
            // --- MẶT SAU ---
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("GIẢI THÍCH CHI TIẾT", fontSize = 14.sp, color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = detail,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF334155),
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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start).padding(16.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(180.dp).scale(scale).background(Color(0xFFE3F2FD), CircleShape))
            Surface(modifier = Modifier.size(120.dp).shadow(15.dp, CircleShape), color = Color.White, shape = CircleShape) {
                Icon(Icons.Default.Redeem, contentDescription = null, modifier = Modifier.padding(30.dp).fillMaxSize(), tint = Color(0xFF4A90E2))
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text("Kiến thức bí ẩn", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Khám phá ngay điều mới lạ", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(60.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF4A90E2))
        } else {
            Button(
                onClick = onRandom,
                modifier = Modifier.fillMaxWidth(0.7f).height(60.dp).shadow(10.dp, RoundedCornerShape(20.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("MỞ QUÀ NGẪU NHIÊN", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
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
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}

@Composable
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color, contentColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor), shape = RoundedCornerShape(12.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}