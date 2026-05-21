package com.example.dacs3.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.dacs3.ui.screen.* @Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "profile"
    ) {

        // Màn hình Đăng nhập
        composable("login") {
            LoginScreen(
                navController = navController,
                onRegisterClick = {
                    navController.navigate("register")
                },
                onGoogleLogin = { /* Xử lý */ }
            )
        }

        // Màn hình Đăng ký (ĐÃ ĐƯỢC CẬP NHẬT)
        composable("register") {
            RegisterScreen(
                onLoginClick = {
                    // Quay lại màn hình đăng nhập
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    // Gọi hàm này khi ViewModel báo đăng ký Firebase thành công
                    // Chuyển vào Home và xóa sạch BackStack của màn login/register
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // --- CÁC MÀN HÌNH CHÍNH CỦA APP ---

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
                onResultClick = { id: String ->
                    navController.navigate("detail/$id")
                },
                onBack = { navController.popBackStack() },
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

        composable("miniquiz") {
            MiniQuizScreen(navController = navController)
        }

        composable("personal_info") {
            PersonalInfoScreen(navController = navController)
        }

        // Màn hình Thông tin tài khoản
        composable("account_info") {
            AccountInfoScreen(navController = navController)
        }
    }
}