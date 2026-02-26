package com.levent.yokla

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.levent.yokla.databinding.ActivityLoginBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Giriş Yap Butonu
        binding.btnLogin.setOnClickListener {
            val emailStr = binding.etEmail.text.toString().trim()
            val passStr = binding.etPassword.text.toString().trim()

            if (emailStr.isNotEmpty() && passStr.isNotEmpty()) {
                loginUser(emailStr, passStr)
            } else {
                Toast.makeText(this, "Lütfen e-posta ve şifre giriniz", Toast.LENGTH_SHORT).show()
            }
        }

        // Kayıt Ol sayfasına yönlendirme
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(emailStr: String, passStr: String) {
        lifecycleScope.launch {
            try {
                // Giriş sırasında butonu pasif yapalım (çift tıklamayı önlemek için)
                binding.btnLogin.isEnabled = false

                // Supabase Auth Giriş (Güncel 3.0.0 Sözdizimi)
                SupabaseClient.client.auth.signInWith(Email) {
                    email = emailStr
                    password = passStr
                }

                Toast.makeText(this@LoginActivity, "Hoş geldiniz!", Toast.LENGTH_SHORT).show()

                // Ana ekrana geçiş
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                binding.btnLogin.isEnabled = true
                Toast.makeText(this@LoginActivity, "Giriş Başarısız: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}