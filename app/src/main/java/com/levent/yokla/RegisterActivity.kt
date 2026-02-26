package com.levent.yokla

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.levent.yokla.databinding.ActivityRegisterBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Spinner içeriğini doldur
        val classes = arrayOf("1. Sınıf", "2. Sınıf", "3. Sınıf", "4. Sınıf")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, classes)
        binding.spinnerClassLevel.adapter = adapter

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val studentNo = binding.etStudentNumber.text.toString().trim()
            val emailStr = binding.etEmail.text.toString().trim()
            val passStr = binding.etPassword.text.toString().trim()
            val selectedLevel = binding.spinnerClassLevel.selectedItemPosition + 1

            if (fullName.isEmpty() || emailStr.isEmpty() || passStr.isEmpty() || studentNo.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passStr.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(emailStr, passStr, fullName, studentNo, selectedLevel)
        }
    }

    private fun registerUser(emailStr: String, passStr: String, name: String, sNo: String, level: Int) {
        lifecycleScope.launch {
            try {
                binding.btnRegister.isEnabled = false

                // 1. Kullanıcıyı Auth sistemine kaydet
                val response = SupabaseClient.client.auth.signUpWith(Email) {
                    email = emailStr
                    password = passStr
                }

                // 2. Yeni oluşan UID'yi al (response üzerinden almak daha güvenlidir)
                val userId = response?.id ?: SupabaseClient.client.auth.currentUserOrNull()?.id

                if (userId != null) {
                    // 3. Veritabanı modelini oluştur (classLevel dahil)
                    val newUser = UserProfile(
                        id = userId,
                        fullName = name,
                        role = "student",
                        studentNumber = sNo,
                        classLevel = level
                    )

                    // 4. Veriyi 'users' tablosuna yaz
                    // Not: Veritabanında RLS kapalı veya 'insert' izni açık olmalı!
                    SupabaseClient.client.from("users").upsert(newUser)

                    Toast.makeText(this@RegisterActivity, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    throw Exception("Kullanıcı kimliği oluşturulamadı.")
                }

            } catch (e: Exception) {
                binding.btnRegister.isEnabled = true
                Toast.makeText(this@RegisterActivity, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}