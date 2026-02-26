package com.levent.yokla

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.levent.yokla.databinding.ActivityHistoryBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<AttendanceHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. GÜVENLİK KONTROLÜ: Ders ID'si gelmemişse sayfayı kapat
        val courseId = intent.getStringExtra("COURSE_ID")
        if (courseId.isNullOrEmpty()) {
            Toast.makeText(this, "Hata: Ders bilgisi alınamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val courseName = intent.getStringExtra("COURSE_NAME") ?: "Geçmiş"
        binding.tvHistoryTitle.text = "$courseName - Yoklama Durumu"

        setupRecyclerView()

        // 2. Verileri çekmeye başla
        fetchEnhancedHistory(courseId, courseName)
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(historyList)
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun fetchEnhancedHistory(courseId: String, courseName: String) {
        lifecycleScope.launch {
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch

                // 1. Derse ait tüm yoklama oturumlarını (session) çek
                val allSessions = SupabaseClient.client.from("attendance_sessions")
                    .select(Columns.ALL) {
                        filter { eq("course_id", courseId) }
                    }.decodeList<AttendanceSession>()

                // 2. Öğrencinin kendi yoklama kayıtlarını çek
                val myRecords = SupabaseClient.client.from("attendance_records")
                    .select(Columns.ALL) {
                        filter { eq("student_id", userId) }
                    }.decodeList<AttendanceRecord>()

                val processedList = mutableListOf<AttendanceHistoryItem>()

                // 3. Karşılaştırma ve Listeleme Mantığı
                allSessions.forEach { session ->
                    val record = myRecords.find { it.sessionId == session.id }
                    val rawDate = session.createdAt ?: ""
                    val formattedDate = formatDisplayDate(rawDate)

                    processedList.add(
                        AttendanceHistoryItem(
                            date = formattedDate,
                            status = if (record != null) "GELDİ" else "GİTMEDİ",
                            sessionId = session.id
                        )
                    )
                }

                // 4. Arayüzü Güncelle
                historyList.clear()
                historyList.addAll(processedList.sortedByDescending { it.date })
                historyAdapter.notifyDataSetChanged()

                // 5. Devamsızlık Özeti
                val absentCount = processedList.count { it.status == "GİTMEDİ" }
                binding.tvHistoryTitle.text = "$courseName\nDevamsızlık: $absentCount Gün"

                if (absentCount > 4) {
                    Toast.makeText(this@HistoryActivity, "Dikkat! Devamsızlık sınırına yaklaştınız.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("YoklaHistory", "Veri çekme hatası: ${e.message}")
                Toast.makeText(this@HistoryActivity, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDisplayDate(rawDate: String): String {
        return try {
            if (rawDate.isEmpty()) return "Bilinmeyen Tarih"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr"))
            val date = inputFormat.parse(rawDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            rawDate.split("T")[0]
        }
    }
}

data class AttendanceHistoryItem(
    val date: String,
    val status: String, // "GELDİ" veya "GİTMEDİ"
    val sessionId: String
)