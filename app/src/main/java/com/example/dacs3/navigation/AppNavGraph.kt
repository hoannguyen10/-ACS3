package com.example.dacs3.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.dacs3.ui.screen.* // Đảm bảo import đúng LoginScreen và RegisterScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "library"
    ) {


        // Màn hình Đăng nhập
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                },
                onGoogleLogin = { /* Xử lý */ }
            )
        }

        // Màn hình Đăng ký
        composable("register") {
            RegisterScreen(
                onLoginClick = {
                    navController.popBackStack()
                },
                onRegisterClick = { user, email, name, pass ->
                    // Sau khi tạo tài khoản thành công, có thể cho vào Home luôn
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoogleLogin = { /* Xử lý */ }
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
    }
}