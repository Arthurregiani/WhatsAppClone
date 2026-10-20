# Avaliação de iOS

O repositório original é Kotlin/JVM com Android Activities, layouts XML,
ViewBinding, Android Keystore, Glide, RecyclerView e dependências AndroidX.
Não contém Flutter, React Native, Compose Multiplatform, Swift ou projeto Xcode.

Portar para iOS exige trabalho significativo: toda a interface e a integração com
o ciclo de vida Android precisam de equivalentes. A API HTTPS/WebSocket do serviço
2.0 pode ser reutilizada integralmente por um cliente iOS.

## Caminho recomendado se iOS virar prioridade

Criar cliente SwiftUI com URLSession para HTTP/WebSocket e Keychain para sessões.
Reutilizar o contrato de `docs/CLOUDFLARE.md`, incluindo paginação por ID,
idempotência de envio e validação de participação feita pelo servidor. Implementar
cadastro/login, lista/pesquisa de contatos, conversas, chat, perfil/fotos,
reconexão, mensagens de erro e tratamento de sessão expirada. Adicionar seleção
de fotos/câmera, acessibilidade e testes em diferentes tamanhos de tela.

Ordem de grandeza, não orçamento: aproximadamente 2–4 semanas de trabalho de uma
pessoa para um cliente com os fluxos desta versão e testes em iPhone, sem incluir
push, aprovação na App Store ou mudanças de requisitos. A estimativa depende da
qualidade de acabamento e dos dispositivos disponíveis.

Migrar para um framework compartilhado também exigiria reescrever o cliente
Android, contrariando a prioridade de recuperar e publicar rapidamente o projeto
existente. Uma PWA ou um WebView não equivalem a uma versão nativa para iOS.

Conforme a condição do pedido (“se não der muito trabalho”), não foi criada uma
versão iOS nesta entrega. Compilar/assinar e testar um IPA requerem Xcode em macOS
e a forma de distribuição escolhida pela conta Apple. Este ambiente é Linux e não
dispõe de Xcode nem de credenciais de distribuição Apple.

Referências oficiais: [SwiftUI](https://developer.apple.com/swiftui/),
[URLSessionWebSocketTask](https://developer.apple.com/documentation/foundation/urlsessionwebsockettask),
[distribuição](https://developer.apple.com/documentation/xcode/distributing-your-app-for-beta-testing-and-releases).
