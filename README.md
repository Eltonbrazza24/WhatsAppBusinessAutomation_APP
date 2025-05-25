# WhatsApp Business Automation

Este projeto é um aplicativo Android que automatiza respostas no WhatsApp Business usando gatilhos específicos. O aplicativo utiliza uma WebView para carregar um dashboard online onde os gatilhos e respostas são configurados.

## Estrutura do Projeto

- **MainActivity**: Tela principal com WebView que carrega o dashboard online
- **WhatsAppAccessibilityService**: Serviço de acessibilidade que monitora notificações do WhatsApp Business
- **Layout e recursos**: Arquivos XML para interface, strings e indicadores visuais

## Como Importar o Projeto no Android Studio

1. Abra o Android Studio
2. Selecione "Open an Existing Project"
3. Navegue até a pasta onde você extraiu este projeto e selecione-a
4. Aguarde o Android Studio importar e indexar o projeto

## Como Executar o Aplicativo

1. Conecte um dispositivo Android via USB ou configure um emulador
2. Clique no botão "Run" (triângulo verde) na barra de ferramentas
3. Selecione o dispositivo/emulador e aguarde a instalação

## Ativando o Serviço de Acessibilidade

Para que o aplicativo funcione corretamente, você precisa ativar o serviço de acessibilidade:

1. No aplicativo, clique no botão "Ativar Serviço de Acessibilidade"
2. Nas configurações do Android, encontre "WhatsApp Business Automation" na lista
3. Ative o serviço e conceda as permissões necessárias
4. Retorne ao aplicativo - o indicador de status deve ficar verde

## Como o Aplicativo Funciona

1. O dashboard online (carregado na WebView) permite configurar gatilhos e respostas
2. O serviço de acessibilidade monitora notificações do WhatsApp Business
3. Quando uma mensagem contendo um gatilho (ex: #PEDIDO123) é recebida, o aplicativo:
   - Identifica o gatilho na mensagem
   - Consulta o dashboard para obter a resposta apropriada
   - Envia a resposta automaticamente

## Próximos Passos para Desenvolvimento

### 1. Implementar a Integração entre WebView e Serviço de Acessibilidade

No arquivo `WhatsAppAccessibilityService.kt`, você precisa implementar:

```kotlin
private fun getResponseForTrigger(trigger: String): String {
    // TODO: Implementar a integração com a WebView para buscar a resposta
    // Dica: Use JavaScript Interface para comunicação entre WebView e código nativo
}
```

### 2. Implementar o Envio Automático de Respostas

No arquivo `WhatsAppAccessibilityService.kt`, você precisa implementar:

```kotlin
private fun sendAutomaticResponse(response: String) {
    // TODO: Implementar a lógica para abrir o WhatsApp Business e enviar a resposta
    // Dica: Use AccessibilityNodeInfo para interagir com a interface do WhatsApp
}
```

### 3. Melhorar a Verificação do Status do Serviço

No arquivo `MainActivity.kt`, você precisa implementar:

```kotlin
private fun isAccessibilityServiceEnabled(): Boolean {
    // TODO: Implementar verificação real do serviço
    // Dica: Use AccessibilityManager para verificar se o serviço está ativo
}
```

### 4. Adicionar Funcionalidades Extras (Opcionais)

- Histórico de respostas automáticas enviadas
- Configurações adicionais (tempo de espera, formato de resposta)
- Suporte a múltiplos gatilhos em uma única mensagem

## Recursos Úteis

- [Documentação de Serviços de Acessibilidade](https://developer.android.com/guide/topics/ui/accessibility/service)
- [Documentação da WebView](https://developer.android.com/reference/android/webkit/WebView)
- [Interação entre JavaScript e Código Nativo](https://developer.android.com/guide/webapps/webview#UsingJavaScript)

## Observações Importantes

- O aplicativo requer permissões de acessibilidade, que são sensíveis e devem ser usadas com responsabilidade
- O WhatsApp Business pode mudar sua interface, o que pode afetar o funcionamento do serviço
- Este aplicativo é apenas para fins educacionais e deve ser usado de acordo com os termos de serviço do WhatsApp Business
