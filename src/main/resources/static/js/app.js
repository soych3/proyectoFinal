// Frontend del e-commerce. Habla con la API en el mismo origen (/api/...).
const API = '/api';
const USUARIO_ID = 1; // usuario demo cargado por data.sql

let productos = [];
let categorias = [];
let carrito = cargarCarrito(); // { [productoId]: cantidad }

// ---------- utilidades ----------
function el(tag, props = {}, ...hijos) {
  const nodo = document.createElement(tag);
  for (const [k, v] of Object.entries(props)) {
    if (k === 'class') nodo.className = v;
    else if (k.startsWith('on')) nodo.addEventListener(k.slice(2), v);
    else nodo[k] = v;
  }
  for (const h of hijos) nodo.append(h);
  return nodo;
}

const dinero = n => Number(n).toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });

function cargarCarrito() {
  try {
    const crudo = JSON.parse(localStorage.getItem('carrito')) || {};
    const limpio = {};
    // Solo se aceptan cantidades enteras >= 1 (el almacenamiento puede haber sido modificado)
    for (const [id, n] of Object.entries(crudo)) {
      if (Number.isInteger(n) && n >= 1) limpio[id] = n;
    }
    return limpio;
  } catch { return {}; }
}
function guardarCarrito() {
  try { localStorage.setItem('carrito', JSON.stringify(carrito)); } catch { /* sin almacenamiento */ }
  const cantidad = Object.values(carrito).reduce((a, b) => a + b, 0);
  document.getElementById('contador').textContent = cantidad;
  document.getElementById('btn-carrito').setAttribute('aria-label', `Carrito, ${cantidad} productos`);
}

let temporizador;
function aviso(texto, tipo = 'ok') {
  const a = document.getElementById('aviso');
  // El contenedor siempre está en la página (vacío = oculto por CSS): así los lectores de
  // pantalla anuncian el texto cuando cambia. Los errores duran más para poder leerse.
  a.textContent = texto;
  a.className = 'aviso ' + tipo;
  clearTimeout(temporizador);
  temporizador = setTimeout(() => { a.textContent = ''; }, tipo === 'error' ? 9000 : 4500);
}

async function api(ruta, opciones = {}) {
  const resp = await fetch(API + ruta, {
    headers: { 'Content-Type': 'application/json' },
    ...opciones
  });
  if (resp.status === 204) return null;
  const texto = await resp.text();
  if (!resp.ok) {
    // Los errores controlados de la API son texto plano; un 5xx o un JSON crudo de Spring no se muestra tal cual
    const esControlado = texto && !texto.trim().startsWith('{') && resp.status < 500;
    throw new Error(esControlado ? texto : 'No se pudo completar la operación (error ' + resp.status + '). Intentá de nuevo.');
  }
  return texto ? JSON.parse(texto) : null;
}

function urlImagenSegura(url) {
  return /^https?:\/\//i.test(url || '') ? url : '';
}

// ---------- navegación ----------
const vistas = ['productos', 'agregar', 'carrito', 'pedidos', 'salir'];

async function mostrar(vista) {
  vistas.forEach(v => { document.getElementById('vista-' + v).hidden = v !== vista; });
  document.querySelectorAll('#nav button').forEach(b => {
    const activo = b.dataset.vista === vista;
    b.classList.toggle('activo', activo);
    if (activo) b.setAttribute('aria-current', 'page'); else b.removeAttribute('aria-current');
  });
  try {
    if (vista === 'productos') await pintarProductos();
    if (vista === 'agregar') await pintarFormulario();
    if (vista === 'carrito') await pintarCarrito();
    if (vista === 'pedidos') await pintarPedidos();
    if (vista === 'salir') salir();
  } catch (e) {
    aviso(e.message, 'error');
  }
  enfocarTitulo(vista);
}

// Al cambiar de vista (o volver a dibujarla) el foco va al título de la sección,
// para que teclado y lectores de pantalla sepan dónde están.
function enfocarTitulo(vista) {
  const h2 = document.querySelector(`#vista-${vista} h2`);
  if (h2) { h2.tabIndex = -1; h2.focus({ preventScroll: true }); }
}

document.getElementById('nav').addEventListener('click', e => {
  const b = e.target.closest('button[data-vista]');
  if (b) mostrar(b.dataset.vista);
});

// ---------- 1. Productos ----------
async function cargarDatos() {
  [productos, categorias] = await Promise.all([api('/productos'), api('/categorias')]);
}

async function pintarProductos() {
  await cargarDatos();
  const filtro = document.getElementById('filtro-categoria');
  const elegido = filtro.value;
  filtro.replaceChildren(el('option', { value: '', textContent: 'Todas' }),
    ...categorias.map(c => el('option', { value: c.nombre, textContent: c.nombre })));
  filtro.value = elegido;
  filtro.onchange = pintarProductos;

  const lista = productos.filter(p => !filtro.value || p.categoria?.nombre === filtro.value);
  const cuerpo = document.getElementById('tabla-productos');
  cuerpo.replaceChildren();

  if (!lista.length) {
    cuerpo.append(el('tr', {}, el('td', { colSpan: 7, class: 'vacio', textContent: 'No hay productos para mostrar.' })));
    return;
  }

  for (const p of lista) {
    const img = urlImagenSegura(p.imagenUrl);
    const acciones = el('td', {});
    if (p.stock > 0) {
      acciones.append(el('button', { class: 'sec', textContent: 'Agregar al carrito',
        ariaLabel: 'Agregar al carrito ' + p.nombre, onclick: () => agregarAlCarrito(p) }));
    } else {
      acciones.append(el('span', { class: 'sin-stock', textContent: 'Sin stock' }));
    }
    cuerpo.append(el('tr', {},
      el('td', { textContent: p.id }),
      el('td', {}, img ? el('img', { class: 'miniatura', src: img, alt: '', onerror: ev => ev.target.remove() }) : ''),
      el('td', {}, el('strong', { textContent: p.nombre }), el('div', { class: 'desc', textContent: p.descripcion || '' })),
      el('td', { textContent: dinero(p.precio) }),
      el('td', { textContent: p.categoria ? p.categoria.nombre : '-' }),
      el('td', { textContent: p.stock }),
      acciones));
  }
}

function agregarAlCarrito(p) {
  const actual = carrito[p.id] || 0;
  if (actual + 1 > p.stock) {
    aviso(`No hay más stock de ${p.nombre} (disponible: ${p.stock}).`, 'error');
    return;
  }
  carrito[p.id] = actual + 1;
  guardarCarrito();
  aviso(`${p.nombre} agregado al carrito.`);
}

// ---------- 2. Agregar producto ----------
async function pintarFormulario() {
  categorias = await api('/categorias');
  const sel = document.getElementById('select-categoria');
  sel.replaceChildren(el('option', { value: '', textContent: 'Seleccioná una categoría' }),
    ...categorias.map(c => el('option', { value: c.id, textContent: c.nombre })));
}

document.getElementById('form-producto').addEventListener('submit', async e => {
  e.preventDefault();
  const enviar = e.target.querySelector('button[type=submit]');
  if (enviar.disabled) return; // evita el doble envío mientras la petición está en curso
  enviar.disabled = true;
  const f = new FormData(e.target);
  const cuerpo = {
    nombre: f.get('nombre'),
    descripcion: f.get('descripcion'),
    precio: Number(f.get('precio')),
    categoriaId: Number(f.get('categoriaId')),
    imagenUrl: f.get('imagenUrl'),
    stock: Number(f.get('stock'))
  };
  try {
    const nuevo = await api('/productos', { method: 'POST', body: JSON.stringify(cuerpo) });
    e.target.reset();
    aviso(`Producto "${nuevo.nombre}" guardado.`);
    mostrar('productos');
  } catch (err) {
    aviso(err.message, 'error');
  } finally {
    enviar.disabled = false;
  }
});

// ---------- 3. Carrito ----------
async function pintarCarrito() {
  await cargarDatos();
  const cont = document.getElementById('carrito-contenido');
  cont.replaceChildren();

  // Quita del carrito los productos que ya no existen o sin stock, y ajusta al stock actual
  for (const id of Object.keys(carrito)) {
    const prod = productos.find(p => String(p.id) === id);
    if (!prod || prod.stock < 1) delete carrito[id];
    else if (carrito[id] > prod.stock) carrito[id] = prod.stock;
  }
  guardarCarrito();

  const items = productos.filter(p => carrito[p.id]);
  if (!items.length) {
    cont.append(el('div', { class: 'vacio', textContent: 'El carrito está vacío. Agregá productos desde la pestaña Productos.' }));
    return;
  }

  let total = 0;
  const cuerpo = el('tbody');
  for (const p of items) {
    const cantidad = carrito[p.id];
    const subtotal = Number(p.precio) * cantidad;
    total += subtotal;
    const campo = el('input', {
      class: 'cant', type: 'number', min: 1, max: p.stock, value: cantidad,
      ariaLabel: 'Cantidad de ' + p.nombre,
      onchange: ev => {
        const n = Math.floor(Number(ev.target.value));
        if (!(n >= 1)) { delete carrito[p.id]; }
        else carrito[p.id] = Math.min(n, p.stock);
        guardarCarrito();
        refrescarCarrito();
      }
    });
    cuerpo.append(el('tr', {},
      el('td', { textContent: p.nombre }),
      el('td', { textContent: dinero(p.precio) }),
      el('td', {}, campo),
      el('td', { textContent: dinero(subtotal) }),
      el('td', {}, el('button', { class: 'peligro', textContent: 'Quitar', ariaLabel: 'Quitar ' + p.nombre,
        onclick: () => { delete carrito[p.id]; guardarCarrito(); refrescarCarrito(); } }))));
  }

  const encabezados = ['Producto', 'Precio', 'Cantidad', 'Subtotal'].map(t => el('th', { scope: 'col', textContent: t }));
  encabezados.push(el('th', { scope: 'col' }, el('span', { class: 'sr-only', textContent: 'Acciones' })));
  const tabla = el('table', {}, el('thead', {}, el('tr', {}, ...encabezados)), cuerpo);
  cont.append(el('div', { class: 'tabla-wrap', tabIndex: 0, role: 'region', ariaLabel: 'Productos del carrito' }, tabla));

  const boton = el('button', { class: 'primario', textContent: 'Realizar pedido', onclick: () => realizarPedido(boton) });
  cont.append(el('div', { class: 'resumen' },
    el('span', { class: 'total', textContent: 'Total: ' + dinero(total) }),
    el('div', {},
      el('button', { class: 'sec', textContent: 'Vaciar', onclick: () => { carrito = {}; guardarCarrito(); refrescarCarrito(); } }),
      ' ', boton)));
}

// Redibuja el carrito tras un cambio hecho por el usuario y devuelve el foco al título
async function refrescarCarrito() {
  try { await pintarCarrito(); } catch (e) { aviso(e.message, 'error'); }
  enfocarTitulo('carrito');
}

async function realizarPedido(boton) {
  // Copia de lo que se envía: si el usuario agrega algo mientras la petición está en curso, no se pierde
  const lineas = Object.entries(carrito).map(([productoId, cantidad]) => ({ productoId: Number(productoId), cantidad }));
  boton.disabled = true;
  try {
    const pedido = await api('/pedidos', { method: 'POST', body: JSON.stringify({ usuarioId: USUARIO_ID, lineas }) });
    for (const l of lineas) {
      const resto = (carrito[l.productoId] || 0) - l.cantidad;
      if (resto > 0) carrito[l.productoId] = resto; else delete carrito[l.productoId];
    }
    guardarCarrito();
    aviso(`Pedido #${pedido.id} creado (${pedido.estado}). Total ${dinero(pedido.total)}.`);
    mostrar('pedidos');
  } catch (e) {
    // Por ejemplo: 400 "Stock insuficiente para ..."
    aviso(e.message, 'error');
    boton.disabled = false;
    refrescarCarrito();
  }
}

// ---------- 4. Pedidos ----------
async function pintarPedidos() {
  const cont = document.getElementById('pedidos-contenido');
  const pedidos = await api(`/usuarios/${USUARIO_ID}/pedidos`);
  cont.replaceChildren();
  if (!pedidos.length) {
    cont.append(el('div', { class: 'vacio', textContent: 'Todavía no realizaste pedidos.' }));
    return;
  }
  for (const p of pedidos) {
    const fecha = new Date(p.fecha).toLocaleString('es-AR');
    cont.append(el('div', { class: 'tarjeta' },
      el('div', { class: 'tarjeta-cab' },
        el('strong', { textContent: `Pedido #${p.id}` }),
        el('span', { textContent: fecha }),
        el('span', { class: 'estado', textContent: p.estado }),
        el('strong', { textContent: dinero(p.total) })),
      el('ul', {}, ...p.lineas.map(l =>
        el('li', { textContent: `${l.nombre} - ${l.cantidad} x ${dinero(l.precioUnitario)} = ${dinero(l.subtotal)}` })))));
  }
}

// ---------- 5. Salir ----------
function salir() {
  carrito = {};
  guardarCarrito();
}
document.getElementById('btn-entrar').addEventListener('click', () => mostrar('productos'));

// ---------- inicio ----------
guardarCarrito();
mostrar('productos');
