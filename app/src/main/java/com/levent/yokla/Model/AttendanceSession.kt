package com.levent.yokla

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceSession(
    @SerialName("id")
    val id: String,

    @SerialName("course_id")
    val courseId: String,

    @SerialName("expires_at")
    val expiresAt: String, // QR kodun 20 saniyelik geçerlilik kontrolü için

    @SerialName("lat")
    val lat: Double? = null,

    @SerialName("lon")
    val lon: Double? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    // Ders ismini 'Geçmiş' ekranında göstermek için bu ilişkiyi ekledim
    @SerialName("courses")
    val courses: Course? = null
)


@Serializable
data class AttendanceRecord(
    @SerialName("id")
    val id: String? = null, // Veritabanı otomatik ID verdiği için null olabilir

    @SerialName("student_id")
    val studentId: String,

    @SerialName("session_id")
    val sessionId: String,

    @SerialName("status")
    val status: String = "geldi", // Varsayılan durum

    @SerialName("created_at")
    val createdAt: String? = null,

    // İlişkisel verileri çekebilmek için bu alanları eklemek JSON hatasını önler
    @SerialName("attendance_sessions")
    val attendanceSessions: AttendanceSession? = null
)