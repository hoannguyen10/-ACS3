package com.example.dacs3.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth // <-- THÊM IMPORT NÀY
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

private val GreenDeep = Color(0xFF3C7363)
private val GreenCorrect = Color(0xFF4CAF50)
private val GreenCorrectLight = Color(0xFFE8F5E9)
private val RedIncorrect = Color(0xFFEF5350)
private val RedIncorrectLight = Color(0xFFFFEBEE)
private val OrangeExp = Color(0xFFF59E0B)
private val TextBlack = Color(0xFF121212)
private val GrayBorder = Color(0xFFE2E8F0)
private val GrayBg = Color(0xFFF8FAFC)

// 1. CẬP NHẬT DATA CLASS: Thêm trường id
data class QuizQuestion(
    val id: String, // <-- Lấy Document ID từ Firestore
    val question: String,
    val correctAnswer: String,
    val options: List<String>
)

@Composable
fun MiniQuizScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    var questionsList by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }

    // --- THAY ĐỔI: Lấy UID động thay vì mã cứng "user_test_01" ---
    val loggedInUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var actualDocId by remember { mutableStateOf("") }

    // Tìm Document ID thực tế
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

    LaunchedEffect(Unit) {
        db.collection("knowledge_snacks").document("mini_quizz").collection("questions").get()
            .addOnSuccessListener { querySnapshot ->
                try {
                    val tempQuestions = mutableListOf<QuizQuestion>()
                    for (document in querySnapshot.documents) {
                        val docId = document.id // <-- Lấy ID của document
                        val qText = document.getString("question") ?: ""
                        val correct = document.getString("correct_answer") ?: ""
                        val distractors = document.get("distractors") as? List<*> ?: emptyList<Any>()

                        if (qText.isNotEmpty() && correct.isNotEmpty()) {
                            val stringDistractors = distractors.map { it.toString() }
                            val options = (stringDistractors + correct).shuffled()
                            // Truyền docId vào data class
                            tempQuestions.add(QuizQuestion(docId, qText, correct, options))
                        }
                    }
                    if (tempQuestions.isEmpty()) {
                        errorMessage = "Collection 'questions' trống!"
                    } else {
                        questionsList = tempQuestions.shuffled()
                    }
                } catch (e: Exception) {
                    errorMessage = "Lỗi xử lý: ${e.message}"
                }
                isLoading = false
            }
            .addOnFailureListener {
                errorMessage = "Lỗi kết nối Firebase"
                isLoading = false
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenDeep)
        }
    } else if (errorMessage.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(errorMessage, color = Color.Red)
        }
    } else if (questionsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không có câu hỏi nào.")
        }
    } else {
        val currentQuestion = questionsList[currentIndex]

        Column(
            modifier = Modifier.fillMaxSize().background(Color.White).padding(20.dp)
        ) {
            // Thanh tiêu đề
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showResultDialog = true }) {
                    Icon(Icons.Default.Close, "Đóng", tint = Color.Gray)
                }
                Text(
                    "Câu ${currentIndex + 1}/${questionsList.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GreenDeep
                )
                Icon(Icons.Default.Info, contentDescription = "Quiz Info", tint = GrayBorder)
            }

            LinearProgressIndicator(
                progress = (currentIndex + 1).toFloat() / questionsList.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = GreenDeep,
                trackColor = Color(0xFFE8F1FD)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Thẻ câu hỏi
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GrayBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = currentQuestion.question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextBlack,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Danh sách lựa chọn
            Column(modifier = Modifier.weight(1f)) {
                currentQuestion.options.forEach { option ->
                    val isSelected = selectedOption == option
                    val isCorrect = option == currentQuestion.correctAnswer

                    val targetBorderColor = when {
                        showFeedback && isCorrect -> GreenCorrect
                        showFeedback && isSelected && !isCorrect -> RedIncorrect
                        isSelected -> GreenDeep
                        else -> GrayBorder
                    }

                    val targetContainerColor = when {
                        showFeedback && isCorrect -> GreenCorrectLight
                        showFeedback && isSelected && !isCorrect -> RedIncorrectLight
                        isSelected -> Color(0xFFE8F5E9)
                        else -> Color.Transparent
                    }

                    val animatedBorderColor by animateColorAsState(targetBorderColor, tween(300))
                    val animatedContainerColor by animateColorAsState(targetContainerColor, tween(300))

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable(enabled = !showFeedback) { selectedOption = option },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(if (isSelected || showFeedback && (isCorrect || (isSelected && !isCorrect))) 2.dp else 1.dp, animatedBorderColor),
                        colors = CardDefaults.cardColors(containerColor = animatedContainerColor)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                color = TextBlack,
                                modifier = Modifier.weight(1f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (showFeedback) {
                                when {
                                    isCorrect -> Icon(Icons.Default.CheckCircle, "Đúng", tint = GreenCorrect)
                                    isSelected && !isCorrect -> Icon(Icons.Default.Cancel, "Sai", tint = RedIncorrect)
                                }
                            }
                        }
                    }
                }
            }

            // --- KHU VỰC NÚT BẤM ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showResultDialog = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GreenDeep)
                ) {
                    Text("Hoàn thành", color = GreenDeep, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (!showFeedback) {
                            // BƯỚC 1: KIỂM TRA ĐÁP ÁN
                            val isCorrect = selectedOption == currentQuestion.correctAnswer
                            if (isCorrect) score++
                            showFeedback = true

                            // 2. LƯU TỐI GIẢN CHỈ CÓ ID VÀ KẾT QUẢ
                            val questionRecord = hashMapOf(
                                "quizz_id" to currentQuestion.id, // <-- Chỉ lưu ID của Document
                                "is_correct" to isCorrect,
                                "timestamp" to FieldValue.serverTimestamp()
                            )

                            // --- THAY ĐỔI: Sử dụng actualDocId thay vì currentUserId ---
                            if (actualDocId.isNotEmpty()) {
                                db.collection("users").document(actualDocId)
                                    .collection("quizz_question_history")
                                    .add(questionRecord)
                            }

                        } else {
                            // BƯỚC 2: TIẾP THEO
                            if (currentIndex < questionsList.size - 1) {
                                currentIndex++
                                selectedOption = null
                                showFeedback = false
                            } else {
                                showResultDialog = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenDeep),
                    enabled = selectedOption != null || showFeedback
                ) {
                    Text(
                        text = when {
                            !showFeedback -> "Kiểm tra"
                            currentIndex < questionsList.size - 1 -> "Tiếp theo"
                            else -> "Kết quả"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // --- DIALOG KẾT QUẢ ---
    if (showResultDialog) {
        val expEarned = score * 5

        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenCorrect, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kết quả bài Quiz", fontWeight = FontWeight.Black, color = GreenDeep)
                }
            },
            text = {
                Column {
                    Text("Số câu đúng: $score", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                    Text("Số câu đã xem: ${currentIndex + 1}", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Phần thưởng dự kiến: +$expEarned EXP",
                            color = OrangeExp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dữ liệu từng câu đã được lưu. Bấm 'Trở về' để nhận điểm EXP.", fontSize = 14.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResultDialog = false }) {
                    Text("Hủy (Ở lại)", color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // --- THAY ĐỔI: Sử dụng actualDocId thay vì currentUserId ---
                        if (expEarned > 0 && actualDocId.isNotEmpty()) {
                            db.collection("users").document(actualDocId)
                                .update("total_exp", FieldValue.increment(expEarned.toLong()))
                                .addOnCompleteListener {
                                    showResultDialog = false
                                    navController.popBackStack()
                                }
                        } else {
                            showResultDialog = false
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenDeep)
                ) {
                    Text("Trở về & Nhận thưởng", color = Color.White)
                }
            }
        )
    }
}