package com.levent.yokla

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.levent.yokla.databinding.ItemCourseBinding

class CourseAdapter(private val courses: List<Course>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]

        // 1. Ders Bilgilerini Bağla (Modeldeki yeni isimlendirmeye göre güncellendi)
        holder.binding.tvCourseName.text = course.courseName // 'name' yerine 'courseName'
        holder.binding.tvTeacherName.text = course.teacherName ?: "Öğretim Görevlisi"

        // 2. Katılım Sayacı
        holder.binding.tvAttendanceCount.text = "Katılım: ${course.attendanceCount}/14"

        // 3. Ders İkonunu Yükle (Coil Kütüphanesi ile)
        if (!course.imageUrl.isNullOrEmpty()) {
            holder.binding.ivCourseIcon.load(course.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_book_open)
                error(R.drawable.ic_book_open)
                transformations(CircleCropTransformation())
            }
        } else {
            holder.binding.ivCourseIcon.setImageResource(R.drawable.ic_book_open)
        }

        // 4. Detay/Geçmiş Sayfasına Gitme Mantığı
        // CourseAdapter.kt içinde onBindViewHolder'daki ilgili kısım
        val navigateToHistory = {
            val context = holder.itemView.context
            val intent = Intent(context, HistoryActivity::class.java).apply {
                // 'course.id' veritabanındaki UUID'dir, 'course.courseName' ise dersin adıdır
                putExtra("COURSE_ID", course.id)
                putExtra("COURSE_NAME", course.courseName)
            }
            context.startActivity(intent)
        }

        // Kartın kendisine veya ikona tıklandığında geçmişi aç
        holder.binding.ivGoHistory.setOnClickListener { navigateToHistory() }
        holder.itemView.setOnClickListener { navigateToHistory() }
    }

    override fun getItemCount(): Int = courses.size
}