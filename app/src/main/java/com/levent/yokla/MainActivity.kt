package com.levent.yokla

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.levent.yokla.databinding.ActivityMainBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.OffsetDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var courseAdapter: CourseAdapter
    private val coursesList = mutableListOf<Course>()
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupButtons()

        // Verileri ve Realtime dinleyicileri başlat
        fetchCourses()
        listenForAnnouncements()
    }

    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(coursesList)
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = courseAdapter
        }
    }

    private fun setupButtons() {
        binding.ivProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnQrRead.setOnClickListener {
            checkLocationPermissionAndScan()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    fetchCourses()
                    true
                }
                R.id.nav_history -> {
                    // DÜZELTME: Buradan direkt sayfayı açma!
                    // Kullanıcıya ders listesinden bir ders seçmesi gerektiğini hatırlat.
                    Toast.makeText(this, "Lütfen geçmişini görmek istediğiniz dersin üzerine tıklayın!", Toast.LENGTH_LONG).show()
                    false // Menüde 'Geçmiş' ikonuna basılmasını engelle, ana sayfada kalsın.
                }
                else -> false
            }
        }
    }
    private fun fetchCourses() {
        lifecycleScope.launch {
            try {
                // 1. Giriş yapan kullanıcının ID'sini al
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch

                // 2. ÖNCE PROFİLİ ÇEK (Sınıf seviyesini öğrenmek için)
                val profile = SupabaseClient.client.from("users")
                    .select() {
                        filter { eq("id", userId) }
                    }.decodeSingle<UserProfile>()

                // 3. ŞİMDİ DERSLERİ ÇEK (Öğrencinin sınıfına göre)
                val fetchedCourses = SupabaseClient.client.from("courses")
                    .select() {
                        filter { eq("class_level", profile.classLevel ?: 1) }
                    }
                    .decodeList<Course>()

                // 4. ARAYÜZÜ GÜNCELLE
                runOnUiThread {
                    coursesList.clear()
                    coursesList.addAll(fetchedCourses)
                    courseAdapter.notifyDataSetChanged() // Listeyi ekrana bastırır

                    if (coursesList.isEmpty()) {
                        binding.rvCourses.visibility = View.GONE
                        // Ders yoksa kullanıcıya bilgi verilebilir
                    } else {
                        binding.rvCourses.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e("YoklaMain", "Ders çekme hatası: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Dersler yüklenemedi!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun listenForAnnouncements() {
        lifecycleScope.launch {
            try {
                val channel = SupabaseClient.client.realtime.channel("announcements_channel")
                val insertFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "announcements"
                }
                channel.subscribe()

                insertFlow.collect { action ->
                    val message = action.record["message"]?.toString() ?: ""
                    if (message.isNotEmpty()) showAnnouncementDialog(message)
                }
            } catch (e: Exception) {
                Log.e("Realtime", "Duyuru dinleme hatası: ${e.message}")
            }
        }
    }

    private fun showAnnouncementDialog(message: String) {
        runOnUiThread {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📢 Hocadan Duyuru")
                .setMessage(message)
                .setPositiveButton("Tamam", null)
                .show()
        }
    }

    private fun checkLocationPermissionAndScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            startQrScanner()
        }
    }

    private fun startQrScanner() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(this, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                barcode.rawValue?.let { sessionId -> validateAndSaveAttendance(sessionId) }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Tarayıcı Hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("MissingPermission")
    private fun validateAndSaveAttendance(sessionId: String) {
        lifecycleScope.launch {
            try {
                val session = SupabaseClient.client.from("attendance_sessions")
                    .select(Columns.ALL) { filter { eq("id", sessionId) } }
                    .decodeSingleOrNull<AttendanceSession>() ?: throw Exception("Geçersiz QR!")

                val expiresAt = OffsetDateTime.parse(session.expiresAt)
                if (OffsetDateTime.now().isAfter(expiresAt)) throw Exception("QR süresi dolmuş!")

                val lastLocation = fusedLocationClient.lastLocation.await()
                if (lastLocation != null) {
                    val distance = calculateDistance(lastLocation.latitude, lastLocation.longitude, session.lat ?: 0.0, session.lon ?: 0.0)
                    if (distance > 10000) throw Exception("Sınıfta değilsiniz! (${distance.toInt()}m)")
                }

                saveToDatabase(sessionId)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun saveToDatabase(sessionId: String) {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return
        try {
            val record = AttendanceRecord(studentId = userId, sessionId = sessionId, status = "geldi")
            SupabaseClient.client.from("attendance_records").insert(record)
            showSuccessFeedback()
        } catch (e: Exception) {
            Toast.makeText(this, "Zaten yoklama verdiniz!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateDistance(sLat: Double, sLon: Double, tLat: Double, tLon: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(sLat, sLon, tLat, tLon, results)
        return results[0]
    }

    private suspend fun showSuccessFeedback() {
        vibratePhone()
        binding.successPanel.visibility = View.VISIBLE
        delay(2000)
        binding.successPanel.visibility = View.GONE
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        else vibrator.vibrate(200)
    }
}