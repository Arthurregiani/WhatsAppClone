# WhatsApp 2 — Android + Cloudflare

Continuação do projeto acadêmico WhatsAppClone, preservando a interface clássica
em Kotlin/XML. A versão 2.0 substitui o Firebase por um serviço próprio em
Cloudflare Workers com SQLite persistente e WebSocket.

**[Baixar APK de instalação](https://github.com/Arthurregiani/WhatsAppClone/releases/latest)** ·
[API online](https://whatsapp-clone-api.arthur-j-regiani.workers.dev/health)

## Instalar e conversar

1. Baixe `WhatsApp-2-2.0.0.apk` na release e permita a instalação pelo navegador ou
   gerenciador de arquivos usado. Requer Android 8.0 ou superior (API 26).
2. Crie uma conta com nome, e-mail e senha de 8 a 128 caracteres. A outra pessoa
   também precisa se cadastrar neste aplicativo.
3. Em **Contatos → +**, adicione o e-mail exato da outra pessoa. Toque no contato
   para conversar. As conversas recebidas aparecem em **Conversas**, mesmo quando
   o remetente não estiver na sua lista de contatos.
4. Use a lupa para pesquisar conversas e contatos e o menu para editar perfil ou
   sair. O histórico anterior pode ser carregado dentro da conversa.

Este aplicativo é independente e não acessa a rede oficial do WhatsApp.
As contas, senhas e mensagens do antigo Firebase **não foram migradas**: não há
exportação nem credenciais administrativas daquele serviço nesta entrega.
Se houver uma instalação antiga assinada com outra chave, o Android exigirá sua
remoção antes de instalar a nova versão. A release 1.0.0 não tinha APK publicado.

## Funcionalidades

- Cadastro e login atômicos, sessão persistente cifrada no Android e logout.
- Contatos por e-mail, fotos de perfil, nome atualizado e busca nas duas abas.
- Lista real de conversas, última mensagem e data, incluindo conversas recebidas.
- Mensagens de texto por HTTPS/WSS, com reconexão, histórico paginado e
  identificação única de envio para evitar duplicatas em uma nova tentativa.
- Rascunho preservado quando o envio falha; erros apresentados na tela.
- Seleção de foto pela galeria e captura delegada ao aplicativo de câmera.
- Serviço no Workers Free, sem dependência do período de teste do Firebase.

Limites desta versão: sem notificações push com o app fechado, anexos, chamadas,
grupos, recuperação de senha ou verificação de propriedade do e-mail.
As mensagens **não têm criptografia ponta a ponta** e ficam no servidor.
A franquia gratuita é compartilhada com outros projetos da conta e pode se esgotar.

## Compilar Android

Requer JDK 17, Android SDK 35 e conexão para baixar dependências. O Gradle Wrapper
está incluído. Configure `ANDROID_HOME` ou um `local.properties` privado:

```properties
sdk.dir=/caminho/para/Android/Sdk
```

```bash
./gradlew :app:assembleDebug :app:lintDebug
# O APK fica em app/build/outputs/apk/debug/app-debug.apk.
```

A URL padrão já aponta para o serviço publicado. Para usar outro serviço HTTPS:

```bash
./gradlew :app:assembleDebug -PapiBaseUrl=https://seu-worker.exemplo.workers.dev
```

Para gerar uma release assinada, crie `signing.properties` **fora do controle de
versão**, apontando para sua chave privada:

```properties
storeFile=/caminho/privado/release.jks
storePassword=SUA_SENHA_LOCAL
keyAlias=SEU_ALIAS
keyPassword=SUA_SENHA_LOCAL
```

```bash
./gradlew :app:assembleRelease :app:lintRelease
```

Sem esse arquivo, a release é **não assinada** e não é instalável. A chave usada na
release 2.0.0 fica apenas na máquina de preparação, em
`.tooling/signing/whatsapp-clone-release.jks`; suas senhas ficam em
`signing.properties`, ambos ignorados pelo Git. Preserve um backup privado desses
arquivos para assinar atualizações. A chave não é anexada às releases.

## Backend e testes

```bash
cd backend
npm ci                 # Node.js 22+
npm test               # integração no runtime Workers/SQLite local isolado
npm run check          # TypeScript e simulação de deploy
npm run dev
npm run deploy         # exige autenticação Cloudflare própria
```

Veja [arquitetura, API, limites e operação](docs/CLOUDFLARE.md).
A CI verifica o backend e compila/verifica o Android em cada alteração.
O artefato de CI `android-debug-apk` usa chave de debug; para instalação da versão
publicada, use o APK assinado na release.

O teste Android `MessagingFlowTest` usa um emulador/celular e o serviço HTTPS
publicado. Ele cria duas contas QA descartáveis, percorre cadastro, contato,
envio/recebimento em tempo real, busca, perfil, logout e novo login, e remove as
contas ao terminar:

```bash
./gradlew :app:connectedDebugAndroidTest
```

As capturas ficam em `Android/data/br.edu.ifsp.dmo.whatsapp/files/evidence/` no
dispositivo. Não execute este teste com dados pessoais ou contas reais.
Veja [evidências da entrega](docs/VALIDATION.md).

## iOS

O cliente é inteiramente Android nativo. A avaliação concluiu que iOS exige uma
nova interface e testes de plataforma; não é uma adaptação pequena. Conforme a
condição do pedido, esta entrega prioriza o Android. A API já pode ser reutilizada
por um futuro cliente SwiftUI. [Avaliação detalhada](docs/IOS.md).
