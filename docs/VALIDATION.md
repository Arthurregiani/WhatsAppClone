# Validação — versão 2.0.0

Evidências obtidas em 20/10/2026. Esta versão recupera os fluxos do Android
original e substitui o Firebase. Não equivale a uma auditoria completa de segurança
nem a validação em todos os modelos de telefone.

| Verificação | Resultado |
|---|---|
| Backend: TypeScript e `wrangler deploy --dry-run` | PASS |
| Integração local no runtime Workers/SQLite | PASS: 2 testes com múltiplos cenários |
| Auditoria npm | PASS: 0 vulnerabilidades |
| Publicação Cloudflare e `/health` HTTPS | PASS |
| Integração HTTPS inicial com três contas | PASS: cadastro, isolamento, WebSocket, mensagens, paginação, perfil, logout e limpeza |
| Compilação Android debug e release assinada | PASS |
| Lint Android | PASS: 0 erros; avisos de dependências e layouts legados permanecem |
| Emulador Android 15/API 35: fluxo das telas | PASS: 1 teste completo, 0 falhas |
| Revalidação remota do código final | Em execução pelo workflow manual `live-validation.yml` |
| Celular físico, câmera física e seletor de galeria do fabricante | NOT TESTED |
| Versão iOS/IPA | Não implementada: esforço significativo; avaliação em `IOS.md` |

## Cenários de integração do serviço

Cadastro normal e duplicado; normalização de e-mail; senha incorreta; sessão
ausente; contatos isolados por usuário e sem duplicatas; criação simultânea da
mesma conversa pelos dois participantes; rejeição de leitura/escrita por terceiro;
mensagem recebida por WebSocket; reenvio idempotente; recusa de conteúdo vazio,
excessivo ou JSON inválido; histórico com mais de 50 mensagens; cursores `before`
e `after`; última mensagem na lista de conversas; nome/foto do contato atualizados;
logout sem invalidar outra sessão; encerramento de socket revogado; exclusão com
senha e remoção das conversas associadas.

Os testes locais usam SQLite temporário isolado. Os testes online usam apenas
contas `qa-...@example.test`, apagadas no encerramento. O limite por IP bloqueou
uma repetição local contra o serviço publicado após a rodada inicial; a validação
final usa um executor GitHub Actions, preservando os mesmos limites em produção.

## Cenários de interface Android

Cadastro pela tela, adicionar contato pelo diálogo, iniciar conversa, enviar texto,
confirmar no servidor do destinatário, receber resposta externa em tempo real,
mostrar conversa no histórico, pesquisar sem resultados, renomear perfil, sair e
entrar novamente. A sessão gravada foi conferida como cifrada (não contém token
nem e-mail em texto). O teste final também valida o processamento/upload de uma
foto local pelo pipeline Android; isto não comprova captura por câmera física.

O falso positivo de lint `NotificationPermission` vem de `NotificationTarget` do
Glide 4.x, que o aplicativo não usa. A supressão está restrita e explicada no
manifesto. Não foram adicionadas permissões de câmera, armazenamento ou
notificação desnecessárias.

## Limitações conhecidas

Sem push com o app fechado, chamadas, anexos, grupos, recuperação de senha,
verificação de e-mail ou criptografia ponta a ponta. Sem migração do Firebase
antigo. Compatibilidade declarada desde Android 8/API 26; teste de execução
realizado em API 35. O serviço gratuito compartilha franquia com outros projetos;
um Durable Object único é adequado a pequeno uso, com limite de escala.
