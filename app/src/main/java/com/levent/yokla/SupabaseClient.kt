package com.levent.yokla

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.android.Android


object SupabaseClient {

    private const val SUPABASE_URL = ""

    // 2. KRİTİK HATA: Aşağıdaki anahtar (Anon Key) yanlış.
    // Panelden "anon public" olan ve 'eyJ...' ile başlayan uzun anahtarı buraya yapıştır.
    private const val SUPABASE_KEY = ""

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
