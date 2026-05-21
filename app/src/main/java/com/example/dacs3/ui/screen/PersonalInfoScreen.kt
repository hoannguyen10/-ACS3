package com.example.dacs3.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.ui.theme.TealMain
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val BackgroundColor = Color(0xFFF2F5F8)
val TextFieldColor = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val loggedInUid = auth.currentUser?.uid ?: ""
    var actualDocId by remember { mutableStateOf("") }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    var currentPhotoUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // State cho Dropdown Giới tính
    var expandedGender by remember { mutableStateOf(false) }
    val genderOptions = listOf("Nam", "Nữ", "Khác")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

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
                        fullName = doc.getString("full_name") ?: ""
                        username = doc.getString("username") ?: ""
                        age = doc.get("age")?.toString() ?: ""
                        gender = doc.getString("gender") ?: ""
                        phoneNumber = doc.getString("phone_number") ?: ""
                        address = doc.getString("address") ?: ""
                        bio = doc.getString("bio") ?: ""
                        currentPhotoUrl = doc.getString("photo_url")
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isLoading) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(end = 16.dp),
                                color = TealMain
                            )
                        } else {
                            TextButton(onClick = {
                                if (isEditing) {
                                    if (actualDocId.isNotEmpty()) {
                                        isSaving = true
                                        val updates = mutableMapOf<String, Any>(
                                            "full_name" to fullName,
                                            "username" to username,
                                            "age" to age,
                                            "gender" to gender,
                                            "phone_number" to phoneNumber,
                                            "address" to address,
                                            "bio" to bio
                                        )

                                        db.collection("users").document(actualDocId)
                                            .update(updates)
                                            .addOnSuccessListener {
                                                isSaving = false
                                                isEditing = false
                                                Toast.makeText(context, "Đã cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                isSaving = false
                                                Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    isEditing = true
                                }
                            }) {
                                Text(
                                    text = if (isEditing) "Lưu" else "Chỉnh sửa",
                                    color = TealMain,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        containerColor = BackgroundColor
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealMain)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- PHẦN AVATAR ---
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.padding(vertical = 20.dp)
                ) {
                    val imageModel: Any? = when {
                        selectedImageUri != null -> selectedImageUri
                        !currentPhotoUrl.isNullOrEmpty() -> currentPhotoUrl
                        else -> null
                    }

                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = Color(0xFFE0E0E0),
                        border = BorderStroke(4.dp, Color.White),
                        shadowElevation = 4.dp
                    ) {
                        if (imageModel != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                modifier = Modifier.clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(30.dp), tint = Color.Gray)
                        }
                    }

                    if (isEditing) {
                        SmallFloatingActionButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            shape = CircleShape,
                            containerColor = TealMain,
                            contentColor = Color.White,
                            modifier = Modifier.size(36.dp).offset(x = (-5).dp, y = (-5).dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Text(
                    text = fullName.ifEmpty { "Người dùng" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                val displayUsername = username.ifEmpty { "" }
                Text(
                    text = if (displayUsername.isNotEmpty()) "@$displayUsername" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 25.dp)
                )

                // --- CÁC TRƯỜNG THÔNG TIN ---

                InfoField(label = "Họ và tên", value = fullName, isEditing = isEditing, icon = Icons.Default.Person) { fullName = it }

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Thay thế đoạn code xử lý ô Tuổi cũ bằng đoạn này:
                    Box(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                "Tuổi",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isEditing) TealMain else Color.Gray,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = age,
                                onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                                readOnly = !isEditing,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (isEditing) {
                                        // Thiết kế Stepper gọn gàng và hiện đại
                                        Row(
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .height(35.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(TealMain.copy(alpha = 0.1f)), // Nền nhạt
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    val current = age.toIntOrNull() ?: 0
                                                    if (current > 0) age = (current - 1).toString()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, "Giảm", tint = TealMain, modifier = Modifier.size(16.dp))
                                            }

                                            // Đường kẻ ngăn cách nhỏ
                                            VerticalDivider(
                                                modifier = Modifier.height(16.dp),
                                                thickness = 1.dp,
                                                color = TealMain.copy(alpha = 0.3f)
                                            )

                                            IconButton(
                                                onClick = {
                                                    val current = age.toIntOrNull() ?: 0
                                                    age = (current + 1).toString()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Add, "Tăng", tint = TealMain, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                },
                                colors = outlinedTextFieldColorsCustom()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Cột Giới tính với Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                "Giới tính",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isEditing) TealMain else Color.Gray,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = expandedGender && isEditing,
                                onExpandedChange = { if (isEditing) expandedGender = !expandedGender }
                            ) {
                                OutlinedTextField(
                                    value = gender,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        if (isEditing) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender)
                                    },
                                    colors = outlinedTextFieldColorsCustom()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedGender && isEditing,
                                    onDismissRequest = { expandedGender = false }
                                ) {
                                    genderOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                gender = option
                                                expandedGender = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                InfoField(label = "Số điện thoại", value = phoneNumber, isEditing = isEditing) { phoneNumber = it }
                InfoField(label = "Địa chỉ", value = address, isEditing = isEditing) { address = it }

                // --- PHẦN BIO ---
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(
                        "Giới thiệu bản thân",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEditing) TealMain else Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { if (it.length <= 200) bio = it },
                        readOnly = !isEditing,
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Viết gì đó về bạn...", color = Color.LightGray) },
                        colors = outlinedTextFieldColorsCustom(),
                        supportingText = {
                            if (isEditing) {
                                Text("${bio.length}/200", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun InfoField(
    label: String,
    value: String,
    isEditing: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (isEditing) TealMain else Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = !isEditing,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = icon?.let { { Icon(it, null, tint = if (isEditing) TealMain else Color.Gray) } },
            shape = RoundedCornerShape(12.dp),
            colors = outlinedTextFieldColorsCustom()
        )
    }
}

@Composable
fun outlinedTextFieldColorsCustom() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = TextFieldColor,
    unfocusedContainerColor = TextFieldColor,
    disabledContainerColor = TextFieldColor,
    focusedBorderColor = TealMain,
    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
    disabledBorderColor = Color.Transparent,
    cursorColor = TealMain,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.DarkGray
)