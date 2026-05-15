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


private val GreenDeep = Color(0xFF3C7363)
private val GreenMedium = Color(0xFF84D9BA)
private val LightBlueBg = Color(0xFFE8F1FD)
private val TextBlack = Color(0xFF121212)


data class SavedKnowledge(
    val id: String = "",
    val content: String = "",
    val topic: String = "",
    val detail: String = ""
)


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


    var savedList by remember { mutableStateOf<List<SavedKnowledge>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Lấy dữ liệu từ Firestore
    LaunchedEffect(Unit) {
        db.collection("users")
            .document("user_test_01")
            .collection("saved_knowledge")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
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
        containerColor = Color.White
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
                placeholder = { Text("Tìm kiếm kiến thức...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = GreenDeep) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = LightBlueBg,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GreenDeep,
                    focusedTextColor = TextBlack,
                    unfocusedTextColor = TextBlack
                )
            )

            Spacer(modifier = Modifier.height(25.dp))

            Text("Chuyên mục", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
            Spacer(modifier = Modifier.height(15.dp))


            val categories = listOf(
                CategoryItem("Lịch Sử", "${savedList.count { it.topic == "Lịch Sử" }} thẻ", Icons.Default.History, LightBlueBg),
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
                Text("Kiến thức đã lưu", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
                Text("Tất cả (${savedList.size})", color = GreenDeep, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenDeep)
                }
            } else {
                val filteredList = savedList.filter {
                    it.content.contains(search, ignoreCase = true) || it.topic.contains(search, ignoreCase = true)
                }

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

@Composable
fun CategoryCard(item: CategoryItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = item.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Surface(modifier = Modifier.size(45.dp), shape = RoundedCornerShape(12.dp), color = Color.White) {
                Icon(item.icon, null, modifier = Modifier.padding(10.dp), tint = GreenDeep)
            }
            Spacer(modifier = Modifier.height(15.dp))
            Text(item.title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextBlack)
            Text(item.subtitle, fontSize = 13.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SavedSnackItem(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(50.dp), shape = RoundedCornerShape(12.dp), color = LightBlueBg) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.padding(12.dp), tint = GreenDeep)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1, color = TextBlack)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = GreenMedium)
        }
    }
}