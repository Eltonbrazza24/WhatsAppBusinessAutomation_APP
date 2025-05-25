package com.example.whatsappbusinessautomation // <-- IMPORTANTE: Ajuste para o nome do SEU pacote

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView

class WebAppInterface(private val context: Context, private val webView: WebView) {

    private var responseCallback: ((String) -> Unit)? = null

    companion object {
        const val JS_INTERFACE_NAME = "AndroidInterface"
    }

    @JavascriptInterface
    fun postResponse(response: String) {
        Log.d("WebAppInterface", "Resposta recebida do JavaScript: $response")
        Handler(Looper.getMainLooper()).post {
            responseCallback?.invoke(response)
            responseCallback = null
        }
    }

    fun getResponseForTrigger(trigger: String, callback: (String) -> Unit) {
        Log.d("WebAppInterface", "Pedindo resposta para o gatilho: $trigger")
        this.responseCallback = callback
        Handler(Looper.getMainLooper()).post {
            val escapedTrigger = trigger.replace("\"", "\\\"").replace("\"", "\\\"") // Escapa aspas
            val jsCode = "javascript:findResponseForTrigger(\"$escapedTrigger\")"
            Log.d("WebAppInterface", "Executando JS: $jsCode")
            webView.evaluateJavascript(jsCode, null)
        }
    }
}
