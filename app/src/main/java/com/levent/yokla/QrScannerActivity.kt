package com.levent.yokla

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class QrScannerActivity : AppCompatActivity() {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Arka planın boş kalmaması için varsa bir layout set edebilirsin
        startQrScanner()
    }

    private fun startQrScanner() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                barcode.rawValue?.let { sessionId ->
                    validateAndSaveAttendance(sessionId)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Tarama Hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnCanceledListener { finish() }
    }

    @SuppressLint("MissingPermission")
    private fun validateAndSaveAttendance(sessionId: String) {
        lifecycleScope.launch {
            try {
                // 1. Oturumun geçerli olup olmadığını ve ders bilgisini çekiyoruz
                val session = SupabaseClient.client.from("attendance_sessions")
                    .select(Columns.ALL) {
                        filter { eq("id", sessionId) }
                    }.decodeSingleOrNull<AttendanceSession>()

                if (session == null) {
                    Toast.makeText(this@QrScannerActivity, "Geçersiz veya süresi dolmuş QR!", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }

                // 2. Mükerrer Kayıt Kontrolü (Aynı oturum için daha önce yoklama verilmiş mi?)
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch
                val existingRecord = SupabaseClient.client.from("attendance_records")
                    .select() {
                        filter {
                            eq("student_id", userId)
                            eq("session_id", sessionId)
                        }
                    }.decodeSingleOrNull<AttendanceRecord>()

                if (existingRecord != null) {
                    Toast.makeText(this@QrScannerActivity, "Bu QR kodunu zaten okuttunuz!", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }

                // 3. Konum Doğrulaması
                if (ActivityCompat.checkSelfPermission(this@QrScannerActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    val lastLocation: Location? = fusedLocationClient.lastLocation.await()

                    if (lastLocation != null) {
                        val distance = calculateDistance(
                            lastLocation.latitude, lastLocation.longitude,
                            session.lat ?: 0.0, session.lon ?: 0.0
                        )

                        // 50 metre sınırı
                        if (distance > 50) {
                            Toast.makeText(this@QrScannerActivity, "Sınıfta değilsiniz! Mesafe: ${distance.toInt()}m", Toast.LENGTH_LONG).show()
                            finish()
                            return@launch
                        }
                    }
                }

                // 4. Kayıt İşlemi
                saveToDatabase(sessionId, userId)

            } catch (e: Exception) {
                Log.e("QrScanner", "Hata: ${e.message}")
                Toast.makeText(this@QrScannerActivity, "Bir hata oluştu!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private suspend fun saveToDatabase(sessionId: String, userId: String) {
        try {
            val record = AttendanceRecord(
                studentId = userId,
                sessionId = sessionId,
                status = "geldi"
            )
            // Veritabanına ekleme
            SupabaseClient.client.from("attendance_records").insert(record)

            vibratePhone()
            Toast.makeText(this, "Yoklama Başarıyla Alındı!", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        } catch (e: Exception) {
            Log.e("QrScanner", "DB Kayıt Hatası: ${e.message}")
            Toast.makeText(this, "Kayıt yapılamadı!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun calculateDistance(sLat: Double, sLon: Double, tLat: Double, tLon: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(sLat, sLon, tLat, tLon, results)
        return results[0]
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }
}