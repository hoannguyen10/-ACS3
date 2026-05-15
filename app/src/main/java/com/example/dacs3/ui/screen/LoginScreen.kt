package com.example.dacs3.ui.screen

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val GreenDeep = Color(0xFF3C7363)
private val GreenMedium = Color(0xFF84D9BA)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onGoogleLogin: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))


        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = GreenDeep //
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "MindSnack",
                style = TextStyle(
                    fontWeight = FontWeight.Black, // Siêu đậm
                    fontSize = 32.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = GreenDeep //
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Chào mừng quay trở lại!",
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            color = TextBlack
        )
        Text(
            text = "Sẵn sàng để tiếp thu thêm kiến thức mới hôm nay?",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 10.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))


        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "EMAIL / TÊN ĐĂNG NHẬP",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = GreenDeep //
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Nhập email của bạn") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GreenMedium) }, //
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = LightBlueBg, //
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GreenDeep, //
                    focusedTextColor = TextBlack,
                    unfocusedTextColor = TextBlack
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))


        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "MẬT KHẨU",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = GreenDeep //
                )
                Text(
                    "Quên mật khẩu?",
                    color = GreenDeep, //
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { /* Xử lý */ }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Nhập mật khẩu của bạn", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GreenMedium) }, //
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = GreenDeep //
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = LightBlueBg, //
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GreenDeep, //
                    focusedTextColor = TextBlack,
                    unfocusedTextColor = TextBlack
                )
            )
        }

        Spacer(modifier = Modifier.height(40.dp))


        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenDeep), //
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                "ĐĂNG NHẬP",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {

            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE2E8F0),
                thickness = 1.dp
            )

            Text(
                text = "hoặc",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Medium
            )


            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE2E8F0),
                thickness = 1.dp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onGoogleLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),

            border = BorderStroke(2.dp, GreenDeep)
        ) {
            Text(
                "Tiếp tục với Google",
                color = GreenDeep,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))


        Row(
            modifier = Modifier
                .padding(vertical = 24.dp)
                .clickable { onRegisterClick() }
        ) {
            Text("Chưa có tài khoản? ", fontSize = 15.sp, color = TextBlack)
            Text(
                "Đăng ký ngay",
                color = GreenDeep, //
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
    }
}