package com.example.whatsappbusinessautomation // <-- IMPORTANTE: Verifique se este é o nome do SEU pacote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity principal que contém a WebView para carregar o dashboard online
 * e controles para gerenciar o serviço de acessibilidade.
 */
class MainActivity : AppCompatActivity() {

    // Declaração da interface de comunicação
    private lateinit var webAppInterface: WebAppInterface

    // Objeto para compartilhar a referência da interface com o serviço
    companion object {
        var sharedWebAppInterface: WebAppInterface? = null
    }

    // Declaração das Views da interface
    private lateinit var webView: WebView
    private lateinit var serviceStatusIndicator: View
    private lateinit var serviceStatusText: TextView
    private lateinit var btnAccessibilitySettings: Button

    // URL do dashboard online
    private val dashboardUrl = "https://bxxcvjoa.manus.space"

    override fun onCreate(savedInstanceState: Bundle? ) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa as views
        webView = findViewById(R.id.webView)
        serviceStatusIndicator = findViewById(R.id.serviceStatusIndicator)
        serviceStatusText = findViewById(R.id.serviceStatusText)
        btnAccessibilitySettings = findViewById(R.id.btnAccessibilitySettings)

        // Configura a WebView (incluindo a interface JavaScript)
        setupWebView()

        // Configura o botão para abrir as configurações de acessibilidade
        btnAccessibilitySettings.setOnClickListener {
            openAccessibilitySettings()
        }

        // Verifica o status inicial do serviço de acessibilidade
        updateServiceStatus()
    }

    /**
     * Configura a WebView para carregar o dashboard online e habilita a interface JavaScript.
     */
    private fun setupWebView() {
        // Configurações básicas da WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.setSupportZoom(true)
        webView.settings.domStorageEnabled = true // Necessário para localStorage

        // --- ORDEM CORRIGIDA ---
        // 1. Cria a instância da interface
        webAppInterface = WebAppInterface(this, webView)
        // 2. Adiciona a interface à WebView, tornando-a acessível pelo JavaScript
        webView.addJavascriptInterface(webAppInterface, WebAppInterface.JS_INTERFACE_NAME)
        // 3. Guarda a referência para o serviço usar
        sharedWebAppInterface = webAppInterface
        // -----------------------

        // 4. Configura o WebViewClient para tratar navegação dentro da WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Mantém a navegação dentro da WebView
                return false
            }
            // Você pode adicionar outros métodos aqui se necessário (onPageFinished, etc.)
        }

        // 5. Carrega o dashboard POR ÚLTIMO
        webView.loadUrl(dashboardUrl)
    }

    /**
     * Abre as configurações de acessibilidade do Android.
     */
    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(
                this,
                "Ative o serviço \"WhatsApp Business Automation\" na lista", // Ajuste o nome se necessário
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Não foi possível abrir as configurações de acessibilidade",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Verifica se o serviço de acessibilidade está ativo
     * e atualiza a interface de acordo.
     */
    private fun updateServiceStatus() {
        val isServiceEnabled = isAccessibilityServiceEnabled()

        if (isServiceEnabled) {
            serviceStatusIndicator.setBackgroundResource(R.drawable.status_indicator_green)
            serviceStatusText.text = "Serviço Ativo"
        } else {
            serviceStatusIndicator.setBackgroundResource(R.drawable.status_indicator_red)
            serviceStatusText.text = "Serviço Inativo"
        }
    }

    /**
     * Verifica se o serviço de acessibilidade está habilitado
     * nas configurações do Android.
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        // TODO: Implementar verificação real do serviço de acessibilidade.
        // Esta é uma implementação temporária que sempre retorna falso.
        // A implementação real envolve verificar as configurações do sistema.
        // Exemplo (requer pesquisa adicional para ser robusto):
        /*
        val service = packageName + "/" + WhatsAppAccessibilityService::class.java.canonicalName
        try {
            val accessibilityEnabled = Settings.Secure.getInt(
                applicationContext.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED
            )
            val settingValue = Settings.Secure.getString(
                applicationContext.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (accessibilityEnabled == 1 && settingValue != null) {
                val splitter = TextUtils.SimpleStringSplitter(':')
                splitter.setString(settingValue)
                while (splitter.hasNext()) {
                    if (splitter.next().equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        } catch (e: Settings.SettingNotFoundException) {
            // Log.e("MainActivity", "Error finding setting, default accessibility to not found: " + e.message)
        }
        */
        return false // Mantenha false por enquanto
    }

    override fun onResume() {
        super.onResume()
        // Atualiza o status do serviço quando a activity volta ao primeiro plano
        updateServiceStatus()
    }
}
