package com.example.dacs3.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

private val GreenDeep = Color(0xFF3C7363)
private val TextBlack = Color(0xFF121212)

data class QuizQuestion(
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

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }

    // Tải dữ liệu ĐÚNG CẤU TRÚC DATABASE
    LaunchedEffect(Unit) {
        db.collection("knowledge_snacks").document("mini_quizz").collection("questions").get()
            .addOnSuccessListener { querySnapshot ->
                try {
                    val tempQuestions = mutableListOf<QuizQuestion>()

                    // Duyệt qua từng Document (quizz_01, quizz_02...)
                    for (document in querySnapshot.documents) {
                        val qText = document.getString("question") ?: ""
                        val correct = document.getString("correct_answer") ?: ""
                        val distractors = document.get("distractors") as? List<*> ?: emptyList<Any>()

                        if (qText.isNotEmpty() && correct.isNotEmpty()) {
                            val stringDistractors = distractors.map { it.toString() }
                            val options = (stringDistractors + correct).shuffled()
                            tempQuestions.add(QuizQuestion(qText, correct, options))
                        }
                    }

                    if (tempQuestions.isEmpty()) {
                        errorMessage = "Collection 'questions' trống hoặc không có câu hỏi hợp lệ!"
                    } else {
                        // Lấy ngẫu nhiên tối đa 5 câu
                        questionsList = tempQuestions.shuffled().take(5)
                    }
                } catch (e: Exception) {
                    errorMessage = "Lỗi khi xử lý dữ liệu: ${e.message}"
                }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                errorMessage = "Lỗi kết nối Firebase: ${exception.message}"
                isLoading = false
            }
    }

    // --- PHẦN GIAO DIỆN (GIỮ NGUYÊN HOÀN TOÀN) ---
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenDeep)
        }
    } else if (errorMessage.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Có lỗi xảy ra:", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage, color = Color.Red, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Quay lại") }
        }
    } else if (questionsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Danh sách câu hỏi trống!", color = Color.Gray, fontSize = 16.sp)
        }
    } else {
        val currentQuestion = questionsList[currentIndex]

        Column(
            modifier = Modifier.fillMaxSize().background(Color.White).padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.Close, "Đóng", tint = Color.Gray)
                }
                Text("Câu ${currentIndex + 1}/${questionsList.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreenDeep)
                Spacer(modifier = Modifier.width(48.dp))
            }

            LinearProgressIndicator(
                progress = (currentIndex + 1).toFloat() / questionsList.size,
                modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 16.dp),
                color = GreenDeep,
                trackColor = Color(0xFFE8F1FD)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = currentQuestion.question,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextBlack,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            currentQuestion.options.forEach { option ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                        if (option == currentQuestion.correctAnswer) score++
                        if (currentIndex < questionsList.size - 1) {
                            currentIndex++
                        } else {
                            showResultDialog = true
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Text(text = option, modifier = Modifier.padding(16.dp), fontSize = 16.sp, color = TextBlack)
                }
            }
        }
    }



    if (showResultDialog) {
        val expEarned = score * 5

        AlertDialog(
            onDismissRequest = { },
            title = { Text("Hoàn thành Mini Quiz!", fontWeight = FontWeight.Black, color = GreenDeep) },
            text = {
                Column {
                    Text("Số câu đúng: $score / ${questionsList.size}", fontSize = 16.sp)
                    Text("Số câu sai: ${questionsList.size - score}", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Phần thưởng: +$expEarned EXP", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), fontSize = 18.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentUserId = "user_test_01"
                        if (expEarned > 0) {
                            db.collection("users").document(currentUserId)
                                .update("total_exp", FieldValue.increment(expEarned.toLong()))
                                .addOnSuccessListener {
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
                    Text("Nhận thưởng & Về", color = Color.White)
                }
            }
        )
    }
}
