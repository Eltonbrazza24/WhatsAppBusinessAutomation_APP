package com.example.whatsappbusinessautomation

import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import android.util.Log
import java.io.IOException

/**
 * Serviço para comunicação com a API de gatilhos
 */
class ApiService {

    private val client = OkHttpClient()
    private val TAG = "ApiService"

    companion object {
        private const val CHECK_TRIGGER_URL = "https://ynhukhcjjevmbuwgzodx.supabase.co/functions/v1/android-trigger-check"
        private const val LIST_TRIGGERS_URL = "https://ynhukhcjjevmbuwgzodx.supabase.co/functions/v1/triggers-api/list"
    }

    /**
     * Verifica se uma mensagem contém um gatilho e retorna a resposta
     */
    fun checkTrigger(message: String, callback: (TriggerResponse?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Verificando gatilho para mensagem: $message")

                // Preparar JSON body
                val json = JSONObject().put("message", message)
                val requestBody = json.toString().toRequestBody("application/json".toMediaType())

                // Criar requisição POST
                val request = Request.Builder()
                    .url(CHECK_TRIGGER_URL)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                // Executar requisição
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "Resposta da API: $responseBody")

                        if (responseBody != null) {
                            val triggerResponse = parseTriggerResponse(responseBody)

                            // Retornar resultado na thread principal
                            withContext(Dispatchers.Main) {
                                callback(triggerResponse)
                            }
                        } else {
                            Log.e(TAG, "Corpo da resposta está vazio")
                            withContext(Dispatchers.Main) {
                                callback(null)
                            }
                        }
                    } else {
                        Log.e(TAG, "Erro na requisição: ${response.code} - ${response.message}")
                        withContext(Dispatchers.Main) {
                            callback(null)
                        }
                    }
                }

            } catch (e: IOException) {
                Log.e(TAG, "Erro de rede ao verificar gatilho", e)
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro inesperado ao verificar gatilho", e)
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    /**
     * Lista todos os gatilhos disponíveis (opcional)
     */
    fun listTriggers(callback: (List<Trigger>?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Listando gatilhos disponíveis")

                // Criar requisição GET
                val request = Request.Builder()
                    .url(LIST_TRIGGERS_URL)
                    .get()
                    .build()

                // Executar requisição
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "Lista de gatilhos: $responseBody")

                        if (responseBody != null) {
                            val triggers = parseTriggersList(responseBody)

                            // Retornar resultado na thread principal
                            withContext(Dispatchers.Main) {
                                callback(triggers)
                            }
                        } else {
                            Log.e(TAG, "Corpo da resposta está vazio")
                            withContext(Dispatchers.Main) {
                                callback(null)
                            }
                        }
                    } else {
                        Log.e(TAG, "Erro na requisição: ${response.code} - ${response.message}")
                        withContext(Dispatchers.Main) {
                            callback(null)
                        }
                    }
                }

            } catch (e: IOException) {
                Log.e(TAG, "Erro de rede ao listar gatilhos", e)
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro inesperado ao listar gatilhos", e)
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    /**
     * Converte a resposta JSON em objeto TriggerResponse
     */
    private fun parseTriggerResponse(jsonString: String): TriggerResponse? {
        return try {
            val json = JSONObject(jsonString)

            TriggerResponse(
                found = json.optBoolean("found", false),
                response = json.optString("response", null),
                trigger = json.optString("trigger", null),
                error = json.optString("error", null)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer parse da resposta", e)
            null
        }
    }

    /**
     * Converte a resposta JSON em lista de gatilhos
     */
    private fun parseTriggersList(jsonString: String): List<Trigger>? {
        return try {
            val json = JSONObject(jsonString)
            val triggersArray = json.optJSONArray("triggers") ?: return emptyList()

            val triggers = mutableListOf<Trigger>()
            for (i in 0 until triggersArray.length()) {
                val triggerJson = triggersArray.getJSONObject(i)
                triggers.add(
                    Trigger(
                        id = triggerJson.optString("id"),
                        keyword = triggerJson.optString("keyword"),
                        response = triggerJson.optString("response"),
                        active = triggerJson.optBoolean("active", true)
                    )
                )
            }

            triggers
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer parse da lista de gatilhos", e)
            null
        }
    }
}