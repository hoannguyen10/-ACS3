package com.example.dacs3.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

object FirebaseStatsHelper {
    private val db = FirebaseFirestore.getInstance()

    fun updateLearningStats(
        userId: String,
        words: Int = 0,
        minutes: Int = 0,
        isTest: Boolean = false,
        accuracy: Int? = null
    ) {
        val filters = listOf("all", "month", "week", "3days", "1day")
        val statsRef = db.collection("users").document(userId).collection("learning_stats")

        // Lấy ngày hiện tại định dạng yyyyMMdd để kiểm tra reset
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        filters.forEach { docId ->
            val docRef = statsRef.document(docId)

            docRef.get().addOnSuccessListener { snapshot ->
                val updates = mutableMapOf<String, Any>(
                    "words_learned" to FieldValue.increment(words.toLong()),
                    "study_time" to FieldValue.increment(minutes / 60.0),
                    "last_update" to todayStr
                )

                if (isTest) updates["tests_completed"] = FieldValue.increment(1)
                if (accuracy != null) updates["accuracy"] = accuracy

                if (snapshot.exists()) {
                    val lastUpdate = snapshot.getString("last_update") ?: ""
                    // Nếu là doc '1day' và đã sang ngày mới -> Reset trước khi cộng
                    if (docId == "1day" && lastUpdate != todayStr) {
                        docRef.set(mapOf(
                            "words_learned" to words,
                            "study_time" to (minutes / 60.0),
                            "tests_completed" to if (isTest) 1 else 0,
                            "accuracy" to (accuracy ?: 0),
                            "last_update" to todayStr
                        ))
                    } else {
                        docRef.update(updates)
                    }
                } else {
                    // Tạo mới nếu chưa có
                    docRef.set(mapOf(
                        "words_learned" to words,
                        "study_time" to (minutes / 60.0),
                        "tests_completed" to if (isTest) 1 else 0,
                        "accuracy" to (accuracy ?: 0),
                        "last_update" to todayStr
                    ))
                }
            }
        }

        // Cộng XP và cập nhật Level ở document user chính
        val userRef = db.collection("users").document(userId)
        userRef.update("total_xp", FieldValue.increment(words * 10L))
    }
}