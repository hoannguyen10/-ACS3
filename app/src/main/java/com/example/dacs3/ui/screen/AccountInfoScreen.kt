package com.example.dacs3.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

val TealMain = Color(0xFF009688)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInfoScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val loggedInUid = currentUser?.uid ?: ""

    // --- TRẠNG THÁI GIAO DIỆN ---
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- TRẠNG THÁI ẨN/HIỆN MẬT KHẨU ---
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // --- DỮ LIỆU ---
    var actualDocId by remember { mutableStateOf("") }
    val email = currentUser?.email ?: ""
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf("") }

    // LẤY DOCUMENT ID TỪ FIRESTORE (Để phục vụ việc xóa acc)
    LaunchedEffect(loggedInUid) {
        if (loggedInUid.isNotEmpty()) {
            db.collection("users")
                .whereEqualTo("uid", loggedInUid)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val doc = snapshot.documents.firstOrNull()
                    if (doc != null) {
                        actualDocId = doc.id
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin tài khoản", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (isEditing) {
                                errorMessage = ""
                                if (currentPassword.isEmpty()) {
                                    errorMessage = "Vui lòng nhập mật khẩu hiện tại."
                                    return@TextButton
                                }
                                if (newPassword.isNotEmpty() && newPassword != confirmNewPassword) {
                                    errorMessage = "Mật khẩu mới không khớp."
                                    return@TextButton
                                }

                                isLoading = true
                                val credential = EmailAuthProvider.getCredential(email, currentPassword)

                                currentUser?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                                    if (reauthTask.isSuccessful) {
                                        if (newPassword.isNotEmpty()) {
                                            currentUser.updatePassword(newPassword).addOnCompleteListener { passTask ->
                                                isLoading = false
                                                if (passTask.isSuccessful) {
                                                    isEditing = false
                                                    currentPassword = ""; newPassword = ""; confirmNewPassword = ""
                                                    coroutineScope.launch { snackbarHostState.showSnackbar("Đổi mật khẩu thành công!") }
                                                } else {
                                                    errorMessage = "Lỗi: ${passTask.exception?.message}"
                                                }
                                            }
                                        } else {
                                            isLoading = false; isEditing = false
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = "Mật khẩu hiện tại không đúng."
                                    }
                                }
                            } else {
                                isEditing = true
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TealMain)
                        else Text(if (isEditing) "Lưu" else "Chỉnh sửa", fontWeight = FontWeight.Bold, color = TealMain)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red, modifier = Modifier.padding(bottom = 10.dp))
            }

            // EMAIL (READ-ONLY)
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email (Không thể sửa)") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            if (!isEditing) {
                OutlinedTextField(
                    value = "********",
                    onValueChange = {},
                    label = { Text("Mật khẩu") },
                    readOnly = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                // MẬT KHẨU HIỆN TẠI
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Mật khẩu hiện tại") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(15.dp))

                // MẬT KHẨU MỚI
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(15.dp))

                // XÁC NHẬN MẬT KHẨU MỚI
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = { Text("Xác nhận mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Xóa tài khoản vĩnh viễn", fontWeight = FontWeight.SemiBold)
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Toàn bộ dữ liệu của bạn sẽ bị xóa vĩnh viễn. Bạn có chắc không?") },
                confirmButton = {
                    TextButton(
                        enabled = !isDeleting,
                        onClick = {
                            isDeleting = true
                            // 1. Xóa Firestore
                            if (actualDocId.isNotEmpty()) {
                                db.collection("users").document(actualDocId).delete().addOnSuccessListener {
                                    // 2. Xóa Auth
                                    currentUser?.delete()?.addOnCompleteListener { task ->
                                        isDeleting = false
                                        if (task.isSuccessful) {
                                            navController.navigate("login") { popUpTo(0) }
                                        } else {
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Cần đăng nhập lại để thực hiện xóa!") }
                                            showDeleteDialog = false
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text("Xóa", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }, enabled = !isDeleting) { Text("Hủy") }
                }
            )
        }
    }
}