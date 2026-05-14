package com.example.dacs3.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.dacs3.ui.screen.GoalsScreen
import com.example.dacs3.ui.screen.HomeScreen
import com.example.dacs3.ui.screen.RandomScreen
import com.example.dacs3.ui.screen.LibraryScreen
import com.example.dacs3.ui.screen.ProfileScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Màn hình chính
        composable("home") {
            HomeScreen(
                navController = navController,
                onLessonClick = { lessonId -> navController.navigate("detail/$lessonId") }
            )
        }

        // Màn hình Thư viện
        composable("library") {
            LibraryScreen(navController = navController)
        }

        // Màn hình Ngẫu nhiên (Xúc xắc)
        composable("random") {
            RandomScreen(
                // Chỉ định rõ kiểu String cho id để tránh lỗi inference
                onResultClick = { id: String ->
                    navController.navigate("detail/$id")
                },
                onBack = { navController.popBackStack() },
                // Bổ sung tham số navController còn thiếu
                navController = navController
            )
        }

        // Màn hình Mục tiêu
        composable("goals") {
            GoalsScreen(navController = navController)
        }

        // Màn hình Cá nhân
        composable("profile") {
            ProfileScreen(navController = navController)
        }
    }
}