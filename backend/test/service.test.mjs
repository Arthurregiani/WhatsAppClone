import { test, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { spawn } from 'node:child_process';
import { mkdtemp, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { randomUUID } from 'node:crypto';

const external = process.env.API_TEST_URL;
const base = external || 'http://127.0.0.1:8791';
const accounts = [];
let server, directory;
const password = randomUUID() + '!aA1';
async function request(path, { method = 'GET', token, data, status = 200 } = {}) {
  if (method !== 'GET' && data === undefined) data = {};
  const response = await fetch(base + '/v1' + path, {
    method, headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(data ? { 'Content-Type': 'application/json' } : {}) },
    body: data ? JSON.stringify(data) : undefined,
  });
  const result = await response.json();
  assert.equal(response.status, status, `${method} ${path}: ${JSON.stringify(result)}`);
  return result;
}
async function register(nome) {
  const email = `qa-${randomUUID()}@example.test`;
  const result = await request('/auth/register', { method: 'POST', data: { nome, email, password }, status: 201 });
  accounts.push(result);
  return result;
}
function event(socket, type) {
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => { cleanup(); reject(new Error(`WebSocket ${type} timeout`)); }, 15_000);
    const handler = e => { const data = JSON.parse(e.data); if (data.type === type) { cleanup(); resolve(data); } };
    function cleanup() { clearTimeout(timeout); socket.removeEventListener('message', handler); }
    socket.addEventListener('message', handler);
  });
}
// node's WebSocket cannot attach Authorization; use the same HTTP header handshake as Android.
import WebSocket from 'ws';

before(async () => {
  if (external) return;
  directory = await mkdtemp(join(tmpdir(), 'whatsapp-clone-test-'));
  server = spawn(process.execPath, ['node_modules/wrangler/bin/wrangler.js', 'dev', '--ip', '127.0.0.1', '--port', '8791', '--persist-to', directory], { stdio: ['ignore', 'pipe', 'pipe'] });
  let output = '';
  server.stdout.on('data', d => { output += d; }); server.stderr.on('data', d => { output += d; });
  for (let i = 0; i < 80; i++) {
    try { if ((await fetch(base + '/health')).ok) return; } catch {}
    if (server.exitCode !== null) throw new Error(output);
    await new Promise(resolve => setTimeout(resolve, 250));
  }
  throw new Error('Worker did not start: ' + output);
});
after(async () => {
  for (const account of accounts) {
    if (account.deleted) continue;
    // A test may have revoked the original token; use a fresh login for cleanup.
    try {
      const login = await request('/auth/login', { method: 'POST', data: { email: account.user.email, password } });
      await request('/me', { method: 'DELETE', token: login.token, data: { password } });
    } catch (error) { if (external) throw error; }
  }
  if (server) { server.kill('SIGTERM'); await new Promise(resolve => server.once('exit', resolve)); }
  if (directory) await rm(directory, { recursive: true, force: true });
});

test('registration, isolation, contacts, atomic chat, live messages, history, profile and session revocation', async () => {
  const alice = await register('Alice QA');
  const bob = await register('Bob QA');
  const eve = await register('Eve QA');
  assert.match(alice.token, /^[a-f0-9]{64}$/);
  assert.equal('password_hash' in alice.user, false);
  await request('/me', { status: 401 });
  await request('/auth/login', { method: 'POST', data: { email: alice.user.email, password: 'incorrect-password' }, status: 401 });
  await request('/auth/register', { method: 'POST', data: { nome: 'Duplicate', email: alice.user.email, password }, status: 409 });
  const logged = await request('/auth/login', { method: 'POST', data: { email: alice.user.email.toUpperCase(), password } });
  assert.equal(logged.user.id, alice.user.id);
  await request('/contacts', { method: 'POST', token: alice.token, data: { email: alice.user.email }, status: 404 });
  await request('/contacts', { method: 'POST', token: alice.token, data: { email: bob.user.email }, status: 201 });
  await request('/contacts', { method: 'POST', token: alice.token, data: { email: bob.user.email }, status: 201 });
  assert.equal((await request('/contacts', { token: alice.token })).contacts.length, 1);
  assert.equal((await request('/contacts', { token: eve.token })).contacts.length, 0);
  const pair = await Promise.all([
    request('/chats', { method: 'POST', token: alice.token, data: { email: bob.user.email } }),
    request('/chats', { method: 'POST', token: bob.token, data: { email: alice.user.email } }),
  ]);
  const chat = pair[0].id;
  assert.equal(pair[1].id, chat);
  await request(`/chats/${chat}/messages`, { token: eve.token, status: 404 });
  await request(`/chats/${chat}/messages`, { method: 'POST', token: eve.token, data: { messageText: 'intrusion', clientId: randomUUID() }, status: 404 });
  const socket = new WebSocket(base.replace('http', 'ws') + '/v1/events', { closeTimeout: 1000, headers: { Authorization: `Bearer ${bob.token}` } });
  const ready = event(socket, 'ready');
  await ready;
  try {
    const refresh = event(socket, 'refresh');
    const clientId = randomUUID();
    const sent = await request(`/chats/${chat}/messages`, { method: 'POST', token: alice.token, data: { messageText: 'Olá, Bob 👋', clientId }, status: 201 });
    assert.equal((await refresh).chatId, chat);
    assert.equal(sent.message.senderId, alice.user.id);
    const retry = await request(`/chats/${chat}/messages`, { method: 'POST', token: alice.token, data: { messageText: 'Olá, Bob 👋', clientId } });
    assert.equal(retry.message.id, sent.message.id);
    await request(`/chats/${chat}/messages`, { method: 'POST', token: alice.token, data: { messageText: 'altered', clientId }, status: 409 });
    await request(`/chats/${chat}/messages`, { method: 'POST', token: alice.token, data: { messageText: ' ', clientId: randomUUID() }, status: 400 });
    await request(`/chats/${chat}/messages`, { method: 'POST', token: alice.token, data: { messageText: 'a'.repeat(4001), clientId: randomUUID() }, status: 400 });
    // Exercise pagination beyond the first page with alternating senders.
    for (let i = 0; i < 53; i++) await request(`/chats/${chat}/messages`, {
      method: 'POST', token: i % 2 ? alice.token : bob.token, data: { messageText: `message-${i}`, clientId: randomUUID() }, status: 201,
    });
    const latest = await request(`/chats/${chat}/messages`, { token: bob.token });
    assert.equal(latest.messages.length, 50); assert.equal(latest.hasMore, true);
    const older = await request(`/chats/${chat}/messages?before=${latest.messages[0].id}`, { token: alice.token });
    assert.equal(older.messages.length, 4); assert.equal(older.hasMore, false);
    assert.equal(new Set([...older.messages, ...latest.messages].map(m => m.id)).size, 54);
    const catchup = await request(`/chats/${chat}/messages?after=${older.messages[0].id}`, { token: alice.token });
    assert.equal(catchup.messages.length, 50); assert.equal(catchup.hasMore, true);
    const rest = await request(`/chats/${chat}/messages?after=${catchup.messages.at(-1).id}`, { token: alice.token });
    assert.equal(rest.messages.length, 3); assert.equal(rest.hasMore, false);
    await request(`/chats/${chat}/messages?before=invalid`, { token: alice.token, status: 400 });
    const conversations = await request('/chats', { token: bob.token });
    assert.equal(conversations.chats[0].contact.id, alice.user.id);
    assert.equal(conversations.chats[0].lastMessage, 'message-52');
    await request('/me', { method: 'PATCH', token: bob.token, data: { nome: 'Bob atualizado' } });
    assert.equal((await request('/contacts', { token: alice.token })).contacts[0].nome, 'Bob atualizado');
    await request('/me/avatar', { method: 'PUT', token: bob.token, data: { image: 'data:image/jpeg;base64,' + Buffer.from('not a photo').toString('base64') }, status: 400 });
    const image = 'data:image/jpeg;base64,' + Buffer.from([255, 216, 255, 224, ...new Array(30).fill(0), 255, 217]).toString('base64');
    await request('/me/avatar', { method: 'PUT', token: bob.token, data: { image } });
    assert.equal((await request('/contacts', { token: alice.token })).contacts[0].profileImageUrl, image);
    await request('/auth/logout', { method: 'POST', token: logged.token });
    await request('/me', { token: logged.token, status: 401 });
    assert.equal((await request('/me', { token: alice.token })).user.id, alice.user.id);
    const closed = new Promise((resolve, reject) => {
      const timeout = setTimeout(() => reject(new Error('Revoked socket stayed open')), 10_000);
      socket.once('close', code => { clearTimeout(timeout); resolve(code); });
    });
    await request('/auth/logout', { method: 'POST', token: bob.token });
    assert.equal(await closed, 1008);
    await request('/me', { method: 'DELETE', token: alice.token, data: { password: 'wrong-password' }, status: 403 });
    await request('/me', { method: 'DELETE', token: alice.token, data: { password } });
    alice.deleted = true;
    await request('/me', { token: alice.token, status: 401 });
    const bobLogin = await request('/auth/login', { method: 'POST', data: { email: bob.user.email, password } });
    assert.equal((await request('/chats', { token: bobLogin.token })).chats.length, 0);
  } finally { socket.close(); }
});

test('body validation, request bounds and password validation', async () => {
  await request('/auth/register', { method: 'POST', data: { nome: 'QA', email: 'invalid', password }, status: 400 });
  await request('/auth/register', { method: 'POST', data: { nome: 'QA', email: 'qa@example.test', password: 'short' }, status: 400 });
  await request('/auth/login', { method: 'POST', data: { email: 'a'.repeat(141_000), password }, status: 413 });
  const malformed = await fetch(base + '/v1/auth/login', { method: 'POST', body: '{bad', headers: { 'Content-Type': 'application/json' } });
  assert.equal(malformed.status, 400);
  const incorrect = await fetch(base + '/v1/auth/login', { method: 'POST', body: '{}' });
  assert.equal(incorrect.status, 415);
});
