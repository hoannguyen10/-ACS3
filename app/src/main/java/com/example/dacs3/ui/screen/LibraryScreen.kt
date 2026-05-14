package com.example.dacs3.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dacs3.ui.component.AppBottomBar
import com.example.dacs3.ui.component.AppTopBar
import com.google.firebase.firestore.FirebaseFirestore

// 1. Định nghĩa Data Model cho Firestore
data class SavedKnowledge(
    val id: String = "",
    val content: String = "",
    val topic: String = "",
    val detail: String = ""
)

// 2. Định nghĩa Data Model cho Categories (Sửa lỗi Unresolved reference CategoryItem)
data class CategoryItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val bgColor: Color
)

@Composable
fun LibraryScreen(navController: NavController) {
    var search by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()

    // State lưu danh sách dữ liệu
    var savedList by remember { mutableStateOf<List<SavedKnowledge>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Lấy dữ liệu từ Firestore
    LaunchedEffect(Unit) {
        db.collection("users")
            .document("user_test_01")
            .collection("saved_knowledge")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ -> // Sử dụng _ thay cho e để tránh lỗi Unused parameter
                if (snapshot != null) {
                    savedList = snapshot.documents.map { doc ->
                        SavedKnowledge(
                            id = doc.id,
                            content = doc.getString("content") ?: "",
                            topic = doc.getString("topic") ?: "Chưa phân loại",
                            detail = doc.getString("detail") ?: ""
                        )
                    }
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = { AppTopBar(streakDays = 12) },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm kiếm kiến thức...") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF00ADEF)
                )
            )

            Spacer(modifier = Modifier.height(25.dp))

            Text("Chuyên mục", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(15.dp))

            // Thiết lập danh sách Category dựa trên dữ liệu thật
            val categories = listOf(
                CategoryItem("Lịch Sử", "${savedList.count { it.topic == "Lịch Sử" }} thẻ", Icons.Default.History, Color(0xFFE0F2FE)),
                CategoryItem("Khoa học", "${savedList.count { it.topic == "Khoa học" }} thẻ", Icons.Default.Science, Color(0xFFF0FDF4)),
                CategoryItem("Code Tips", "0 snippets", Icons.Default.Code, Color(0xFFFFF7ED)),
                CategoryItem("Khác", "${savedList.count { it.topic != "Lịch Sử" && it.topic != "Khoa học" }} thẻ", Icons.Default.MoreHoriz, Color(0xFFEFF6FF))
            )

            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    CategoryCard(categories[0], Modifier.weight(1f))
                    CategoryCard(categories[1], Modifier.weight(1f))
                }
                Spacer(Modifier.height(15.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    CategoryCard(categories[2], Modifier.weight(1f))
                    CategoryCard(categories[3], Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Kiến thức đã lưu", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Tất cả (${savedList.size})", color = Color(0xFF00ADEF), fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00ADEF))
                }
            } else {
                val filteredList = savedList.filter {
                    it.content.contains(search, ignoreCase = true) || it.topic.contains(search, ignoreCase = true)
                }

                // Sửa lỗi infer type bằng cách chỉ định rõ kiểu dữ liệu trong items
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items = filteredList) { item: SavedKnowledge ->
                        SavedSnackItem(
                            title = item.content,
                            subtitle = "Chủ đề: ${item.topic}"
                        )
                    }
                }
            }
        }
    }
}

// 3. Định nghĩa các Component bổ trợ (Sửa lỗi Unresolved reference CategoryCard & SavedSnackItem)

@Composable
fun CategoryCard(item: CategoryItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Surface(modifier = Modifier.size(45.dp), shape = RoundedCornerShape(12.dp), color = item.bgColor) {
                Icon(item.icon, null, modifier = Modifier.padding(10.dp), tint = Color(0xFF1E293B))
            }
            Spacer(modifier = Modifier.height(15.dp))
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.subtitle, fontSize = 13.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SavedSnackItem(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(50.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFF1F5F9)) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.padding(12.dp), tint = Color(0xFF00ADEF))
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}