package com.example.whatsappbusinessautomation // <-- IMPORTANTE: Verifique se este é o nome do SEU pacote

data class TriggerResponse(
    val found: Boolean,
    val response: String? = null,
    val trigger: String? = null
)
