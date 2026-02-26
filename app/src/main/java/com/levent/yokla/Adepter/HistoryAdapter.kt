package com.levent.yokla

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.levent.yokla.databinding.ItemHistoryBinding

class HistoryAdapter(private val historyList: List<AttendanceHistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]

        // 1. Tarih ve Saat Parçalama (2026-02-22T11:00:00 -> 22.02.2026 | 11:00)
        val fullDate = item.date
        val datePart = fullDate.split("T").getOrNull(0) ?: fullDate
        val timePart = fullDate.split("T").getOrNull(1)?.take(5) ?: ""
        holder.binding.tvHistoryDate.text = "$datePart | $timePart"

        // 2. Duruma Göre Renk ve İkon Yönetimi
        if (item.status == "GELDİ") {
            // Katıldı: Yeşil Renk ve Tık İkonu
            holder.binding.ivStatusIcon.setImageResource(R.drawable.ic_check)
            holder.binding.statusIconContainer.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#4CAF50"))

            holder.binding.tvHistoryStatusText.text = "Katılım Sağlandı"
            holder.binding.tvHistoryStatusText.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            // Gitmedi: Kırmızı Renk ve Çarpı İkonu
            holder.binding.ivStatusIcon.setImageResource(R.drawable.ic_close)
            holder.binding.statusIconContainer.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#EF4444"))

            holder.binding.tvHistoryStatusText.text = "Katılım Sağlanmadı"
            holder.binding.tvHistoryStatusText.setTextColor(Color.parseColor("#EF4444"))
        }
    }

    override fun getItemCount(): Int = historyList.size
}