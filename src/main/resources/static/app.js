/* ── API Base URL — empty = same origin (served by Spring Boot) ── */
const API = '';

/* ── Token helpers ────────────────────────────────────────────── */
const Auth = {
  save(token)  { localStorage.setItem('jwt', token); },
  get()        { return localStorage.getItem('jwt'); },
  clear()      { localStorage.removeItem('jwt'); localStorage.removeItem('user'); },
  isLoggedIn() { return !!this.get(); },
  saveUser(u)  { localStorage.setItem('user', JSON.stringify(u)); },
  getUser()    {
    try { return JSON.parse(localStorage.getItem('user')); }
    catch { return null; }
  }
};

/* ── HTTP helpers ─────────────────────────────────────────────── */
async function apiPost(path, body) {
  const headers = { 'Content-Type': 'application/json' };
  const token = Auth.get();
  if (token) headers['Authorization'] = 'Bearer ' + token;
  const res = await fetch(API + path, { method: 'POST', headers, body: JSON.stringify(body) });
  return { status: res.status, data: await res.json() };
}

async function apiPut(path, body) {
  const res = await fetch(API + path, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + Auth.get() },
    body: JSON.stringify(body)
  });
  return { status: res.status, data: await res.json() };
}

async function apiGet(path) {
  const res = await fetch(API + path, {
    headers: { 'Authorization': 'Bearer ' + Auth.get() }
  });
  return { status: res.status, data: await res.json() };
}

async function apiDelete(path) {
  const res = await fetch(API + path, {
    method: 'DELETE',
    headers: { 'Authorization': 'Bearer ' + Auth.get() }
  });
  return { status: res.status, data: await res.json() };
}

/* ── UI helpers ───────────────────────────────────────────────── */
function showAlert(id, message, type = 'error') {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = message;
  el.className = `alert alert-${type} show`;
}

function hideAlert(id) {
  const el = document.getElementById(id);
  if (el) el.className = 'alert';
}

function setLoading(btnId, loading, label) {
  const btn = document.getElementById(btnId);
  if (!btn) return;
  btn.disabled = loading;
  btn.innerHTML = loading
    ? `<span class="spinner"></span> ${label}…`
    : label;
}

/* ── Guard helpers ────────────────────────────────────────────── */
function requireAuth() {
  if (!Auth.isLoggedIn()) { window.location.href = '/index.html'; }
}

function requireAdmin() {
  requireAuth();
  const user = Auth.getUser();
  if (!user || user.role !== 'ADMIN') { window.location.href = '/dashboard.html'; }
}

function redirectIfLoggedIn() {
  if (Auth.isLoggedIn()) {
    const user = Auth.getUser();
    window.location.href = user && user.role === 'ADMIN' ? '/admin.html' : '/dashboard.html';
  }
}
