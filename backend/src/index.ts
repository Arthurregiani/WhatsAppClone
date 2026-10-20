import { DurableObject } from 'cloudflare:workers';

interface Env { CHAT: DurableObjectNamespace<ChatService> }
type Row = Record<string, SqlStorageValue>;
type Account = { id: string; nome: string; email: string; avatar: string | null };
const SESSION_MS = 30 * 24 * 60 * 60 * 1000;
const encoder = new TextEncoder();

class HttpError extends Error {
  constructor(public status: number, message: string) { super(message); }
}
function json(data: unknown, status = 200) {
  return Response.json(data, { status, headers: { 'Cache-Control': 'no-store', 'X-Content-Type-Options': 'nosniff' } });
}
function requireValue(condition: unknown, status: number, message: string): asserts condition {
  if (!condition) throw new HttpError(status, message);
}
function text(value: unknown, min: number, max: number, label: string): string {
  requireValue(typeof value === 'string', 400, `${label} inválido.`);
  const result = value.trim();
  requireValue(result.length >= min && result.length <= max, 400, `${label}: use entre ${min} e ${max} caracteres.`);
  return result;
}
function email(value: unknown) {
  const result = text(value, 3, 254, 'E-mail').toLowerCase();
  requireValue(/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(result), 400, 'E-mail inválido.');
  return result;
}
function password(value: unknown) {
  requireValue(typeof value === 'string' && value.length >= 8 && value.length <= 128, 400, 'A senha deve ter entre 8 e 128 caracteres.');
  return value;
}
function hex(buffer: ArrayBuffer) {
  return [...new Uint8Array(buffer)].map(x => x.toString(16).padStart(2, '0')).join('');
}
async function digest(value: string) { return hex(await crypto.subtle.digest('SHA-256', encoder.encode(value))); }
async function hashPassword(value: string, salt: string) {
  const key = await crypto.subtle.importKey('raw', encoder.encode(value), 'PBKDF2', false, ['deriveBits']);
  return hex(await crypto.subtle.deriveBits({ name: 'PBKDF2', hash: 'SHA-256', salt: encoder.encode(salt), iterations: 100_000 }, key, 256));
}
function equal(a: string, b: string) {
  let difference = a.length ^ b.length;
  for (let i = 0; i < a.length; i++) difference |= a.charCodeAt(i) ^ (b.charCodeAt(i) || 0);
  return difference === 0;
}
function publicUser(user: Account) {
  return { id: user.id, nome: user.nome, email: user.email, profileImageUrl: user.avatar };
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);
    if (url.pathname === '/health' && request.method === 'GET') return json({ status: 'ok', version: '2.0.0' });
    if (!url.pathname.startsWith('/v1/')) return json({ error: 'Rota não encontrada.' }, 404);
    // The object name is fixed, so every device sees the same persistent database.
    return env.CHAT.getByName('whatsapp-clone-v2').fetch(request);
  }
} satisfies ExportedHandler<Env>;

export class ChatService extends DurableObject<Env> {
  private sql: SqlStorage;
  constructor(ctx: DurableObjectState, env: Env) {
    super(ctx, env);
    this.sql = ctx.storage.sql;
    this.sql.exec(`
      CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY, nome TEXT NOT NULL, email TEXT NOT NULL UNIQUE,
        password_hash TEXT NOT NULL, salt TEXT NOT NULL, avatar TEXT, created_at INTEGER NOT NULL
      );
      CREATE TABLE IF NOT EXISTS sessions (
        token_hash TEXT PRIMARY KEY, user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE, expires_at INTEGER NOT NULL
      );
      CREATE INDEX IF NOT EXISTS sessions_user ON sessions(user_id);
      CREATE INDEX IF NOT EXISTS sessions_expiry ON sessions(expires_at);
      CREATE TABLE IF NOT EXISTS contacts (
        owner_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        contact_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        PRIMARY KEY(owner_id, contact_id)
      );
      CREATE TABLE IF NOT EXISTS chats (
        id TEXT PRIMARY KEY, user_a TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        user_b TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        last_message TEXT NOT NULL DEFAULT '', updated_at INTEGER NOT NULL, UNIQUE(user_a,user_b)
      );
      CREATE INDEX IF NOT EXISTS contacts_reverse ON contacts(contact_id);
      CREATE INDEX IF NOT EXISTS chats_a ON chats(user_a,updated_at DESC);
      CREATE INDEX IF NOT EXISTS chats_b ON chats(user_b,updated_at DESC);
      CREATE TABLE IF NOT EXISTS messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT, chat_id TEXT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
        sender_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        client_id TEXT NOT NULL, message_text TEXT NOT NULL, timestamp INTEGER NOT NULL,
        UNIQUE(sender_id,client_id)
      );
      CREATE INDEX IF NOT EXISTS messages_chat ON messages(chat_id,id DESC);
      CREATE TABLE IF NOT EXISTS limits (key TEXT PRIMARY KEY, attempts INTEGER NOT NULL, expires_at INTEGER NOT NULL);
      CREATE INDEX IF NOT EXISTS limits_expiry ON limits(expires_at);
    `);
  }

  private one<T = Row>(query: string, ...args: SqlStorageValue[]): T | undefined {
    return this.sql.exec(query, ...args).toArray()[0] as T | undefined;
  }
  private limit(key: string, max: number, duration: number) {
    const now = Date.now();
    const row = this.one<{ attempts: number }>(`INSERT INTO limits(key,attempts,expires_at) VALUES(?,1,?)
      ON CONFLICT(key) DO UPDATE SET attempts=CASE WHEN expires_at<=? THEN 1 ELSE attempts+1 END,
      expires_at=CASE WHEN expires_at<=? THEN excluded.expires_at ELSE expires_at END RETURNING attempts`, key, now + duration, now, now)!;
    requireValue(row.attempts <= max, 429, 'Muitas tentativas. Aguarde e tente novamente.');
  }
  private async body(request: Request): Promise<Record<string, unknown>> {
    requireValue(request.headers.get('Content-Type')?.startsWith('application/json'), 415, 'Envie application/json.');
    requireValue(Number(request.headers.get('Content-Length') || 0) <= 140_000, 413, 'Conteúdo muito grande.');
    // Stream with a hard bound, including clients that omit Content-Length.
    const reader = request.body?.getReader();
    requireValue(reader, 400, 'Corpo JSON obrigatório.');
    let size = 0;
    const chunks: Uint8Array[] = [];
    while (true) {
      const part = await reader.read();
      if (part.done) break;
      size += part.value.byteLength;
      if (size > 140_000) { await reader.cancel(); throw new HttpError(413, 'Conteúdo muito grande.'); }
      chunks.push(part.value);
    }
    const bytes = new Uint8Array(size);
    let offset = 0;
    for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.byteLength; }
    let data: unknown;
    try { data = JSON.parse(new TextDecoder().decode(bytes)); } catch { throw new HttpError(400, 'JSON inválido.'); }
    requireValue(data && typeof data === 'object' && !Array.isArray(data), 400, 'JSON inválido.');
    return data as Record<string, unknown>;
  }
  private async session(request: Request) {
    const token = request.headers.get('Authorization')?.match(/^Bearer ([a-f0-9]{64})$/)?.[1];
    requireValue(token, 401, 'Entre novamente na sua conta.');
    const hash = await digest(token);
    const session = this.one<Account & { expires_at: number }>(`SELECT u.*,s.expires_at FROM sessions s
      JOIN users u ON u.id=s.user_id WHERE s.token_hash=? AND s.expires_at>?`, hash, Date.now());
    requireValue(session, 401, 'Sessão expirada. Entre novamente.');
    return { user: session, hash, expiresAt: session.expires_at };
  }
  private member(chatId: string, userId: string) {
    const chat = this.one<{ id: string; user_a: string; user_b: string }>(
      'SELECT id,user_a,user_b FROM chats WHERE id=? AND (user_a=? OR user_b=?)', chatId, userId, userId);
    requireValue(chat, 404, 'Conversa não encontrada.');
    return chat;
  }
  private notify(userIds: string[], chatId?: string) {
    for (const id of userIds) {
      for (const socket of this.ctx.getWebSockets(id)) {
        const attachment = socket.deserializeAttachment() as { hash: string; expiresAt: number };
        const valid = attachment.expiresAt > Date.now() && this.one('SELECT 1 FROM sessions WHERE token_hash=?', attachment.hash);
        if (!valid) { socket.close(1008, 'Sessão encerrada'); continue; }
        try { socket.send(JSON.stringify({ type: 'refresh', chatId })); } catch { socket.close(1011, 'Reconecte'); }
      }
    }
  }
  private profileAudience(userId: string): string[] {
    const rows = this.sql.exec(`SELECT owner_id AS id FROM contacts WHERE contact_id=?
      UNION SELECT CASE WHEN user_a=? THEN user_b ELSE user_a END AS id FROM chats WHERE user_a=? OR user_b=?`, userId, userId, userId, userId).toArray();
    return [userId, ...rows.map(row => String(row.id))];
  }
  private message(row: Row) {
    return { id: String(row.id), senderId: row.sender_id, clientId: row.client_id, messageText: row.message_text, timestamp: row.timestamp };
  }
  private async issueSession(user: Account) {
    const token = hex(crypto.getRandomValues(new Uint8Array(32)).buffer);
    const hash = await digest(token);
    const expiresAt = Date.now() + SESSION_MS;
    return { token, hash, expiresAt, user: publicUser(user) };
  }
  async fetch(request: Request): Promise<Response> {
    try { return await this.route(request); }
    catch (error) {
      if (error instanceof HttpError) return json({ error: error.message }, error.status);
      // Never log bodies, passwords, emails or session tokens.
      console.error('Request failed', error instanceof Error ? error.name : 'UnknownError');
      return json({ error: 'Não foi possível concluir a operação. Tente novamente.' }, 500);
    }
  }
  private async route(request: Request): Promise<Response> {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;
    if (method === 'POST' && (path === '/v1/auth/register' || path === '/v1/auth/login')) {
      const ip = request.headers.get('CF-Connecting-IP') || 'local';
      this.limit(`auth:${ip}`, 25, 15 * 60_000);
      if (path.endsWith('register')) this.limit(`signup:${ip}`, 8, 60 * 60_000);
      const data = await this.body(request);
      const address = email(data.email);
      const secret = password(data.password);
      let user: Account;
      if (path.endsWith('register')) {
        const nome = text(data.nome, 1, 80, 'Nome');
        requireValue(!this.one('SELECT id FROM users WHERE email=?', address), 409, 'Não foi possível cadastrar este e-mail. Tente entrar.');
        const salt = hex(crypto.getRandomValues(new Uint8Array(16)).buffer);
        const hash = await hashPassword(secret, salt);
        user = { id: crypto.randomUUID(), nome, email: address, avatar: null };
        const issued = await this.issueSession(user);
        this.ctx.storage.transactionSync(() => {
          // Recheck after await to prevent concurrent registrations with the same email.
          requireValue(!this.one('SELECT id FROM users WHERE email=?', address), 409, 'Não foi possível cadastrar este e-mail. Tente entrar.');
          this.sql.exec('INSERT INTO users(id,nome,email,password_hash,salt,created_at) VALUES(?,?,?,?,?,?)', user.id, nome, address, hash, salt, Date.now());
          this.sql.exec('INSERT INTO sessions VALUES(?,?,?)', issued.hash, user.id, issued.expiresAt);
        });
        await this.ensureCleanup();
        return json({ token: issued.token, expiresAt: issued.expiresAt, user: issued.user }, 201);
      }
      const stored = this.one<Account & { password_hash: string; salt: string }>('SELECT * FROM users WHERE email=?', address);
      // Run the KDF even for nonexistent users to reduce account enumeration via timing.
      const computed = await hashPassword(secret, stored?.salt || '00000000000000000000000000000000');
      requireValue(stored && equal(computed, stored.password_hash), 401, 'E-mail ou senha incorretos.');
      user = stored;
      const issued = await this.issueSession(user);
      this.sql.exec('INSERT INTO sessions VALUES(?,?,?)', issued.hash, user.id, issued.expiresAt);
      await this.ensureCleanup();
      return json({ token: issued.token, expiresAt: issued.expiresAt, user: issued.user });
    }
    const session = await this.session(request);
    const user = session.user;
    if (method !== 'GET') this.limit(`write:${user.id}`, 90, 60_000);

    if (path === '/v1/events' && method === 'GET') {
      requireValue(request.headers.get('Upgrade')?.toLowerCase() === 'websocket', 426, 'WebSocket obrigatório.');
      requireValue(this.ctx.getWebSockets(user.id).length < 6, 429, 'Muitas conexões abertas.');
      const pair = new WebSocketPair();
      this.ctx.acceptWebSocket(pair[1], [user.id]);
      pair[1].serializeAttachment({ hash: session.hash, expiresAt: session.expiresAt });
      pair[1].send(JSON.stringify({ type: 'ready' }));
      return new Response(null, { status: 101, webSocket: pair[0] });
    }
    if (path === '/v1/auth/logout' && method === 'POST') {
      if (request.body) await this.body(request);
      this.sql.exec('DELETE FROM sessions WHERE token_hash=?', session.hash);
      this.notify([user.id]);
      return json({ ok: true });
    }
    if (path === '/v1/me' && method === 'GET') return json({ user: publicUser(user) });
    if (path === '/v1/me' && method === 'PATCH') {
      const data = await this.body(request);
      const nome = text(data.nome, 1, 80, 'Nome');
      this.sql.exec('UPDATE users SET nome=? WHERE id=?', nome, user.id);
      this.notify(this.profileAudience(user.id));
      return json({ user: publicUser({ ...user, nome }) });
    }
    if (path === '/v1/me/avatar' && method === 'PUT') {
      const data = await this.body(request);
      const image = text(data.image, 30, 96_000, 'Foto');
      requireValue(/^data:image\/jpeg;base64,\/[0-9a-zA-Z+/]+=*$/.test(image), 400, 'Envie uma imagem JPEG.');
      let bytes: string;
      try { bytes = atob(image.split(',')[1]); } catch { throw new HttpError(400, 'Imagem inválida.'); }
      requireValue(bytes.charCodeAt(0) === 255 && bytes.charCodeAt(1) === 216 && bytes.charCodeAt(2) === 255, 400, 'Imagem inválida.');
      this.sql.exec('UPDATE users SET avatar=? WHERE id=?', image, user.id);
      this.notify(this.profileAudience(user.id));
      return json({ profileImageUrl: image });
    }
    if (path === '/v1/contacts' && method === 'GET') {
      const contacts = this.sql.exec(`SELECT u.id,u.nome,u.email,u.avatar FROM contacts c JOIN users u ON u.id=c.contact_id
        WHERE c.owner_id=? ORDER BY u.nome COLLATE NOCASE`, user.id).toArray() as unknown as Account[];
      return json({ contacts: contacts.map(publicUser) });
    }
    if (path === '/v1/contacts' && method === 'POST') {
      const data = await this.body(request);
      const contact = this.one<Account>('SELECT id,nome,email,avatar FROM users WHERE email=?', email(data.email));
      requireValue(contact && contact.id !== user.id, 404, 'Contato não encontrado. Informe o e-mail de outra pessoa cadastrada.');
      const count = this.one<{ count: number }>('SELECT COUNT(*) AS count FROM contacts WHERE owner_id=?', user.id)!.count;
      requireValue(count < 200 || this.one('SELECT 1 FROM contacts WHERE owner_id=? AND contact_id=?', user.id, contact.id), 409, 'Limite de 200 contatos atingido.');
      this.sql.exec('INSERT OR IGNORE INTO contacts VALUES(?,?)', user.id, contact.id);
      this.notify([user.id]);
      return json({ contact: publicUser(contact) }, 201);
    }
    if (path === '/v1/chats' && method === 'POST') {
      const data = await this.body(request);
      const contact = this.one<Account>('SELECT id,nome,email,avatar FROM users WHERE email=?', email(data.email));
      requireValue(contact && contact.id !== user.id, 404, 'Contato não encontrado.');
      const [a, b] = [user.id, contact.id].sort();
      this.sql.exec('INSERT OR IGNORE INTO chats(id,user_a,user_b,updated_at) VALUES(?,?,?,?)', crypto.randomUUID(), a, b, Date.now());
      const chat = this.one('SELECT id FROM chats WHERE user_a=? AND user_b=?', a, b)!;
      return json({ id: chat.id });
    }
    if (path === '/v1/chats' && method === 'GET') {
      const rows = this.sql.exec(`SELECT c.*,u.id AS contact_id,u.nome,u.email,u.avatar FROM chats c
        JOIN users u ON u.id=CASE WHEN c.user_a=? THEN c.user_b ELSE c.user_a END
        WHERE c.user_a=? OR c.user_b=? ORDER BY c.updated_at DESC,c.id LIMIT 200`, user.id, user.id, user.id).toArray();
      return json({ chats: rows.map(row => ({ id: row.id, lastMessage: row.last_message, lastMessageTimestamp: row.updated_at,
        contact: publicUser({ id: row.contact_id as string, nome: row.nome as string, email: row.email as string, avatar: row.avatar as string | null }) })) });
    }
    const match = path.match(/^\/v1\/chats\/([a-f0-9-]{36})\/messages$/);
    if (match) {
      const chat = this.member(match[1], user.id);
      if (method === 'GET') {
        const beforeText = url.searchParams.get('before');
        const afterText = url.searchParams.get('after');
        requireValue(beforeText === null || /^[1-9][0-9]{0,14}$/.test(beforeText), 400, 'Cursor inválido.');
        requireValue(afterText === null || /^[1-9][0-9]{0,14}$/.test(afterText), 400, 'Cursor inválido.');
        requireValue(beforeText === null || afterText === null, 400, 'Use somente um cursor.');
        const before = beforeText ? Number(beforeText) : Number.MAX_SAFE_INTEGER;
        const rows = afterText
          ? this.sql.exec('SELECT * FROM messages WHERE chat_id=? AND id>? ORDER BY id ASC LIMIT 51', chat.id, Number(afterText)).toArray()
          : this.sql.exec('SELECT * FROM messages WHERE chat_id=? AND id<? ORDER BY id DESC LIMIT 51', chat.id, before).toArray();
        const hasMore = rows.length > 50;
        const page = afterText ? rows.slice(0, 50) : rows.slice(0, 50).reverse();
        return json({ messages: page.map(row => this.message(row)), hasMore });
      }
      if (method === 'POST') {
        const data = await this.body(request);
        const content = text(data.messageText, 1, 4000, 'Mensagem');
        const clientId = text(data.clientId, 36, 36, 'Identificador');
        requireValue(/^[a-f0-9-]{36}$/.test(clientId), 400, 'Identificador inválido.');
        const previous = this.one('SELECT * FROM messages WHERE sender_id=? AND client_id=?', user.id, clientId);
        if (previous) {
          requireValue(previous.chat_id === chat.id && previous.message_text === content, 409, 'Identificador já utilizado.');
          return json({ message: this.message(previous) });
        }
        const now = Date.now();
        let row: Row;
        this.ctx.storage.transactionSync(() => {
          row = this.one('INSERT INTO messages(chat_id,sender_id,client_id,message_text,timestamp) VALUES(?,?,?,?,?) RETURNING *', chat.id, user.id, clientId, content, now)!;
          this.sql.exec('UPDATE chats SET last_message=?,updated_at=? WHERE id=?', content, now, chat.id);
        });
        this.notify([chat.user_a, chat.user_b], chat.id);
        return json({ message: this.message(row!) }, 201);
      }
    }
    // Deleting a test or real account cascades its private data and closes all sessions.
    if (path === '/v1/me' && method === 'DELETE') {
      const data = await this.body(request);
      requireValue(typeof data.password === 'string', 400, 'Confirme sua senha.');
      this.limit(`delete:${user.id}`, 5, 15 * 60_000);
      const stored = this.one<{ salt: string; password_hash: string }>('SELECT salt,password_hash FROM users WHERE id=?', user.id)!;
      requireValue(equal(await hashPassword(data.password, stored.salt), stored.password_hash), 403, 'Senha incorreta.');
      const audience = this.profileAudience(user.id);
      this.sql.exec('DELETE FROM users WHERE id=?', user.id);
      this.notify(audience);
      return json({ ok: true });
    }
    throw new HttpError(404, 'Rota não encontrada.');
  }
  private async ensureCleanup() {
    if (await this.ctx.storage.getAlarm() === null) await this.ctx.storage.setAlarm(Date.now() + 24 * 60 * 60_000);
  }
  async alarm() {
    this.sql.exec('DELETE FROM sessions WHERE expires_at<=?', Date.now());
    this.sql.exec('DELETE FROM limits WHERE expires_at<=?', Date.now());
    await this.ctx.storage.setAlarm(Date.now() + 24 * 60 * 60_000);
  }
  webSocketMessage(socket: WebSocket) { socket.close(1008, 'Canal somente de leitura'); }
  webSocketClose(socket: WebSocket, code: number) { socket.close(code === 1005 ? 1000 : code); }
  webSocketError(socket: WebSocket) { socket.close(1011, 'Reconecte'); }
}
