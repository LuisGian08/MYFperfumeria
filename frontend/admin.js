const API = window.MYF_API ||
    ((location.hostname === 'localhost' || location.hostname === '127.0.0.1')
        ? 'http://localhost:8080'
        : '');

function assetUrl(path) {
  if (!path) return '';

  try {
    const apiUrl = API ? new URL(API) : null;
    const localApi = !apiUrl ||
        apiUrl.hostname === 'localhost' ||
        apiUrl.hostname === '127.0.0.1';
    const base = localApi ? location.origin : apiUrl.origin;
    const url = new URL(path, base);

    if (!localApi &&
        (url.hostname === 'localhost' || url.hostname === '127.0.0.1')) {
      return new URL(`${url.pathname}${url.search}${url.hash}`, apiUrl.origin).href;
    }

    return url.href;
  } catch {
    return path;
  }
}

let token = localStorage.getItem('myf_token');
let userId = Number(localStorage.getItem('myf_user') || 1);

let products = [];
let inventories = [];
let brands = [];
let categories = [];
let orders = [];

const $ = id => document.getElementById(id);

const money = n => `S/ ${Number(n || 0).toFixed(2)}`;

const authHeaders = () => ({
  'Content-Type': 'application/json',
  'Authorization': 'Bearer ' + token
});


// ======================================================
// PETICIONES
// ======================================================

async function req(path, opt = {}) {

  const r = await fetch(API + path, {
    ...opt,
    headers: {
      ...authHeaders(),
      ...(opt.headers || {})
    }
  });

  const t = await r.text();

  let d;

  try {
    d = t ? JSON.parse(t) : null;
  } catch {
    d = t;
  }

  if (!r.ok) {
    throw new Error(d?.error || t || `HTTP ${r.status}`);
  }

  return d;
}


// ======================================================
// MENSAJES
// ======================================================

function toast(m) {

  $('toast').textContent = m;
  $('toast').classList.add('show');

  clearTimeout(window._toast);

  window._toast = setTimeout(() => {
    $('toast').classList.remove('show');
  }, 2400);
}


// ======================================================
// LOGIN / PANEL
// ======================================================

function showApp() {

  const loginScreen = $('loginScreen');
  const app = $('app');

  if (token) {

    loginScreen.style.display = 'none';

    app.hidden = false;
    app.style.display = '';

    loadProducts();
    loadDashboard();

  } else {

    loginScreen.style.display = 'flex';
    loginScreen.style.width = '100%';
    loginScreen.style.minHeight = '100vh';
    loginScreen.style.justifyContent = 'center';
    loginScreen.style.alignItems = 'center';

    app.hidden = true;
    app.style.display = 'none';
  }
}


$('login').onclick = async () => {

  try {

    const r = await fetch(API + '/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: $('email').value,
        password: $('password').value
      })
    });

    const d = await r.json();

    if (!r.ok) {
      throw new Error(d.error || 'Credenciales incorrectas');
    }

    token = d.token;
    userId = d.userId;

    localStorage.setItem('myf_token', token);
    localStorage.setItem('myf_user', userId);

    $('loginMsg').textContent = '';

    showApp();

  } catch (e) {

    $('loginMsg').textContent = e.message;
  }
};


$('logout').onclick = () => {

  token = null;

  localStorage.removeItem('myf_token');
  localStorage.removeItem('myf_user');

  showApp();
};


// ======================================================
// NAVEGACIÓN
// ======================================================

const title = {
  dashboard: 'Dashboard',
  productos: 'Productos',
  inventario: 'Inventario',
  ventas: 'Nueva venta',
  pedidos: 'Pedidos',
  clientes: 'Clientes',
  alertas: 'Alertas',
  reportes: 'Reportes'
};


function switchTab(name) {

  document.querySelectorAll('.tab').forEach(x => x.hidden = true);

  $(name).hidden = false;

  document.querySelectorAll('.sidebar nav button').forEach(x => {
    x.classList.toggle('active', x.dataset.tab === name);
  });

  $('pageTitle').textContent = title[name] || name;

  if (name === 'productos') loadProducts();
  if (name === 'inventario') loadInventory();
  if (name === 'ventas') setupSale();
  if (name === 'pedidos') loadOrders();
  if (name === 'clientes') loadClients();
  if (name === 'alertas') loadAlerts();
  if (name === 'reportes') loadReports();
  if (name === 'dashboard') loadDashboard();
}


document.querySelectorAll('[data-tab]').forEach(b => {
  b.onclick = () => switchTab(b.dataset.tab);
});

document.querySelectorAll('[data-tab-jump]').forEach(b => {
  b.onclick = () => switchTab(b.dataset.tabJump);
});


// ======================================================
// PRODUCTOS
// ======================================================

async function loadProducts() {

  try {

    [products, brands, categories] = await Promise.all([
      req('/api/admin/productos'),
      req('/api/marcas'),
      req('/api/categorias')
    ]);

    renderProducts();
    fillProductSelectors();

  } catch (e) {

    console.error('ERROR CARGANDO PRODUCTOS:', e);
    toast('Error cargando productos: ' + e.message);
  }
}


function renderProducts() {

  if (!$('productRows')) return;

  $('productRows').innerHTML = products.map(p => {

    const precios = (p.presentaciones || [{precio: p.precio}])
        .map(x => Number(x.precio));

    const desde = precios.length
        ? Math.min(...precios)
        : Number(p.precio || 0);

    return `
        <tr>

            <td>
                <div class="mini-product">

                    <div class="thumb">
                        ${
        p.imagenUrl
            ? `<img src="${assetUrl(p.imagenUrl)}" alt="${p.nombre}">`
            : 'MYF'
    }
                    </div>

                    <div>
                        <b>${p.nombre}</b><br>
                        <small>${p.codigo} · ${p.estado}</small>
                    </div>

                </div>
            </td>

            <td>${p.marca || '—'}</td>

            <td>${p.genero}</td>

            <td>
                ${(p.presentaciones || []).map(x => `
                    <span class="badge ${x.tipo === 'DECANT' ? 'decant' : ''}">
                        ${x.tipo === 'DECANT' ? 'D' : 'B'}
                        ${Number(x.volumenMl)} ml
                    </span>
                `).join('')}
            </td>

            <td>${money(desde)}</td>

            <td>
                <button class="row-action" data-edit="${p.id}">
                    Editar
                </button>

                ·

                <button class="row-action" data-product-state="${p.id}" data-next-state="${p.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO'}">
                    ${p.estado === 'ACTIVO' ? 'Desactivar' : 'Activar'}
                </button>
            </td>

        </tr>
        `;
  }).join('');


  document.querySelectorAll('[data-edit]').forEach(b => {

    b.onclick = () => {
      openProductForm(Number(b.dataset.edit));
    };
  });


  document.querySelectorAll('[data-product-state]').forEach(b => {

    b.onclick = () => {
      changeProductState(Number(b.dataset.productState), b.dataset.nextState);
    };
  });
}


$('newProduct').onclick = () => openProductForm();


// ======================================================
// MARCAS
// ======================================================

$('newBrand').onclick = async () => {

  const nombre = prompt('Nombre de la nueva marca:');

  if (!nombre?.trim()) return;

  const pais = prompt('País de origen (opcional):') || '';

  try {

    await req('/api/marcas', {
      method: 'POST',
      body: JSON.stringify({
        nombre: nombre.trim(),
        paisOrigen: pais.trim() || null,
        estado: 'ACTIVO'
      })
    });

    toast('Marca creada');

    await loadProducts();

  } catch (e) {

    toast(e.message);
  }
};


// ======================================================
// CATEGORÍAS
// ======================================================

$('newCategory').onclick = async () => {

  const nombre = prompt('Nombre de la nueva categoría:');

  if (!nombre?.trim()) return;

  const descripcion =
      prompt('Descripción (opcional):') || '';

  try {

    await req('/api/categorias', {
      method: 'POST',
      body: JSON.stringify({
        nombre: nombre.trim(),
        descripcion: descripcion.trim() || null,
        estado: 'ACTIVO'
      })
    });

    toast('Categoría creada');

    await loadProducts();

  } catch (e) {

    toast(e.message);
  }
};


// ======================================================
// MODAL PRODUCTO
// ======================================================

$('closeProductModal').onclick = () => {
  $('productModal').classList.remove('open');
};


// ======================================================
// SUBIR IMAGEN DEL PRODUCTO
// ======================================================

$('pImageFile').onchange = async function () {

  const archivo = this.files[0];

  if (!archivo) return;


  // Vista previa inmediata

  const preview = $('pImagePreview');
  const previewBox = $('pImagePreviewBox');

  preview.src = URL.createObjectURL(archivo);
  previewBox.style.display = 'block';


  // Preparar archivo

  const formData = new FormData();

  formData.append('imagen', archivo);


  try {

    const respuesta = await fetch(
        API + '/api/productos/imagen',
        {
          method: 'POST',

          headers: {
            'Authorization': 'Bearer ' + token
          },

          body: formData
        }
    );


    let datos;

    try {
      datos = await respuesta.json();
    } catch {
      datos = {};
    }


    if (!respuesta.ok) {

      throw new Error(
          datos.error ||
          'No se pudo subir la imagen'
      );
    }


    // Guardamos URL de la imagen

    $('pImage').value = datos.url;


    toast('Imagen subida correctamente');


  } catch (e) {

    $('pImage').value = '';

    toast(
        'Error al subir imagen: ' +
        e.message
    );
  }
};


// ======================================================
// SELECTORES MARCA / CATEGORÍA
// ======================================================

function fillCatalogSelects() {

  $('pBrand').innerHTML =
      '<option value="">Sin marca</option>' +
      brands.map(x =>
          `<option value="${x.id}">
                ${x.nombre}
            </option>`
      ).join('');


  $('pCategory').innerHTML =
      '<option value="">Sin categoría</option>' +
      categories.map(x =>
          `<option value="${x.id}">
                ${x.nombre}
            </option>`
      ).join('');
}


// ======================================================
// ABRIR FORMULARIO PRODUCTO
// ======================================================

function openProductForm(id) {

  fillCatalogSelects();

  $('productForm').reset();

  $('productId').value = '';
  $('pImage').value = '';

  $('vBottle').checked = true;
  $('vBottleMl').value = 100;

  $('productFormMsg').textContent = '';

  $('productFormTitle').textContent =
      id
          ? 'Editar perfume'
          : 'Nuevo perfume';


  // Limpiar vista previa

  $('pImagePreview').src = '';

  $('pImagePreviewBox').style.display =
      'none';


  // EDITAR PRODUCTO

  if (id) {

    const p = products.find(
        x => Number(x.id) === Number(id)
    );

    if (!p) return;


    $('productId').value = p.id;

    $('pCode').value =
        p.codigo || '';

    $('pName').value =
        p.nombre || '';

    $('pBrand').value =
        p.marcaId || '';

    $('pCategory').value =
        p.categoriaId || '';

    $('pGender').value =
        p.genero || 'UNISEX';

    $('pImage').value =
        p.imagenUrl || '';

    $('pDescription').value =
        p.descripcion || '';


    // Mostrar imagen existente

    if (p.imagenUrl) {

      $('pImagePreview').src =
          assetUrl(p.imagenUrl);

      $('pImagePreviewBox').style.display =
          'block';
    }


    const configuraciones = [

      [
        'BOTELLA',
        'vBottle',
        'vBottleMl',
        'vBottlePrice',
        'vBottleCost'
      ],

      [
        5,
        'v5',
        null,
        'v5Price'
      ],

      [
        10,
        'v10',
        null,
        'v10Price'
      ],

      [
        30,
        'v30',
        null,
        'v30Price'
      ]
    ];


    configuraciones.forEach(a => {

      let pr;

      if (a[0] === 'BOTELLA') {

        pr =
            (p.presentaciones || [])
                .find(
                    x => x.tipo === 'BOTELLA'
                );

      } else {

        pr =
            (p.presentaciones || [])
                .find(
                    x =>
                        x.tipo === 'DECANT' &&
                        Number(x.volumenMl) ===
                        Number(a[0])
                );
      }


      $(a[1]).checked = !!pr;


      if (pr) {

        if (a[2]) {

          $(a[2]).value =
              Number(pr.volumenMl);
        }


        $(a[3]).value =
            Number(pr.precio);


        if (a[4]) {

          $(a[4]).value =
              Number(pr.costo || 0);
        }
      }
    });
  }


  $('productModal').classList.add('open');
}


// ======================================================
// GUARDAR PRODUCTO
// ======================================================

$('productForm').onsubmit = async e => {

  e.preventDefault();


  const prs = [];


  // BOTELLA

  if ($('vBottle').checked) {

    prs.push({

      tipo: 'BOTELLA',

      volumenMl:
          Number($('vBottleMl').value),

      precio:
          Number(
              $('vBottlePrice').value || 0
          ),

      costo:
          Number(
              $('vBottleCost').value || 0
          ),

      activo: true
    });
  }


  // DECANTS

  for (
      const [check, price, ml]
      of [
    ['v5', 'v5Price', 5],
    ['v10', 'v10Price', 10],
    ['v30', 'v30Price', 30]
  ]
      ) {

    if ($(check).checked) {

      prs.push({

        tipo: 'DECANT',

        volumenMl: ml,

        precio:
            Number(
                $(price).value || 0
            ),

        costo: 0,

        activo: true
      });
    }
  }


  if (!prs.length) {

    $('productFormMsg').textContent =
        'Activa al menos una presentación.';

    return;
  }


  const main =
      prs.find(
          x => x.tipo === 'BOTELLA'
      ) || prs[0];


  const body = {

    categoriaId:
        Number(
            $('pCategory').value
        ) || null,

    marcaId:
        Number(
            $('pBrand').value
        ) || null,

    codigo:
        $('pCode').value.trim(),

    nombre:
        $('pName').value.trim(),

    descripcion:
        $('pDescription').value.trim(),

    tipo:
        main.tipo === 'BOTELLA'
            ? 'PERFUME'
            : 'DECANT',

    volumenMl:
    main.volumenMl,

    genero:
    $('pGender').value,

    precio:
    main.precio,

    costo:
        main.costo || 0,

    imagenUrl:
        $('pImage').value.trim() || null,

    presentaciones:
    prs
  };


  try {

    const id =
        $('productId').value;


    await req(
        '/api/productos' +
        (id ? '/' + id : ''),
        {
          method:
              id ? 'PUT' : 'POST',

          body:
              JSON.stringify(body)
        }
    );


    $('productModal')
        .classList
        .remove('open');


    toast('Producto guardado');


    await loadProducts();


  } catch (err) {

    $('productFormMsg').textContent =
        err.message;
  }
};


// ======================================================
// DESACTIVAR PRODUCTO
// ======================================================

async function changeProductState(id, estado) {

  const action = estado === 'ACTIVO' ? 'activar' : 'desactivar';
  if (!confirm(`¿${action} este producto?`)) return;


  try {

    await req(
        '/api/productos/' + id + '/estado',
        {
          method: 'PUT',
          body: JSON.stringify({ estado })
        }
    );

    toast(`Producto ${estado === 'ACTIVO' ? 'activado' : 'desactivado'}`);

    loadProducts();

  } catch (e) {

    toast(e.message);
  }
}


// ======================================================
// INVENTARIO
// ======================================================

async function loadInventory() {

  try {

    inventories =
        await req('/api/inventario');


    if (!products.length) {
      await loadProducts();
    }


    renderInventory();


    const mov =
        await req(
            '/api/inventario/movimientos'
        );


    renderMovements(mov);


  } catch (e) {

    toast(e.message);
  }
}


function invFor(id) {

  return inventories.find(
      x =>
          Number(x.productoId) ===
          Number(id)
  ) || {

    stockActual: 0,

    stockMinimo: 2,

    volumenDecantMl: 0,

    volumenDecantMinimoMl: 20
  };
}


function renderInventory() {

  $('inventoryGrid').innerHTML =
      products.map(p => {

        const x =
            invFor(p.id);


        const bottle =
            (p.presentaciones || [])
                .find(
                    z =>
                        z.tipo ===
                        'BOTELLA'
                );


        const hasBottle =
            !!bottle;


        const hasD =
            (p.presentaciones || [])
                .some(
                    z =>
                        z.tipo ===
                        'DECANT'
                );


        const level =
            hasBottle
                ? (
                    x.stockActual === 0
                        ? 'out'
                        : x.stockActual <=
                        x.stockMinimo
                            ? 'low'
                            : 'ok'
                )
                : 'ok';


        const bottleMl =
            bottle
                ? Number(
                    bottle.volumenMl
                )
                : 0;


        return `

            <div class="inv-card">

                <h4>
                    ${p.nombre}
                </h4>

                <small>
                    ${p.marca || ''}
                    · #${p.id}
                </small>

                <p class="status ${level}">

                    ${
            hasBottle
                ? x.stockActual +
                ' botellas'
                : 'Sin venta por botella'
        }

                    ·

                    ${
            hasD
                ? (x.volumenDecantMl || 0) +
                ' ml decant'
                : 'Sin decants'
        }

                </p>


                <div class="inv-fields">

                    <label>

                        Botellas

                        <input
                            id="stock-${p.id}"
                            type="number"
                            min="0"
                            value="${
            hasBottle
                ? x.stockActual
                : 0
        }"
                            ${
            hasBottle
                ? ''
                : 'disabled'
        }
                        >

                    </label>


                    <label>

                        Mínimo

                        <input
                            id="min-${p.id}"
                            type="number"
                            min="0"
                            value="${
            hasBottle
                ? x.stockMinimo
                : 0
        }"
                            ${
            hasBottle
                ? ''
                : 'disabled'
        }
                        >

                    </label>


                    <label>

                        ml decant

                        <input
                            id="ml-${p.id}"
                            type="number"
                            min="0"
                            value="${
            hasD
                ? (x.volumenDecantMl || 0)
                : 0
        }"
                            ${
            hasD
                ? ''
                : 'disabled'
        }
                        >

                    </label>


                    <label>

                        Mín. ml

                        <input
                            id="mlmin-${p.id}"
                            type="number"
                            min="0"
                            value="${
            hasD
                ? (x.volumenDecantMinimoMl || 20)
                : 0
        }"
                            ${
            hasD
                ? ''
                : 'disabled'
        }
                        >

                    </label>

                </div>


                ${
            hasBottle && hasD
                ? `
                        <button
                            class="open-bottle"
                            data-open-bottle="${p.id}"
                            data-bottle-ml="${bottleMl}"
                        >
                            Abrir 1 botella para decants
                            (+${bottleMl} ml)
                        </button>
                        `
                : ''
        }


                <button
                    data-save-inv="${p.id}"
                >
                    Guardar inventario
                </button>

            </div>
            `;

      }).join('');


  document
      .querySelectorAll(
          '[data-save-inv]'
      )
      .forEach(b => {

        b.onclick = () =>
            saveInventory(
                Number(
                    b.dataset.saveInv
                )
            );
      });


  document
      .querySelectorAll(
          '[data-open-bottle]'
      )
      .forEach(b => {

        b.onclick = () =>
            openBottleForDecants(
                Number(
                    b.dataset.openBottle
                ),
                Number(
                    b.dataset.bottleMl
                )
            );
      });
}


// ======================================================
// ABRIR BOTELLA PARA DECANTS
// ======================================================

async function openBottleForDecants(
    id,
    ml
) {

  if (!confirm(
      `¿Abrir una botella cerrada y agregar ${ml} ml al inventario de decants?`
  )) return;


  try {

    await req(
        `/api/inventario/${id}/decant/abrir`,
        {
          method: 'POST',

          body: JSON.stringify({

            mililitros: ml,

            motivo:
                'Botella abierta para preparar decants',

            usuarioId:
            userId
          })
        }
    );


    toast(
        'Botella abierta para decants'
    );


    loadInventory();


  } catch (e) {

    toast(e.message);
  }
}


// ======================================================
// GUARDAR INVENTARIO
// ======================================================

async function saveInventory(id) {

  const p =
      products.find(
          x =>
              Number(x.id) ===
              Number(id)
      );


  const hasBottle =
      (p?.presentaciones || [])
          .some(
              x =>
                  x.tipo ===
                  'BOTELLA'
          );


  const hasD =
      (p?.presentaciones || [])
          .some(
              x =>
                  x.tipo ===
                  'DECANT'
          );


  try {

    await req(
        `/api/inventario/${id}/configurar`,
        {
          method: 'POST',

          body: JSON.stringify({

            stockActual:
                hasBottle
                    ? Number(
                        $(
                            `stock-${id}`
                        ).value
                    )
                    : 0,

            stockMinimo:
                hasBottle
                    ? Number(
                        $(
                            `min-${id}`
                        ).value
                    )
                    : 0,

            volumenDecantMl:
                hasD
                    ? Number(
                        $(
                            `ml-${id}`
                        ).value || 0
                    )
                    : 0,

            volumenDecantMinimoMl:
                hasD
                    ? Number(
                        $(
                            `mlmin-${id}`
                        ).value || 20
                    )
                    : 0
          })
        }
    );


    toast(
        'Inventario actualizado'
    );


    loadInventory();


  } catch (e) {

    toast(e.message);
  }
}


// ======================================================
// MOVIMIENTOS INVENTARIO
// ======================================================

function renderMovements(m) {

  $('movementRows').innerHTML =
      (m || [])
          .slice(0, 30)
          .map(x => `

                <tr>

                    <td>
                        ${
              new Date(
                  x.fechaMovimiento
              ).toLocaleString()
          }
                    </td>

                    <td>
                        #${x.productoId}
                    </td>

                    <td>
                        ${x.tipoMovimiento}
                    </td>

                    <td>
                        ${x.cantidad}
                        ${
              x.unidad === 'ML'
                  ? 'ml'
                  : 'u.'
          }
                    </td>

                    <td>
                        ${x.motivo || ''}
                    </td>

                </tr>

            `).join('')

      ||

      '<tr><td colspan="5">Sin movimientos</td></tr>';
}


$('reloadInventory').onclick =
    loadInventory;


// ======================================================
// NUEVA VENTA
// ======================================================

function fillProductSelectors() {

  if (!$('saleProduct')) return;


  $('saleProduct').innerHTML =
      products.map(p => `
            <option value="${p.id}">
                ${p.nombre}
            </option>
        `).join('');


  fillSalePresentations();
}


function fillSalePresentations() {

  const p =
      products.find(
          x =>
              Number(x.id) ===
              Number(
                  $('saleProduct').value
              )
      );


  $('salePresentation').innerHTML =
      (p?.presentaciones || [])
          .map(x => `

                <option value="${x.id}">
                    ${x.tipo}
                    ${Number(x.volumenMl)} ml
                    · ${money(x.precio)}
                </option>

            `).join('');
}


$('saleProduct').onchange =
    fillSalePresentations;


async function setupSale() {

  if (!products.length) {
    await loadProducts();
  }

  fillProductSelectors();
}


$('makeSale').onclick =
    async () => {

      const b =
          $('makeSale');

      b.disabled = true;


      try {

        const d =
            await req(
                '/api/ventas',
                {
                  method: 'POST',

                  body: JSON.stringify({

                    usuarioId:
                    userId,

                    descuento:
                        0,

                    metodoPago:
                    $('salePay').value,

                    observacion:
                    $('saleNotes').value,

                    canal:
                        'ADMIN',

                    items: [
                      {
                        productoId:
                            Number(
                                $('saleProduct').value
                            ),

                        presentacionId:
                            Number(
                                $('salePresentation').value
                            ),

                        cantidad:
                            Number(
                                $('saleQty').value
                            )
                      }
                    ]
                  })
                }
            );


        $('saleMsg').textContent =
            `Venta ${d.numeroVenta} registrada · ${money(d.total)}`;


        toast(
            'Venta registrada'
        );


        loadDashboard();


      } catch (e) {

        $('saleMsg').textContent =
            e.message;

      } finally {

        b.disabled = false;
      }
    };


// ======================================================
// PEDIDOS
// ======================================================

async function loadOrders() {

  try {

    orders = await req('/api/ventas');

    $('ordersList').innerHTML = orders.map(v => {

      const tipoEntrega =
          v.tipoEntrega ||
          (v.canal === 'ADMIN' ? 'VENTA DIRECTA' : 'RECOJO');

      const estadoPago =
          v.estadoPago ||
          (v.canal === 'ADMIN' ? 'PAGADO' : 'PENDIENTE');


      // ======================================================
      // COMPROBANTE DE PAGO
      // ======================================================

      let comprobanteHtml = `
        <span style="color:#777;">
          Sin comprobante
        </span>
      `;

      if (v.comprobanteUrl) {

        const comprobanteUrl =
            assetUrl(v.comprobanteUrl);

        comprobanteHtml = `
          <a
            href="${comprobanteUrl}"
            target="_blank"
            rel="noopener noreferrer"
            style="
              display:inline-block;
              margin-top:5px;
              padding:7px 12px;
              border-radius:7px;
              background:#1662A0;
              color:white;
              text-decoration:none;
              font-size:13px;
              font-weight:700;
            "
          >
            Ver comprobante
          </a>
        `;
      }


      return `

        <div class="order-card">

          <div>

            <h4>
              ${v.numeroVenta}
              ·
              ${money(v.total)}
            </h4>

            <small>
              ${new Date(v.fechaVenta).toLocaleString()}
              ·
              ${v.canal}
            </small>


            <div style="margin-top:10px; line-height:1.8;">

              <div>
                <strong>Entrega:</strong>
                ${tipoEntrega}
              </div>

              <div>
                <strong>Método de pago:</strong>
                ${v.metodoPago}
              </div>

              <div>

                <strong>Estado de pago:</strong>

                <span style="
                  font-weight:700;
                  ${
          estadoPago === 'PAGADO'
              ? 'color:#18864b;'
              : 'color:#d97706;'
      }
                ">
                  ${estadoPago}
                </span>

              </div>


              <div style="margin-top:5px;">

                <strong>Comprobante:</strong>

                <div>
                  ${comprobanteHtml}
                </div>

              </div>

            </div>


            <div class="order-items">

              ${
          (v.detalles || [])
              .map(d => `
                      ${d.nombreProducto}
                      ·
                      ${d.tipoProducto}
                      ${Number(d.volumenMl || 0)} ml
                      ×
                      ${d.cantidad}
                    `)
              .join('<br>')
      }

            </div>

          </div>


          <div>

            <small>
              Cliente ID
            </small>

            <div>
              ${v.clienteId || 'Venta directa'}
            </div>

          </div>


          <div>

            <small>
              Estado del pago
            </small>

            <select
              data-payment-state="${v.id}"
              ${v.estado === 'CANCELADO' ? 'disabled' : ''}
            >

              <option
                value="PENDIENTE"
                ${estadoPago === 'PENDIENTE' ? 'selected' : ''}
              >
                PENDIENTE
              </option>

              <option
                value="PAGADO"
                ${estadoPago === 'PAGADO' ? 'selected' : ''}
              >
                PAGADO
              </option>

            </select>

          </div>


          <div>

            <small>
              Estado del pedido
            </small>

            <select data-order-state="${v.id}">

              ${
          [
            'PENDIENTE',
            'CONFIRMADO',
            'PREPARANDO',
            'LISTO',
            'ENTREGADO',
            'REGISTRADA',
            'CANCELADO'
          ]
              .map(s => `
                      <option
                        value="${s}"
                        ${v.estado === s ? 'selected' : ''}
                      >
                        ${s}
                      </option>
                    `)
              .join('')
      }

            </select>

          </div>

        </div>

      `;

    }).join('') || 'Sin pedidos';


    document
        .querySelectorAll('[data-order-state]')
        .forEach(s => {

          s.onchange = () =>
              changeOrder(
                  Number(s.dataset.orderState),
                  s.value
              );

        });


    document
        .querySelectorAll('[data-payment-state]')
        .forEach(s => {

          s.onchange = () =>
              changePaymentStatus(
                  Number(s.dataset.paymentState),
                  s.value
              );

        });


  } catch (e) {

    toast(e.message);

  }
}
async function changePaymentStatus(id, estadoPago) {

  try {

    await req(
        `/api/ventas/${id}/pago`,
        {
          method: 'PUT',
          body: JSON.stringify({
            estadoPago: estadoPago
          })
        }
    );

    toast(
        estadoPago === 'PAGADO'
            ? 'Pago marcado como PAGADO'
            : 'Pago marcado como PENDIENTE'
    );

    await loadOrders();

  } catch (e) {

    toast(e.message);

    await loadOrders();

  }
}
async function changeOrder(
    id,
    state
) {

  try {

    await req(
        `/api/ventas/${id}/estado`,
        {
          method: 'PUT',

          body: JSON.stringify({
            estado: state
          })
        }
    );


    toast(
        'Estado actualizado'
    );


    loadOrders();
    loadInventory();


  } catch (e) {

    toast(e.message);
  }
}


$('reloadOrders').onclick =
    loadOrders;


// ======================================================
// CLIENTES
// ======================================================

async function loadClients() {

  try {

    const c =
        await req(
            '/api/clientes'
        );


    $('clientRows').innerHTML =
        c.map(x => `

                <tr>

                    <td>
                        <b>
                            ${x.nombre}
                        </b>
                    </td>

                    <td>
                        ${x.telefono || '—'}
                    </td>

                    <td>
                        ${x.email || '—'}
                    </td>

                    <td>
                        ${x.direccion || '—'}
                    </td>

                </tr>

            `).join('')

        ||

        '<tr><td colspan="4">Aún no hay clientes registrados.</td></tr>';


  } catch (e) {

    toast(e.message);
  }
}


// ======================================================
// ALERTAS
// ======================================================

async function loadAlerts() {

  try {

    const a =
        await req(
            '/api/alertas'
        );

    renderAlerts(
        a,
        $('alertsList')
    );


  } catch (e) {

    toast(e.message);
  }
}


function renderAlerts(a, el) {

  el.innerHTML =
      (a || [])
          .map(x => `

                <div class="alert-item ${
              String(x.tipo).includes('SIN_')
                  ? 'danger'
                  : ''
          }">

                    <div>

                        <b>
                            ${
              x.tipo.replaceAll(
                  '_',
                  ' '
              )
          }
                        </b>

                        <br>

                        <small>
                            Producto #${x.productoId}
                            ·
                            ${x.stockActual}/${x.stockMinimo}
                            ${
              x.unidad === 'ML'
                  ? 'ml'
                  : 'u.'
          }
                        </small>

                        <div>
                            ${x.mensaje}
                        </div>

                    </div>


                    <button
                        class="row-action"
                        data-alert="${x.id}"
                    >
                        Atender
                    </button>

                </div>

            `).join('')

      ||

      '<p>Sin alertas pendientes.</p>';


  el.querySelectorAll(
      '[data-alert]'
  ).forEach(b => {

    b.onclick = async () => {

      await req(
          `/api/alertas/${b.dataset.alert}/atender`,
          {
            method: 'PUT'
          }
      );

      loadAlerts();
      loadDashboard();
    };
  });
}


$('reloadAlerts').onclick =
    loadAlerts;


// ======================================================
// REPORTES
// ======================================================

async function loadReports() {

  try {

    const [dash, daily, prod] =
        await Promise.all([

          req(
              '/api/reportes/dashboard'
          ),

          req(
              '/api/reportes/ventas'
          ),

          req(
              '/api/reportes/productos'
          )
        ]);


    $('reportStats').innerHTML =
        statsHtml(dash);


    $('dailyReports').innerHTML =
        (daily || [])
            .sort(
                (a, b) =>
                    String(b.fecha)
                        .localeCompare(
                            String(a.fecha)
                        )
            )
            .map(x => `

                    <div class="report-row">

                        <span>

                            ${x.fecha}

                            <br>

                            <small>
                                ${x.botellasVendidas || 0}
                                botellas
                                ·
                                ${x.decantsVendidos || 0}
                                decants
                            </small>

                        </span>

                        <b>
                            ${x.cantidadVentas}
                            ventas
                            ·
                            ${money(x.totalVentas)}
                        </b>

                    </div>

                `).join('')

        ||

        'Sin datos';


    $('productReports').innerHTML =
        (prod || [])
            .map(x => `

                    <div class="report-row">

                        <span>

                            ${x.nombreProducto}

                            <br>

                            <small>
                                ${x.tipoPresentacion}
                                ${Number(x.volumenMl || 0)}
                                ml
                            </small>

                        </span>

                        <b>
                            ${x.cantidadVendida}
                            ·
                            ${money(x.totalVendido)}
                        </b>

                    </div>

                `).join('')

        ||

        'Sin datos';


  } catch (e) {

    toast(e.message);
  }
}


function statsHtml(d) {

  return `

        <div class="stat">

            <small>
                Ventas hoy
            </small>

            <strong>
                ${d.ventasHoy || 0}
            </strong>

        </div>


        <div class="stat">

            <small>
                Ingresos hoy
            </small>

            <strong>
                ${money(d.ingresosHoy)}
            </strong>

        </div>


        <div class="stat">

            <small>
                Unidades
            </small>

            <strong>
                ${d.unidadesHoy || 0}
            </strong>

        </div>


        <div class="stat">

            <small>
                Botellas
            </small>

            <strong>
                ${d.botellasHoy || 0}
            </strong>

        </div>


        <div class="stat">

            <small>
                Decants
            </small>

            <strong>
                ${d.decantsHoy || 0}
            </strong>

        </div>
    `;
}


// ======================================================
// DASHBOARD
// ======================================================

async function loadDashboard() {

  try {

    const [dash, inv, a] =
        await Promise.all([

          req(
              '/api/reportes/dashboard'
          ),

          req(
              '/api/inventario'
          ),

          req(
              '/api/alertas'
          )
        ]);


    $('dashboardStats').innerHTML =
        statsHtml(dash);


    inventories = inv;


    if (!products.length) {

      products =
          await req(
              '/api/admin/productos'
          );
    }


    const attention =
        products
            .map(p => ({
              p,
              i: invForFrom(
                  inv,
                  p.id
              )
            }))
            .filter(x => {

              const prs =
                  x.p.presentaciones || [];


              const b =
                  prs.some(
                      z =>
                          z.tipo ===
                          'BOTELLA'
                  )
                  &&
                  x.i.stockMinimo > 0
                  &&
                  x.i.stockActual <=
                  x.i.stockMinimo;


              const d =
                  prs.some(
                      z =>
                          z.tipo ===
                          'DECANT'
                  )
                  &&
                  x.i.volumenDecantMinimoMl > 0
                  &&
                  x.i.volumenDecantMl <=
                  x.i.volumenDecantMinimoMl;


              return b || d;
            })
            .slice(0, 8);


    $('dashboardInventory').innerHTML =
        attention.map(x => `

                <div class="report-row">

                    <span>
                        ${x.p.nombre}
                    </span>

                    <b>
                        ${x.i.stockActual}
                        u.
                        ·
                        ${x.i.volumenDecantMl}
                        ml
                    </b>

                </div>

            `).join('')

        ||

        '<p>Inventario en niveles normales.</p>';


    renderAlerts(
        a.slice(0, 5),
        $('dashboardAlerts')
    );


  } catch (e) {

    toast(e.message);
  }
}


function invForFrom(arr, id) {

  return arr.find(
      x =>
          Number(x.productoId) ===
          Number(id)
  ) || {

    stockActual: 0,

    stockMinimo: 2,

    volumenDecantMl: 0,

    volumenDecantMinimoMl: 20
  };
}


// ======================================================
// INICIAR
// ======================================================

showApp();