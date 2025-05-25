package com.example.whatsappbusinessautomation // <-- IMPORTANTE: Verifique se este é o nome do SEU pacote


object ApiConstants {
    const val BASE_URL = "https://ynhukhcjjevmbuwgzodx.supabase.co/functions/v1"
    const val CHECK_TRIGGER_URL = "$BASE_URL/android-trigger-check"
    const val LIST_TRIGGERS_URL = "$BASE_URL/triggers-api/list"
}
