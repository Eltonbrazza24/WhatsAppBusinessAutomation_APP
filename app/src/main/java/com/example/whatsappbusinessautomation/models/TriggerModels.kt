package com.example.whatsappbusinessautomation

/**
 * Resposta da API ao verificar um gatilho
 */
data class TriggerResponse(
    val found: Boolean,           // Se o gatilho foi encontrado
    val response: String?,        // A resposta a ser enviada
    val trigger: String?,         // O gatilho que foi encontrado
    val error: String?            // Mensagem de erro (se houver)
)

/**
 * Modelo de um gatilho individual
 */
data class Trigger(
    val id: String,               // ID único do gatilho
    val keyword: String,          // A palavra-chave (ex: #PRECO)
    val response: String,         // A resposta automática
    val active: Boolean = true    // Se o gatilho está ativo
)