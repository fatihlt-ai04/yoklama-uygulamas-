package com.levent.yokla.Model

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Attendance(
    val id: Int? = null,
    @SerialName("student_id") val studentId: String,
    @SerialName("course_id") val courseId: String,
    @SerialName("created_at") val createdAt: String // Yoklama zamanı
)
