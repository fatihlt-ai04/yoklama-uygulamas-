package com.levent.yokla

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Course(
    @SerialName("id")
    val id: String,

    @SerialName("course_name") // Veritabanındaki sütun adı 'course_name' olduğu için bu ŞART!
    val courseName: String,

    @SerialName("class_level") // Öğrencinin sınıfına göre dersleri listelemek için kullanılır
    val classLevel: Int,

    @SerialName("teacher_id") // Hocanın kim olduğunu tutan ID
    val teacherId: String? = null,

    @SerialName("created_at") // Veritabanı kayıt tarihi
    val createdAt: String? = null,

    // Uygulama içinde senin kullandığın ek alanlar
    var teacherName: String? = "Bilinmiyor",
    var attendanceCount: Int = 0,
    var imageUrl: String? = null
)