package com.levent.yokla

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.levent.yokla.databinding.ActivityProfileBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var selectedImageUri: Uri? = null

    // Galeriden resim seçme işlemi
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Seçilen resmi anında ekranda göster (Daire içine alarak)
            binding.ivProfileLarge.load(it) {
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserProfile()

        // Artı butonuna basınca galeriyi aç
        binding.ivAddPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnUpdateProfile.setOnClickListener {
            updateUserProfile()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val userProfile = SupabaseClient.client.from("users")
                        .select { filter { eq("id", userId) } }
                        .decodeSingle<UserProfile>()

                    binding.etFullName.setText(userProfile.fullName)
                    binding.etStudentNumber.setText(userProfile.studentNumber)

                    // Supabase'den gelen resim URL'sini yükle
                    if (!userProfile.avatarUrl.isNullOrEmpty()) {
                        binding.ivProfileLarge.load(userProfile.avatarUrl) {
                            crossfade(true)
                            placeholder(android.R.drawable.ic_menu_myplaces)
                            transformations(CircleCropTransformation())
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Bilgiler çekilemedi: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserProfile() {
        val newName = binding.etFullName.text.toString().trim()
        val newNo = binding.etStudentNumber.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Ad Soyad boş olamaz", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                if (userId != null) {

                    var avatarUrl: String? = null

                    // Eğer yeni bir resim seçildiyse Supabase Storage'a yükle
                    selectedImageUri?.let { uri ->
                        val bytes = contentResolver.openInputStream(uri)?.readBytes()
                        if (bytes != null) {
                            val fileName = "$userId.jpg"

                            // HATA DÜZELTİLDİ: upload fonksiyonu lambda (dsl) yapısı kullanmalı
                            SupabaseClient.client.storage.from("avatars").upload(fileName, bytes) {
                                upsert = true
                            }

                            avatarUrl = SupabaseClient.client.storage.from("avatars").publicUrl(fileName)
                        }
                    }

                    // Veritabanını güncelle
                    SupabaseClient.client.from("users").update(
                        {
                            set("full_name", newName)
                            set("student_number", newNo)
                            if (avatarUrl != null) set("avatar_url", avatarUrl)
                        }
                    ) {
                        filter { eq("id", userId) }
                    }

                    Toast.makeText(this@ProfileActivity, "Profil başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Güncelleme hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Çıkış hatası", Toast.LENGTH_SHORT).show()
            }
        }
    }
}