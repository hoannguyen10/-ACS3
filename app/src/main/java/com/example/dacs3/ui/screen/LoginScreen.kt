package com.example.dacs3.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Bảng màu giữ nguyên
private val GreenDeep = Color(0xFF3C7363)
private val GreenMedium = Color(0xFF84D9BA)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)

@Composable
fun LoginScreen(
    navController: NavController,
    onRegisterClick: () -> Unit = {},
    onGoogleLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scrollState = rememberScrollState()

    var emailInput by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Dùng duy nhất 1 Column chính để tránh lỗi cuộn
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- PHẦN HEADER LOGO CỦA BẠN ---
        Spacer(modifier = Modifier.height(60.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = GreenDeep
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "MindSnack",
                style = TextStyle(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = GreenDeep
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- PHẦN CHÀO MỪNG ---
        Text(
            text = "Chào mừng trở lại!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = GreenDeep
        )

        Text(
            text = "Đăng nhập để tiếp tục học tập",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- Ô NHẬP EMAIL / USERNAME ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("EMAIL / USERNAME", fontWeight = FontWeight.Black, fontSize = 13.sp, color = GreenDeep)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                placeholder = { Text("Nhập username hoặc email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GreenMedium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = LightBlueBg,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GreenDeep
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Ô NHẬP MẬT KHẨU ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("MẬT KHẨU", fontWeight = FontWeight.Black, fontSize = 13.sp, color = GreenDeep)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Nhập mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GreenMedium) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = GreenDeep
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = LightBlueBg,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GreenDeep
                ),
                singleLine = true
            )
        }

        Text(
            text = "Quên mật khẩu?",
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 12.dp)
                .clickable { /* Handle forgot pass */ },
            color = GreenDeep,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- NÚT ĐĂNG NHẬP (LOGIC FIREBASE) ---
        Button(
            onClick = {
                val input = emailInput.trim()
                val pass = password.trim()

                if (input.isNotEmpty() && pass.isNotEmpty()) {
                    isLoading = true
                    val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()

                    if (isEmail) {
                        performLogin(input, pass, auth, navController, context) { isLoading = false }
                    } else {
                        db.collection("users")
                            .whereEqualTo("username", input)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val actualEmail = querySnapshot.documents[0].getString("email") ?: ""
                                    performLogin(actualEmail, pass, auth, navController, context) { isLoading = false }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Username không tồn tại", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenDeep),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("ĐĂNG NHẬP", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- DIVIDER ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            Text("hoặc", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- GOOGLE LOGIN ---
        OutlinedButton(
            onClick = onGoogleLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, GreenDeep)
        ) {
            Text("Tiếp tục với Google", color = GreenDeep, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // --- ĐĂNG KÝ ---
        Row(modifier = Modifier.padding(bottom = 24.dp).clickable { onRegisterClick() }) {
            Text("Chưa có tài khoản? ", fontSize = 15.sp, color = TextBlack)
            Text("Đăng ký ngay", fontSize = 15.sp, color = GreenDeep, fontWeight = FontWeight.Bold)
        }
    }
}

private fun performLogin(
    email: String,
    pass: String,
    auth: FirebaseAuth,
    navController: NavController,
    context: Context,
    onFinish: () -> Unit
) {
    auth.signInWithEmailAndPassword(email, pass)
        .addOnCompleteListener { task ->
            onFinish()
            if (task.isSuccessful) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }
}