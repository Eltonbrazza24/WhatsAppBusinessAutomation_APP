package com.example.whatsappbusinessautomation // <-- IMPORTANTE: Verifique se este é o nome do SEU pacote

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Serviço de Acessibilidade para monitorar notificações do WhatsApp Business
 * e responder automaticamente a mensagens com gatilhos.
 */
class WhatsAppAccessibilityService : AccessibilityService() {

    companion object {
        const val API_URL = "https://ynhukhcjjevmbuwgzodx.supabase.co/functions/v1/android-trigger-check"
    }

    private val TAG = "WhatsAppAccessibilityService"
    private val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

    private val apiService = ApiService()

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Log detalhado do evento (útil para depuração)
        // Log.d(TAG, "Evento recebido: ${AccessibilityEvent.eventTypeToString(event.eventType)}, Pacote: ${event.packageName}")

        // Verifica se o evento é uma notificação do WhatsApp Business
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED &&
            event.packageName == WHATSAPP_BUSINESS_PACKAGE) {

            // Extrai o texto da notificação
            val messageText = getNotificationText(event)
            if (messageText.isNotEmpty()) {
                Log.d(TAG, "Texto da notificação extraído: $messageText")

                // Verifica se a mensagem contém um gatilho (palavra que começa com #)
                val trigger = extractTrigger(messageText)
                if (trigger.isNotEmpty()) {
                    Log.i(TAG, "Gatilho encontrado: '$trigger'")

                    // Usar o novo método processMessage com ApiService
                    processMessage(messageText)

                } else {
                    // Log.d(TAG, "Nenhum gatilho '#' encontrado na mensagem.")
                }
            } else {
                // Log.d(TAG, "Texto da notificação está vazio ou não pôde ser extraído.")
            }
        }
        // Você pode adicionar mais condições aqui para outros tipos de eventos se necessário
        // por exemplo, TYPE_WINDOW_STATE_CHANGED para interagir com a janela do chat
    }

    /**
     * Extrai o texto de uma notificação.
     */
    private fun getNotificationText(event: AccessibilityEvent): String {
        val parcelable = event.parcelableData ?: return ""
        if (parcelable !is android.app.Notification) return ""

        val notification = parcelable as android.app.Notification
        val extras = notification.extras

        // Tenta obter o texto principal da notificação
        val title = extras?.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras?.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""

        // Log para depuração
        // Log.d(TAG, "Título da Notificação: $title")
        // Log.d(TAG, "Texto da Notificação: $text")

        // Retorna o texto principal (geralmente contém a mensagem)
        // Pode precisar de ajustes dependendo da versão do WhatsApp/Android
        return text
    }

    /**
     * Extrai o primeiro gatilho (palavra iniciada com #) de uma mensagem.
     */
    private fun extractTrigger(message: String): String {
        // Divide a mensagem em palavras
        val words = message.split(Regex("\\s+")) // CORREÇÃO: Escapa a barra invertida

        // Encontra a primeira palavra que começa com #
        return words.find { it.startsWith("#") } ?: ""
    }

    /**
     * Solicita a resposta para um gatilho usando a WebAppInterface.
     * A resposta é recebida de forma assíncrona no callback.
     */
    private fun getResponseForTrigger(trigger: String) {
        Log.d(TAG, "Solicitando resposta para o gatilho: $trigger")

        // Acessa a interface compartilhada pela MainActivity
        val webInterface = MainActivity.sharedWebAppInterface

        if (webInterface == null) {
            Log.e(TAG, "Erro Crítico: WebAppInterface não está disponível! A MainActivity pode não estar ativa ou a referência não foi compartilhada.")
            // Sem a interface, não podemos obter a resposta.
            return
        }

        // Chama a função da interface para pedir a resposta ao JavaScript
        // O resultado virá de forma assíncrona no callback
        webInterface.getResponseForTrigger(trigger) { result ->
            // Este código dentro do callback será executado DEPOIS que o JavaScript responder
            Log.d(TAG, "Callback recebido da WebView com resultado: $result")

            // Processa o resultado recebido do JavaScript
            when {
                result.startsWith("FOUND:") -> {
                    val response = result.substringAfter("FOUND:")
                    Log.i(TAG, "Resposta encontrada para '$trigger': $response")
                    // Chama a função para enviar a resposta
                    sendAutomaticResponse(response)
                }
                result.startsWith("NOT_FOUND:") -> {
                    Log.d(TAG, "Gatilho '$trigger' não encontrado no dashboard ou está inativo.")
                    // Nenhuma ação necessária se não encontrou
                }
                result.startsWith("ERROR:") -> {
                    val errorMsg = result.substringAfter("ERROR:")
                    Log.e(TAG, "Erro retornado pelo JavaScript ao buscar gatilho '$trigger': $errorMsg")
                }
                else -> {
                    // Caso a resposta não siga o padrão esperado
                    Log.w(TAG, "Resultado inesperado recebido da WebView: $result")
                }
            }
        }
    }

    private fun checkTrigger(message: String) {
        Log.d(TAG, "Verificando gatilho na mensagem: $message")

        apiService.checkTrigger(message) { response ->
            response?.let {
                if (it.found && !it.response.isNullOrEmpty()) {
                    Log.i(TAG, "Gatilho '${it.trigger}' encontrado. Enviando resposta: ${it.response}")
                    sendWhatsAppMessage(it.response)
                } else {
                    Log.d(TAG, "Nenhum gatilho ativo encontrado na mensagem")
                }

                if (!it.error.isNullOrEmpty()) {
                    Log.e(TAG, "Erro da API: ${it.error}")
                }
            } ?: run {
                Log.e(TAG, "Falha ao verificar gatilho - resposta nula da API")
            }
        }
    }

    private fun sendWhatsAppMessage(response: String) {
        Log.i(TAG, "Enviando mensagem automática: '$response'")

        // Usar a implementação existente de sendAutomaticResponse
        sendAutomaticResponse(response)
    }

    private fun processMessage(messageText: String) {
        apiService.checkTrigger(messageText) { response ->
            response?.let {
                if (it.found && it.response != null) {
                    // Enviar resposta automática
                    sendWhatsAppMessage(it.response)
                }
            }
        }
    }

    /**
     * Tenta enviar a resposta automática interagindo com a UI do WhatsApp Business.
     * ATENÇÃO: Esta função é TEÓRICA e NÃO TESTADA. Requer ajustes e testes no dispositivo real.
     */
    private fun sendAutomaticResponse(response: String) {
        Log.i(TAG, "Tentando enviar resposta automática: '$response'")

        // Obtém o nó raiz da janela ativa (pode ser a notificação ou a janela do chat)
        val rootNode = rootInActiveWindow ?: run {
            Log.e(TAG, "Nó raiz (rootInActiveWindow) não encontrado. Não é possível interagir com a UI.")
            return
        }

        // --- Estratégia 1: Tentar responder pela Ação da Notificação (Mais seguro) ---
        // (Esta parte é complexa e depende da implementação da notificação pelo WhatsApp)
        // val replyAction = findReplyAction(rootNode) // Função auxiliar necessária
        // if (replyAction != null) { ... Lógica para usar RemoteInput ... }

        // --- Estratégia 2: Interagir diretamente com a UI (Mais frágil) ---
        Log.d(TAG, "Tentando interação direta com a UI...")

        // IDs de exemplo - PRECISAM SER VERIFICADOS E AJUSTADOS no seu dispositivo!
        val inputFieldId = "com.whatsapp.w4b:id/entry"
        val sendButtonId = "com.whatsapp.w4b:id/send"

        // Encontrar o campo de texto
        val inputNodes = rootNode.findAccessibilityNodeInfosByViewId(inputFieldId)
        val inputNode = inputNodes?.firstOrNull { it.isEditable }

        if (inputNode == null) {
            Log.e(TAG, "Campo de texto ('$inputFieldId') não encontrado ou não editável.")
            // Tentar logar a hierarquia para depuração (se possível)
            // logNodeHierarchy(rootNode)
            rootNode.recycle()
            return
        }
        Log.d(TAG, "Campo de texto encontrado.")

        // Preencher o campo de texto
        val arguments = Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, response)
        val successInput = inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        Log.d(TAG, "Preenchimento do campo de texto: ${if(successInput) "Sucesso" else "Falha"}")

        // Pequena pausa antes de clicar em enviar (pode ajudar)
        Thread.sleep(200) // 200 milissegundos

        // Encontrar o botão de enviar
        val sendButtonNodes = rootNode.findAccessibilityNodeInfosByViewId(sendButtonId)
        val sendButtonNode = sendButtonNodes?.firstOrNull { it.isClickable }

        if (sendButtonNode == null) {
            Log.e(TAG, "Botão de enviar ('$sendButtonId') não encontrado ou não clicável.")
            inputNode.recycle()
            rootNode.recycle()
            return
        }
        Log.d(TAG, "Botão de enviar encontrado.")

        // Clicar no botão de enviar
        val successClick = sendButtonNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Log.d(TAG, "Clique no botão de enviar: ${if(successClick) "Sucesso" else "Falha"}")

        // Limpar recursos
        inputNode.recycle()
        sendButtonNode.recycle()
        rootNode.recycle()

        if (!successInput || !successClick) {
            Log.w(TAG, "Falha ao preencher o texto ou clicar em enviar.")
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Serviço de acessibilidade interrompido.")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Serviço de acessibilidade conectado.")
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED // Monitora apenas notificações
        // Se precisar interagir com a janela, adicione: | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.packageNames = arrayOf(WHATSAPP_BUSINESS_PACKAGE) // Apenas do WhatsApp Business
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        // info.notificationTimeout = 100 // Timeout em ms (opcional)
        // info.flags = AccessibilityServiceInfo.DEFAULT // Flags padrão
        // Se precisar recuperar conteúdo da janela:
        // info.flags = info.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS

        this.serviceInfo = info
        Log.d(TAG, "Configuração do serviço aplicada.")
    }
}