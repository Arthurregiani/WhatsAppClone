# Serviço Cloudflare

O aplicativo 2.0 usa `https://whatsapp-clone-api.arthur-j-regiani.workers.dev`.
`GET /health` deve retornar `{"status":"ok","version":"2.0.0"}`.

## Arquitetura e privacidade

O Worker encaminha `/v1/*` para um Durable Object `ChatService`, nomeado
`whatsapp-clone-v2`, com SQLite persistente. O objeto centraliza usuários, contatos,
conversas, mensagens, sessões e limites por IP/usuário. É uma arquitetura para um
aplicativo pequeno; um objeto único não é uma solução de escala ilimitada.

- Não usa Firebase, D1, R2, SMS, e-mail transacional ou serviços pagos adicionais.
- HTTPS/WSS; mensagens armazenadas no servidor, **sem criptografia ponta a ponta**.
- Senhas com PBKDF2-SHA256, salt aleatório e 100.000 iterações (limite por chamada
  do Web Crypto no Workers). Senhas nunca são retornadas ou registradas em logs.
- Tokens aleatórios de 256 bits: somente SHA-256 no servidor; expiração em 30 dias.
  No Android, a sessão é cifrada com AES-GCM e chave do Android Keystore, excluída
  de backup e transferência entre dispositivos.
- Cada operação exige sessão válida e participação na conversa. Remetente e
  horário vêm do servidor. SQL parametrizado; criação de conversa e envio
  transacionais. `clientId` garante idempotência do reenvio.
- Canal WebSocket envia apenas sinais de atualização para sessões do usuário;
  dados são buscados novamente pela API autenticada. Reconexão busca mensagens
  posteriores ao último ID; histórico anterior usa paginação de 50 mensagens.
- Os sockets usam a API de hibernação e fecham quando a tela sai de primeiro plano.
  Não há push com o aplicativo fechado, chamadas, grupos ou envio de anexos.
- Fotos são JPEG de até 256 px, comprimidas no celular, armazenadas no perfil
  como data URI (máximo de 96.000 caracteres). Acesso somente autenticado.
- Não há validação de propriedade do e-mail nem recuperação de senha. O cadastro
  por e-mail é identificador de acesso; não é uma identidade verificada.
- `DELETE /v1/me` exige a senha e remove conta, sessões, contatos e conversas
  associadas, inclusive o histórico compartilhado. Limpeza diária remove sessões
  expiradas e contadores antigos; não apaga mensagens de contas ativas.

## Franquia e verificação em 20/10/2026

O painel da conta confirmou Workers **Free**, 2.028/100.000 requisições diárias
utilizadas antes desta publicação, US$ 0 de consumo faturável e três Workers
existentes. A publicação adiciona `whatsapp-clone-api`, preservando os anteriores.
O consumo aumenta durante uso e testes e é compartilhado com os outros projetos.

Limites documentados do Durable Objects Free: 100.000 requisições/dia,
13.000 GB-s/dia, 5 milhões de linhas lidas/dia, 100.000 linhas escritas/dia,
5 GB de armazenamento na conta. Índices também consomem escritas. Ao atingir
uma franquia, o serviço gratuito pode falhar até a renovação; não foi solicitado
upgrade. Verifique também armazenamento por objeto e métricas no painel.

Fontes oficiais:
- https://developers.cloudflare.com/workers/platform/limits/
- https://developers.cloudflare.com/durable-objects/platform/pricing/
- https://developers.cloudflare.com/durable-objects/best-practices/websockets/
- https://developers.cloudflare.com/workers/runtime-apis/web-crypto/

## Desenvolvimento e publicação

Requer Node.js 22 ou superior e uma conta Cloudflare autorizada para Workers.

```bash
cd backend
npm ci
npm test
npm run check
npm run dev
# Em outro terminal, verifique http://127.0.0.1:8787/health.
npx wrangler whoami
npm run deploy
```

Wrangler autentica por OAuth. Não inclua tokens no repositório ou em argumentos
de comandos. O binding e a migração SQLite estão em `backend/wrangler.jsonc`.
Não mude o nome do objeto, do Worker, da classe ou remova a migração em produção
sem planejar a migração dos dados. Novas versões do código preservam o SQLite.

Testes locais usam armazenamento temporário isolado e apagam suas contas.
Para validar a instalação publicada, criando apenas contas QA descartáveis:

```bash
API_TEST_URL=https://whatsapp-clone-api.arthur-j-regiani.workers.dev npm test
```

O teste cria contas `qa-...@example.test`, verifica isolamento, mensagens,
WebSocket, paginação, perfil e logout, e remove suas contas ao terminar.
Se um teste remoto for interrompido, trate a limpeza como pendente.

## Operação, rollback e backup

Inspecione Workers & Pages → `whatsapp-clone-api` e Durable Objects no painel.
O logging persistente está desligado para reduzir a coleta de metadados.
Use a tela de Metrics para requisições, duração e uso de SQLite.

`npx wrangler deployments list` mostra versões. `npx wrangler rollback <version>`
reverte código, **não** o banco de dados. Não delete o namespace para reverter uma
publicação. O SQLite tem Time Travel conforme os limites do plano; confirme a
janela vigente antes de qualquer migração. Não há rotina de backup externo
automatizada nesta versão. Exportações de dados exigem destino privado e controle
de acesso, pois mensagens não são cifradas ponta a ponta.

## Contrato HTTP

Todos os corpos são JSON. Rotas privadas exigem `Authorization: Bearer <token>`.
Erros retornam `{"error":"mensagem em português"}` com status apropriado.

| Método | Rota | Dados |
|---|---|---|
| POST | `/v1/auth/register` | `nome`, `email`, `password` |
| POST | `/v1/auth/login` | `email`, `password` |
| POST | `/v1/auth/logout` | sessão atual |
| GET/PATCH | `/v1/me` | leitura / atualização de `nome` |
| PUT | `/v1/me/avatar` | `image` como data URI JPEG |
| DELETE | `/v1/me` | `password` |
| GET/POST | `/v1/contacts` | lista / `email` exato |
| GET/POST | `/v1/chats` | lista / `email` do outro participante |
| GET | `/v1/chats/:id/messages` | `before` ou `after`, opcional |
| POST | `/v1/chats/:id/messages` | `messageText`, `clientId` UUID |
| GET | `/v1/events` | upgrade WebSocket com Authorization |
