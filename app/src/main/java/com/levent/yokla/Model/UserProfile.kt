package com.levent.yokla

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class UserProfile(
    @SerialName("id")
    val id: String, // auth.users tablosundaki UUID ile eşleşir

    @SerialName("full_name")
    val fullName: String, // Kullanıcının Ad Soyad bilgisi

    @SerialName("role")
    val role: String, // 'teacher' veya 'student'

    @SerialName("student_number")
    val studentNumber: String? = null, // Sadece öğrenciler için opsiyonel öğrenci numarası

    @SerialName("avatar_url")
    val avatarUrl: String? = null, // Tasarımdaki sağ üst profil resmi için URL

    @SerialName("created_at")
    val createdAt: String? = null ,// Kayıt tarihi (opsiyonel)
    @SerialName("class_level") val classLevel: Int, // EKSİK OLAN BUYDU, EKLEDİK
)