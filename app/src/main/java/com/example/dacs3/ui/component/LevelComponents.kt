package com.example.dacs3.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.model.LevelStep

// --- MÀU SẮC ĐÃ ĐỒNG BỘ ---
private val GreenDeep = Color(0xFF3C7363)
private val GoldColor = Color(0xFFF59E0B)
private val BackgroundLightBlue = Color(0xFFE8F1FD)
private val LockedGray = Color(0xFFE5E7EB)

// Màu bổ sung cho Lộ trình
private val GreenMedium = Color(0xFF5BA38D) // Xanh lục cho level đã mở
private val LockedIconGray = Color(0xFF9CA3AF)
private val GreenLight = Color(0xFFC3E8D7)

val LevelPathData = listOf(
    LevelStep(1, "Người Chơi Tân Thủ", "Bước chân đầu tiên trên hành trình tri thức!", 0, Icons.Default.ChildCare),
    LevelStep(2, "Người Chơi Tập Sự", "Làm quen với các khái niệm cơ bản.", 50, Icons.AutoMirrored.Filled.MenuBook),
    LevelStep(3, "Người Chơi Kiên Trì", "Bạn đang làm rất tốt, hãy giữ vững phong độ.", 150, Icons.Default.Inventory2),
    LevelStep(4, "Người Chơi Thông Thái", "Kiến thức của bạn đã vượt trội hơn hẳn.", 350, Icons.Default.Psychology),
    LevelStep(5, "Người Chơi Bậc Thầy", "Ngôi sao sáng! Không gì làm khó được bạn.", 700, Icons.Default.Stars)
)

@Composable
fun LevelRoadmapOverlay(
    streakDays: Int,
    userTotalExp: Int,
    levelPath: List<LevelStep>,
    onBack: () -> Unit,
    onSelect: (LevelStep) -> Unit
) {
    val maxExp = levelPath.lastOrNull()?.xpRequired ?: 1
    val progress = (userTotalExp.toFloat() / maxExp).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize().background(BackgroundLightBlue)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            AppTopBar(streakDays = streakDays)

            // Thanh tiến độ tổng quan
            Surface(
                color = Color.White,
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.background(BackgroundLightBlue, CircleShape).size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = GreenDeep)
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tổng tiến độ Lộ trình", fontWeight = FontWeight.Black, fontSize = 16.sp, color = GreenDeep)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                color = GoldColor,
                                trackColor = LockedGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$userTotalExp / $maxExp EXP", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Danh sách Level có ziczac và đường kẻ
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 40.dp, bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                levelPath.forEachIndexed { index, level ->
                    val isUnlocked = userTotalExp >= level.xpRequired
                    val isNext = !isUnlocked && (index == 0 || userTotalExp >= levelPath[index - 1].xpRequired)

                    LevelNode(
                        level = level,
                        isUnlocked = isUnlocked,
                        isNext = isNext,
                        modifier = Modifier.offset(x = if (index % 2 != 0) (-60).dp else 60.dp),
                        onClick = { onSelect(level) }
                    )

                    // Vẽ đường nối
                    if (index < levelPath.lastIndex) {
                        val isUnlockedLine = userTotalExp >= levelPath[index + 1].xpRequired
                        val lineColor = if (isUnlockedLine) GreenMedium else LockedGray

                        Canvas(modifier = Modifier.width(120.dp).height(70.dp)) {
                            val isRightToLeft = (index % 2 == 0)
                            val startX = if (isRightToLeft) size.width else 0f
                            val endX = if (isRightToLeft) 0f else size.width

                            drawLine(
                                color = lineColor,
                                start = Offset(startX, 0f),
                                end = Offset(endX, size.height),
                                strokeWidth = 16f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelNode(level: LevelStep, isUnlocked: Boolean, isNext: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by if (isNext) {
        infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 1.08f,
            animationSpec = infiniteRepeatable(animation = tween(1000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "scale"
        )
    } else { remember { mutableStateOf(1f) } }

    val displayIcon = if (!isUnlocked && !isNext) Icons.Default.Lock else level.icon

    // --- MÀU MẶT TRÊN CỦA NÚT ---
    val faceColor = when {
        isUnlocked -> GreenMedium
        isNext -> Color.White
        else -> LockedGray
    }

    // --- MÀU BÓNG 3D (ĐỘ DÀY) Ở DƯỚI ĐÁY NÚT ---
    val shadowColor = when {
        isUnlocked -> Color(0xFF407D69)  // Tối hơn GreenMedium
        isNext -> Color(0xFFD1D5DB)      // Xám nhạt làm bóng cho nút Trắng
        else -> Color(0xFFCBD5E1)        // Xám đậm làm bóng cho nút Khóa
    }

    val iconColor = when {
        isUnlocked -> Color.White
        isNext -> GreenMedium
        else -> LockedIconGray
    }

    val borderColor = if (isNext) GreenDeep else Color.Transparent

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        // Tag "Mục tiêu"
        if (isNext) {
            Surface(color = GoldColor, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 8.dp).scale(scale)) {
                Text("MỤC TIÊU", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }

        // --- KHUNG HIỆU ỨNG 3D ---
        Box(
            modifier = Modifier
                .size(if (isNext) 90.dp else 80.dp)
                .scale(scale)
                .clickable { onClick() }
        ) {
            // 1. LỚP ĐÁY (Đổ màu viền dày 6dp xuống dưới)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = 6.dp) // Kéo xuống để tạo độ dày
                    .background(shadowColor, CircleShape)
            )

            // 2. LỚP MẶT TRÊN (Phủ lên lớp đáy)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(faceColor, CircleShape)
                    .border(if (isNext) 4.dp else 0.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = displayIcon, contentDescription = null, modifier = Modifier.size(36.dp), tint = iconColor)
            }
        }
    }
}

@Composable
fun LevelDetailDialog(level: LevelStep, userTotalExp: Int, onDismiss: () -> Unit) {
    val isUnlocked = userTotalExp >= level.xpRequired
    val progress = if (userTotalExp >= level.xpRequired) 1f else (userTotalExp.toFloat() / level.xpRequired.coerceAtLeast(1))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = CircleShape, color = if (isUnlocked) GreenLight else LockedGray,
                    modifier = Modifier.size(70.dp).padding(bottom = 12.dp)
                ) {
                    Icon(level.icon, null, modifier = Modifier.padding(16.dp), tint = if (isUnlocked) GreenDeep else LockedIconGray)
                }
                Text(level.title, fontWeight = FontWeight.Black, fontSize = 22.sp, color = GreenDeep, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(level.description, fontSize = 15.sp, textAlign = TextAlign.Center, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = if (isUnlocked) GoldColor else GreenMedium, trackColor = LockedGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(if (isUnlocked) "Đã mở khóa!" else "Hiện tại: $userTotalExp / ${level.xpRequired} EXP", fontWeight = FontWeight.Bold, color = if (isUnlocked) GreenDeep else Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GreenDeep), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("TUYỆT VỜI", fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}