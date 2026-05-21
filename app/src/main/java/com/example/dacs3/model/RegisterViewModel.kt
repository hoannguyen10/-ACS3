package com.example.dacs3.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    // =========================================================================
    // XỬ LÝ ĐĂNG KÝ BẰNG EMAIL & PASSWORD
    // =========================================================================

    fun registerUser(username: String, email: String, fullName: String, pass: String) {
        if (username.isBlank() || email.isBlank() || fullName.isBlank() || pass.isBlank()) {
            _registerState.value = RegisterState.Error("Vui lòng điền đầy đủ thông tin")
            return
        }

        _registerState.value = RegisterState.Loading

        // BƯỚC 1: Kiểm tra username đã tồn tại trong Firestore chưa
        db.collection("users").document(username).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _registerState.value = RegisterState.Error("Tên đăng nhập này đã được sử dụng!")
                } else {
                    // BƯỚC 2: Nếu username khả dụng, tiến hành tạo tài khoản trên Firebase Auth
                    createAuthUser(username, email, fullName, pass)
                }
            }
            .addOnFailureListener {
                _registerState.value = RegisterState.Error("Lỗi kiểm tra database: ${it.message}")
            }
    }

    private fun createAuthUser(username: String, email: String, fullName: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: ""

                // BƯỚC 3: Tạo dữ liệu khớp với cấu trúc
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = sdf.format(Date())

                // Tính số ngày từ Epoch
                val currentDayNumber = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

                val userMap = hashMapOf(
                    "username" to username,
                    "email" to email,
                    "full_name" to fullName,
                    "current_streak" to 0,
                    "total_exp" to 0,
                    "last_task_date" to currentDate,
                    "last_check_in_day" to currentDayNumber,
                    "uid" to userId
                )

                // BƯỚC 4: Đẩy lên Firestore với ID là USERNAME
                db.collection("users").document(username)
                    .set(userMap)
                    .addOnSuccessListener {
                        _registerState.value = RegisterState.Success
                    }
                    .addOnFailureListener { e ->
                        _registerState.value = RegisterState.Error("Lỗi lưu Firestore: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                _registerState.value = RegisterState.Error("Lỗi đăng ký Auth: ${e.message}")
            }
    }

    // =========================================================================
    // XỬ LÝ ĐĂNG NHẬP / ĐĂNG KÝ BẰNG GOOGLE
    // =========================================================================

    fun signInWithGoogle(idToken: String) {
        _registerState.value = RegisterState.Loading

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                val email = user?.email ?: ""
                val fullName = user?.displayName ?: "Người dùng Google"
                val userId = user?.uid ?: ""

                // Lấy phần trước dấu @ của email làm username tạm thời
                val tempUsername = email.substringBefore("@")

                // Kiểm tra xem user này đã tồn tại trong Firestore chưa (tìm theo email)
                db.collection("users").whereEqualTo("email", email).get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            // Nếu là user mới hoàn toàn -> Tạo document mới
                            saveGoogleUserToFirestore(tempUsername, email, fullName, userId)
                        } else {
                            // Nếu user đã tồn tại -> Thành công luôn (coi như đăng nhập thành công)
                            _registerState.value = RegisterState.Success
                        }
                    }
                    .addOnFailureListener { e ->
                        _registerState.value = RegisterState.Error("Lỗi kiểm tra dữ liệu Google: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                _registerState.value = RegisterState.Error("Lỗi xác thực Google: ${e.message}")
            }
    }

    private fun saveGoogleUserToFirestore(username: String, email: String, fullName: String, userId: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val currentDayNumber = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

        val userMap = hashMapOf(
            "username" to username,
            "email" to email,
            "full_name" to fullName,
            "current_streak" to 0,
            "total_exp" to 0,
            "last_task_date" to currentDate,
            "last_check_in_day" to currentDayNumber,
            "uid" to userId
        )

        // Lưu vào Firestore với ID là username tạm thời
        db.collection("users").document(username).set(userMap)
            .addOnSuccessListener {
                _registerState.value = RegisterState.Success
            }
            .addOnFailureListener { e ->
                _registerState.value = RegisterState.Error("Lỗi lưu dữ liệu Google: ${e.message}")
            }
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH
    // =========================================================================

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}