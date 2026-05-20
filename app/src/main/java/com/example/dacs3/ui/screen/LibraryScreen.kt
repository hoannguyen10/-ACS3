package com.example.dacs3.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

data class KnowledgeItem(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val detail: String = "",
    val topic: String = "",
    val isUnlocked: Boolean = false,
    val isSaved: Boolean = false
)

data class CategoryItemInfo(
    val title: String,
    val icon: ImageVector,
    val bgColor: Color
)

fun getCategoryInfo(topic: String): CategoryItemInfo {
    return when (topic) {
        "Lịch Sử" -> CategoryItemInfo("Lịch Sử", Icons.Default.History, LightBlueBg)
        "Khoa học" -> CategoryItemInfo("Khoa học", Icons.Default.Science, Color(0xFFF0FDF4))
        "Sinh học" -> CategoryItemInfo("Sinh học", Icons.Default.Pets, Color(0xFFFFF7ED))
        "Địa lý" -> CategoryItemInfo("Địa lý", Icons.Default.Public, Color(0xFFEFF6FF))
        "Văn hóa", "Văn Hóa" -> CategoryItemInfo("Văn hóa", Icons.Default.MenuBook, Color(0xFFFAF5FF))
        "Khác" -> CategoryItemInfo("Khác", Icons.Default.MoreHoriz, Color(0xFFF3F4F6))
        else -> CategoryItemInfo(topic, Icons.Default.Folder, Color(0xFFF8FAFC))
    }
}

@Composable
fun LibraryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var streakDays by remember { mutableIntStateOf(0) }
    val currentUserId = "user_test_01"

    // States lưu trữ dữ liệu
    var allKnowledgeBase by remember { mutableStateOf<List<KnowledgeItem>>(emptyList()) }
    var unlockedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var savedListDetail by remember { mutableStateOf<List<KnowledgeItem>>(emptyList()) }
    var savedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    // States giao diện
    var isExpanded by remember { mutableStateOf(false) }

    // States cho Flashcard
    var viewingItem by remember { mutableStateOf<KnowledgeItem?>(null) }
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "rotation"
    )

    // Xử lý nút Back vật lý hoặc hệ thống
    BackHandler(enabled = selectedCategory != null || viewingItem != null || search.isNotEmpty()) {
        if (viewingItem != null) {
            viewingItem = null
            rotated = false
        } else if (selectedCategory != null) {
            selectedCategory = null
        } else if (search.isNotEmpty()) {
            search = ""
        }
    }

    // Cập nhật trạng thái Lưu trực tiếp từ Flashcard chi tiết
    fun toggleSaveStatus(item: KnowledgeItem) {
        val newState = !item.isSaved
        db.collection("users").document(currentUserId)
            .collection("interactions").document(item.id)
            .update("is_saved", newState)
            .addOnSuccessListener {
                Toast.makeText(context, if (newState) "Đã lưu" else "Đã bỏ lưu", Toast.LENGTH_SHORT).show()
            }
    }

    LaunchedEffect(Unit) {
        db.collection("users").document(currentUserId).addSnapshotListener { snapshot, error ->
            if (snapshot != null && snapshot.exists()) {
                streakDays = (snapshot.getLong("current_streak") ?: 0L).toInt()
            }
        }
        val topics = listOf("Khoa học", "Khác", "Lịch Sử", "Sinh học", "Văn hóa", "Văn Hóa", "Địa lý")
        val tempKnowledgeList = mutableListOf<KnowledgeItem>()
        var fetchCount = 0

        topics.forEach { cat ->
            db.collection("knowledge_snacks").document("Kiến thức").collection(cat).get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach { doc ->
                        tempKnowledgeList.add(
                            KnowledgeItem(
                                id = doc.id,
                                title = doc.getString("title") ?: "Chưa có tiêu đề",
                                content = doc.getString("content") ?: "",
                                detail = doc.getString("detail") ?: "",
                                topic = cat
                            )
                        )
                    }
                }
                .addOnCompleteListener {
                    fetchCount++
                    if (fetchCount == topics.size) {
                        allKnowledgeBase = tempKnowledgeList
                    }
                }
        }

        db.collection("users").document(currentUserId).collection("interactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val tempUnlockedIds = mutableSetOf<String>()
                    val tempSavedIds = mutableSetOf<String>()
                    val tempSavedList = mutableListOf<KnowledgeItem>()

                    snapshot.documents.forEach { doc ->
                        val id = doc.id
                        val isSeen = doc.getBoolean("is_seen") ?: false
                        val isSaved = doc.getBoolean("is_saved") ?: false

                        if (isSeen) tempUnlockedIds.add(id)

                        if (isSaved) {
                            tempSavedIds.add(id)
                            tempSavedList.add(
                                KnowledgeItem(
                                    id = id,
                                    title = doc.getString("title") ?: "Chưa có tiêu đề",
                                    content = "",
                                    topic = doc.getString("topic") ?: "Khác",
                                    isUnlocked = true,
                                    isSaved = true
                                )
                            )
                        }
                    }

                    unlockedIds = tempUnlockedIds
                    savedIds = tempSavedIds
                    savedListDetail = tempSavedList
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = { AppTopBar(
            streakDays = streakDays,
            onNotificationClick = { /* Điều hướng đến thông báo */ }
        ) },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = Color.White
    ) { padding ->

        // 1. MÀN HÌNH XEM FLASHCARD CHI TIẾT
        if (viewingItem != null) {
            val currentItem = allKnowledgeBase.find { it.id == viewingItem!!.id }?.copy(
                isUnlocked = true,
                isSaved = savedIds.contains(viewingItem!!.id)
            ) ?: viewingItem!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp)
                ) {
                    IconButton(onClick = { viewingItem = null; rotated = false }) {
                        Icon(Icons.Default.ArrowBack, null, tint = GreenDeep)
                    }
                    Text("Chi tiết kiến thức", fontWeight = FontWeight.Black, fontSize = 20.sp)
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    LibraryFlashcardView(
                        rotation = rotation,
                        topic = currentItem.topic,
                        title = currentItem.title,
                        content = currentItem.content,
                        detail = currentItem.detail,
                        isSaved = currentItem.isSaved,
                        onFlip = {
                            rotated = !rotated


                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))



                Button(
                    onClick = { toggleSaveStatus(currentItem) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentItem.isSaved) Color(0xFFE8F5E9) else LightBlueBg,
                        contentColor = if (currentItem.isSaved) Color(0xFF4CAF50) else GreenDeep
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (currentItem.isSaved) Icons.Default.Bookmark else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentItem.isSaved) "Đã lưu" else "Lưu thẻ",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        // 2. MÀN HÌNH DANH SÁCH THEO CHUYÊN MỤC CỤ THỂ
        else if (selectedCategory != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 15.dp)) {
                    IconButton(onClick = { selectedCategory = null }) { Icon(Icons.Default.ArrowBack, null, tint = GreenDeep) }
                    Text(selectedCategory!!, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }

                val displayList = allKnowledgeBase
                    .filter { it.topic == selectedCategory }
                    .map {
                        it.copy(
                            isUnlocked = unlockedIds.contains(it.id),
                            isSaved = savedIds.contains(it.id)
                        )
                    }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(displayList) { item ->
                        KnowledgeStatusCard(
                            item = item,
                            onClick = { viewingItem = item }
                        )
                    }
                }
            }
        }
        // 3. MÀN HÌNH THƯ VIỆN CHÍNH (Xử lý Tìm kiếm tại đây)
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(15.dp)) }

                // Ô Tìm kiếm toàn cục
                item {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tìm tiêu đề hoặc chuyên mục...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = GreenDeep) },
                        trailingIcon = {
                            if (search.isNotEmpty()) {
                                IconButton(onClick = { search = "" }) {
                                    Icon(Icons.Default.Clear, null, tint = Color.Gray)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = LightBlueBg,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                item { Spacer(modifier = Modifier.height(25.dp)) }

                // PHÂN TÁCH LOGIC GIAO DIỆN KHI ĐANG TÌM KIẾM VS BÌNH THƯỜNG
                if (search.isEmpty()) {
                    // --- A. TRẠNG THÁI BÌNH THƯỜNG (HIỆN CHUYÊN MỤC + ĐÃ LƯU) ---
                    val uniqueCategories = allKnowledgeBase.map { it.topic }.distinct()
                    val sortedCategories = uniqueCategories.sortedByDescending { topic ->
                        allKnowledgeBase.count { it.topic == topic && unlockedIds.contains(it.id) }
                    }
                    val displayedCategories = if (isExpanded) sortedCategories else sortedCategories.take(4)

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Chuyên mục", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
                            if (sortedCategories.size > 4) {
                                Text(
                                    text = if (isExpanded) "Tóm gọn" else "Xem thêm",
                                    color = GreenDeep,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(15.dp)) }

                    item {
                        Column {
                            displayedCategories.chunked(2).forEach { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                                    row.forEach { topic ->
                                        val info = getCategoryInfo(topic)
                                        val totalCount = allKnowledgeBase.count { it.topic == topic }
                                        val unlockedCount = allKnowledgeBase.count { it.topic == topic && unlockedIds.contains(it.id) }

                                        CategoryCard(
                                            title = topic,
                                            subtitle = "$unlockedCount/$totalCount thẻ",
                                            icon = info.icon,
                                            bgColor = info.bgColor,
                                            modifier = Modifier.weight(1f).clickable { selectedCategory = topic }
                                        )
                                    }
                                    if (row.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Kiến thức đã lưu", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextBlack)
                            Text("Tất cả (${savedListDetail.size})", color = GreenDeep, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(15.dp)) }

                    if (isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GreenDeep)
                            }
                        }
                    } else {
                        items(savedListDetail) { item ->
                            val fullItem = allKnowledgeBase.find { it.id == item.id }?.copy(isSaved = true, isUnlocked = true) ?: item
                            SavedSnackItem(
                                title = item.title,
                                categoryNote = "Chuyên mục: ${item.topic}",
                                onClick = { viewingItem = fullItem }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                } else {
                    // --- B. TRẠNG THÁI TÌM KIẾM (HIỆN KẾT QUẢ TÌM KIẾM TOÀN CỤC) ---
                    item {
                        Text(
                            text = "Kết quả tìm kiếm phù hợp",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = TextBlack
                        )
                    }

                    item { Spacer(modifier = Modifier.height(15.dp)) }

                    // CẬP NHẬT LOGIC TÌM KIẾM TẠI ĐÂY
                    val searchResults = allKnowledgeBase.filter { item ->
                        // 1. Chỉ lấy những thẻ người dùng đã từng xem qua (đã mở khóa)
                        val isUnlocked = unlockedIds.contains(item.id)

                        // 2. Kiểm tra từ khóa có nằm trong Tiêu đề HOẶC Chuyên mục không
                        val matchTitle = item.title.contains(search, ignoreCase = true)
                        val matchTopic = item.topic.contains(search, ignoreCase = true)

                        isUnlocked && (matchTitle || matchTopic)
                    }.map {
                        it.copy(
                            isUnlocked = true, // Chắc chắn là đã mở khóa vì đã qua filter
                            isSaved = savedIds.contains(it.id)
                        )
                    }

                    if (searchResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Không tìm thấy kiến thức phù hợp", color = Color.Gray, fontSize = 15.sp)
                                }
                            }
                        }
                    } else {
                        items(searchResults) { item ->
                            KnowledgeStatusCard(
                                item = item,
                                onClick = { viewingItem = item }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedSnackItem(title: String, categoryNote: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(50.dp), shape = RoundedCornerShape(12.dp), color = LightBlueBg) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.padding(12.dp), tint = GreenDeep)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1, color = TextBlack)
                Text(categoryNote, fontSize = 12.sp, color = GreenDeep, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.ChevronRight, null, tint = GreenMedium)
        }
    }
}

@Composable
fun StatusBadge(isSaved: Boolean, isUnlocked: Boolean) {
    val (bgColor, textColor, icon, text) = when {
        isSaved -> listOf(Color(0xFFFEE2E2), Color(0xFFDC2626), Icons.Default.Favorite, "Đã lưu")
        isUnlocked -> listOf(Color(0xFFDCFCE7), Color(0xFF16A34A), Icons.Default.CheckCircle, "Đã mở")
        else -> listOf(Color(0xFFF1F5F9), Color(0xFF64748B), Icons.Default.Lock, "Chưa mở")
    }

    Surface(
        color = bgColor as Color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon as ImageVector,
                contentDescription = null,
                tint = textColor as Color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text as String,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KnowledgeStatusCard(item: KnowledgeItem, onClick: () -> Unit) {
    val isLocked = !item.isUnlocked
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.6f else 1f)
            .clickable(enabled = !isLocked) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (item.isSaved) GreenDeep else Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isLocked) Color.LightGray else LightBlueBg
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = if (isLocked) Color.Gray else GreenDeep
                )
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLocked) "Kiến thức chưa mở khóa" else item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    color = if (isLocked) Color.Gray else TextBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(isSaved = item.isSaved, isUnlocked = item.isUnlocked)
            }
            if (!isLocked) Icon(Icons.Default.ChevronRight, null, tint = GreenMedium)
        }
    }
}

@Composable
fun CategoryCard(title: String, subtitle: String, icon: ImageVector, bgColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(10.dp), color = Color.White) {
                Icon(icon, null, modifier = Modifier.padding(10.dp), tint = GreenDeep)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextBlack)
            Text(subtitle, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LibraryFlashcardView(
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
            .shadow(20.dp, RoundedCornerShape(32.dp))
            .background(Color.White, RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp).fillMaxSize()
            ) {
                Text(
                    text = topic.uppercase(),
                    fontSize = 13.sp,
                    color = GreenDeep,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = TextBlack,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = content,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(Icons.Default.TouchApp, contentDescription = null, tint = GreenMedium)
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
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("GIẢI THÍCH CHI TIẾT", fontSize = 14.sp, color = GreenDeep, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = detail,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = TextBlack,
                    lineHeight = 30.sp
                )
            }
        }
    }
}