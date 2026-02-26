package com.levent.yokla

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.android.Android


object SupabaseClient {

    private const val SUPABASE_URL = "https://bpsmfhqkfgonxzuehmkr.supabase.co"

    // 2. KRİTİK HATA: Aşağıdaki anahtar (Anon Key) yanlış.
    // Panelden "anon public" olan ve 'eyJ...' ile başlayan uzun anahtarı buraya yapıştır.
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJwc21maHFrZmdvbnh6dWVobWtyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE1ODMyNjUsImV4cCI6MjA4NzE1OTI2NX0.Z6uJFIuPHM_VG5r3GP7cuKELzJq0qiupFf6Swz8VBeM"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        httpEngine = Android.create()

        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}