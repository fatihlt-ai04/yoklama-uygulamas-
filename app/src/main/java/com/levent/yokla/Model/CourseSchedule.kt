package com.levent.yokla.Model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseSchedule(
    @SerialName("id") val id: String? = null,
    @SerialName("course_id") val courseId: String,
    @SerialName("day_of_week") val dayOfWeek: Int, // 1: Pazartesi...
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String
)
