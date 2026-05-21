package com.example.dacs3.ui.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs3.model.RegisterState
import com.example.dacs3.model.RegisterViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

private val GreenDeep = Color(0xFF3C7363)
private val GreenMedium = Color(0xFF84D9BA)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(), // Khởi tạo ViewModel
    onLoginClick: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {} // Chuyển màn hình khi thành công
) {
    val context = LocalContext.current
    val registerState by viewModel.registerState.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // ==========================================================
    // CẤU HÌNH GOOGLE SIGN-IN LAUNCHER
    // ==========================================================
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // THAY MÃ CLIENT ID CỦA BẠN VÀO ĐÂY
            .requestIdToken("886043032657-j8t1tjgstl5n9g5o1387schu146v4p03.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { token ->
                viewModel.signInWithGoogle(token)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show()
        }
    }
    // ==========================================================

    // Xử lý các trạng thái từ ViewModel
    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onRegisterSuccess() // Gọi callback để chuyển hướng về màn Home/Login
            }
            is RegisterState.Error -> {
                val errorMsg = (registerState as RegisterState.Error).message
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "MindSnack",
            style = TextStyle(
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = GreenDeep,
                letterSpacing = (-0.5).sp
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Tham gia cộng đồng\nhọc tập",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            color = TextBlack
        )

        Text(
            text = "Bắt đầu hành trình khám phá kiến thức mỗi ngày cùng MindSnack.",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 12.dp, start = 20.dp, end = 20.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        RegisterTextField(
            label = "TÊN ĐĂNG NHẬP",
            value = username,
            onValueChange = { username = it },
            placeholder = "username123",
            leadingIcon = Icons.Default.AccountCircle
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterTextField(
            label = "EMAIL",
            value = email,
            onValueChange = { email = it },
            placeholder = "email@example.com",
            leadingIcon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterTextField(
            label = "HỌ VÀ TÊN",
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = "Nguyễn Văn A",
            leadingIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterPasswordField(
            label = "MẬT KHẨU",
            value = password,
            onValueChange = { password = it },
            isVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterPasswordField(
            label = "XÁC NHẬN MẬT KHẨU",
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            isVisible = confirmPasswordVisible,
            onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (password == confirmPassword) {
                    viewModel.registerUser(username, email, fullName, password)
                } else {
                    Toast.makeText(context, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenDeep),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            enabled = registerState !is RegisterState.Loading // Khóa nút khi đang load
        ) {
            if (registerState is RegisterState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    "TẠO TÀI KHOẢN",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            Text(
                "hoặc",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nút Tiếp tục với Google
        OutlinedButton(
            onClick = {
                // Khởi chạy màn hình chọn tài khoản Google
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, GreenDeep),
            enabled = registerState !is RegisterState.Loading
        ) {
            Text(
                "Tiếp tục với Google",
                color = GreenDeep,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }

        Row(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .clickable { onLoginClick() }
        ) {
            Text("Đã có tài khoản? ", fontSize = 15.sp, color = TextBlack)
            Text(
                "Đăng nhập",
                color = GreenDeep,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun RegisterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = GreenDeep
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = GreenMedium) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = LightBlueBg,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = GreenDeep,
                focusedTextColor = TextBlack,
                unfocusedTextColor = TextBlack
            ),
            singleLine = true
        )
    }
}

@Composable
fun RegisterPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = GreenDeep
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Nhập mật khẩu", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GreenMedium) },
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = GreenDeep
                    )
                }
            },
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = LightBlueBg,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = GreenDeep,
                focusedTextColor = TextBlack,
                unfocusedTextColor = TextBlack
            ),
            singleLine = true
        )
    }
}