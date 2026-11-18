const API_BASE = window.MYF_API ||
    ((location.hostname === 'localhost' || location.hostname === '127.0.0.1')
        ? 'http://localhost:8080'
        : '');

const $ = id => document.getElementById(id);

function assetUrl(path) {
  if (!path) return '';

  try {
    const apiUrl = API_BASE ? new URL(API_BASE) : null;
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

let products = [],
    filtered = [],
    currentProduct = null,
    selectedPresentation = null,
    detailQty = 1;

let cart = JSON.parse(localStorage.getItem('myf_cart') || '[]');

const money = n => `S/ ${Number(n || 0).toFixed(2)}`;

const placeholder =
    `<div class="placeholder-bottle"><span>MYF</span></div>`;


function imgHtml(p) {

  if (!p.imagenUrl) {
    return placeholder;
  }

  return `
        <img
            src="${esc(assetUrl(p.imagenUrl))}"
            alt="${esc(p.nombre)}"
            onerror="this.onerror=null; this.style.display='none';"
        >
    `;
}


function esc(v = '') {
  return String(v).replace(
      /[&<>'"]/g,
      c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        "'": '&#39;',
        '"': '&quot;'
      }[c])
  );
}


async function get(path) {

  const r = await fetch(API_BASE + path);

  const text = await r.text();

  let d;

  try {
    d = text ? JSON.parse(text) : null;
  } catch {
    d = text;
  }

  if (!r.ok) {
    throw new Error(d?.error || `HTTP ${r.status}`);
  }

  return d;
}


async function post(path, body) {

  const r = await fetch(
      API_BASE + path,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
      }
  );

  const text = await r.text();

  let d;

  try {
    d = text ? JSON.parse(text) : null;
  } catch {
    d = text;
  }

  if (!r.ok) {
    throw new Error(d?.error || `HTTP ${r.status}`);
  }

  return d;
}


// ======================================================
// CATÁLOGO
// ======================================================

async function loadCatalog() {

  $('catalogProducts').innerHTML =
      '<div class="loading-card">Cargando catálogo...</div>';

  try {

    const ps = await get('/api/productos');

    products = ps || [];

    buildCatalogFilters();
    applyFilters();
    renderCart();

  } catch (e) {

    $('catalogProducts').innerHTML = `
            <div class="empty-state">
                <b>No se pudo cargar el catálogo.</b><br>
                ${esc(e.message)}<br>
                <small>
                    Verifica que Gateway (8080) y Product Service estén encendidos.
                </small>
            </div>
        `;

    $('productCount').textContent =
        'Catálogo no disponible';
  }
}


function buildCatalogFilters() {

  const b = $('filterBrand');
  const c = $('filterCategory');

  const oldB = b.value;
  const oldC = c.value;

  b.innerHTML =
      '<option value="">Todas las marcas</option>' +
      [...new Set(
          products
              .map(p => p.marca)
              .filter(Boolean)
      )]
          .sort()
          .map(x =>
              `<option>${esc(x)}</option>`
          )
          .join('');

  c.innerHTML =
      '<option value="">Todas las categorías</option>' +
      [...new Set(
          products
              .map(p => p.categoria)
              .filter(Boolean)
      )]
          .sort()
          .map(x =>
              `<option>${esc(x)}</option>`
          )
          .join('');

  b.value = oldB;
  c.value = oldC;
}


function availability(p, pr) {
  return {
    ok: true,
    qty: Number.POSITIVE_INFINITY,
    label: 'Stock confirmado al finalizar el pedido',
    level: 'ok'
  };
}

function productState() {
  return {
    label: 'Consultar stock al comprar',
    level: 'ok'
  };
}


function minPrice(p) {

  const a =
      (p.presentaciones || [])
          .map(
              x => Number(x.precio)
          );

  return a.length
      ? Math.min(...a)
      : Number(p.precio || 0);
}


function formatPresentations(p) {

  const a =
      (p.presentaciones || []);

  return a
      .map(
          x =>
              x.tipo === 'BOTELLA'
                  ? `${Number(x.volumenMl)} ml`
                  : `Decant ${Number(x.volumenMl)} ml`
      )
      .join(' · ');
}


function renderProducts() {

  $('productCount').textContent =
      `${filtered.length} ${
          filtered.length === 1
              ? 'producto'
              : 'productos'
      }`;

  if (!filtered.length) {

    $('catalogProducts').innerHTML =
        '<div class="empty-state">No encontramos productos con esos filtros.</div>';

    return;
  }

  $('catalogProducts').innerHTML =
      filtered.map(p => {

        const st =
            productState(p);

        return `

                <article class="product-card">

                    <span class="stock-pill ${st.level}">
                        ${st.label}
                    </span>

                    <div
                        class="product-media"
                        data-open-product="${p.id}"
                    >
                        ${imgHtml(p)}
                    </div>

                    <div class="product-body">

                        <div class="product-brand">
                            ${esc(p.marca || 'MYF Perfumes')}
                        </div>

                        <div
                            class="product-name"
                            title="${esc(p.nombre)}"
                        >
                            ${esc(p.nombre)}
                        </div>

                        <div class="product-meta">
                            ${esc(p.categoria || 'Perfumes')}
                            ·
                            ${esc(formatPresentations(p))}
                        </div>

                        <div class="product-price">
                            Desde ${money(minPrice(p))}
                        </div>

                        <button
                            class="product-action"
                            data-open-product="${p.id}"
                        >
                            Ver opciones
                        </button>

                    </div>

                </article>
            `;

      }).join('');

  document
      .querySelectorAll('[data-open-product]')
      .forEach(x => {

        x.onclick = () =>
            openProduct(
                Number(x.dataset.openProduct)
            );
      });
}


function applyFilters() {

  const q =
      $('searchProduct')
          .value
          .trim()
          .toLowerCase();

  const brand =
      $('filterBrand').value;

  const category =
      $('filterCategory').value;

  const type =
      $('filterType').value;

  filtered =
      products.filter(p => {

        const prs =
            p.presentaciones || [];

        return (

            (
                !q ||
                `${p.nombre} ${p.marca || ''}`
                    .toLowerCase()
                    .includes(q)
            )

            &&

            (
                !brand ||
                p.marca === brand
            )

            &&

            (
                !category ||
                p.categoria === category
            )

            &&

            (
                !type ||
                prs.some(
                    x => x.tipo === type
                )
            )
        );
      });

  renderProducts();
}


[
  'searchProduct',
  'filterBrand',
  'filterCategory',
  'filterType'
]
    .forEach(id => {

      $(id).addEventListener(
          id === 'searchProduct'
              ? 'input'
              : 'change',
          applyFilters
      );
    });


$('clearFilters').onclick = () => {

  $('searchProduct').value = '';
  $('filterBrand').value = '';
  $('filterCategory').value = '';
  $('filterType').value = '';

  applyFilters();
};


$('filterButton').onclick = () => {

  $('filtersPanel')
      .classList
      .toggle('open');
};


$('openSearch').onclick = () => {

  location.hash = 'catalogo';

  setTimeout(
      () =>
          $('searchProduct').focus(),
      300
  );
};


document
    .querySelector('[data-decant-filter]')
    .onclick = e => {

  e.preventDefault();

  $('filterType').value =
      'DECANT';

  applyFilters();

  location.hash =
      'catalogo';
};


// ======================================================
// DETALLE DEL PRODUCTO
// ======================================================

function openProduct(id) {

  currentProduct =
      products.find(
          x => Number(x.id) === id
      );

  if (!currentProduct) return;

  detailQty = 1;

  const active =
      (currentProduct.presentaciones || []);

  selectedPresentation =
      active.find(
          x =>
              availability(
                  currentProduct,
                  x
              ).ok
      )
      ||
      active[0]
      ||
      null;

  renderProductDetail();

  $('productOverlay')
      .classList
      .add('open');

  $('productPanel')
      .classList
      .add('open');

  $('productPanel')
      .setAttribute(
          'aria-hidden',
          'false'
      );
}


function closeProduct() {

  $('productOverlay')
      .classList
      .remove('open');

  $('productPanel')
      .classList
      .remove('open');

  $('productPanel')
      .setAttribute(
          'aria-hidden',
          'true'
      );
}


$('closeProduct').onclick =
    closeProduct;

$('productOverlay').onclick =
    closeProduct;


function renderProductDetail() {

  const p =
      currentProduct;

  const prs =
      (p.presentaciones || []);

  const av =
      selectedPresentation
          ? availability(
              p,
              selectedPresentation
          )
          : {
            ok: false,
            label: 'No disponible'
          };

  $('productDetail').innerHTML = `

        <div class="detail-media">
            ${imgHtml(p)}
        </div>

        <div class="detail-content">

            <div class="product-brand">
                ${esc(p.marca || 'MYF')}
            </div>

            <h2>
                ${esc(p.nombre)}
            </h2>

            <div class="product-meta">
                ${esc(p.categoria || 'Perfumes')}
                ·
                ${esc(formatPresentations(p))}
            </div>

            <p class="detail-description">
                ${
      esc(
          p.descripcion ||
          'Selecciona la presentación que prefieras y agrégala a tu carrito.'
      )
  }
            </p>

            <div class="presentation-list">

                ${
      prs.map(pr => {

        const a =
            availability(p, pr);

        const sel =
            selectedPresentation &&
            Number(
                selectedPresentation.id
            ) ===
            Number(pr.id);

        return `

                            <div
                                class="presentation-option ${
            sel
                ? 'selected'
                : ''
        } ${
            a.ok
                ? ''
                : 'disabled'
        }"
                                data-pr="${pr.id}"
                            >

                                <div>

                                    <b>
                                        ${
            pr.tipo === 'BOTELLA'
                ? 'Botella'
                : 'Decant'
        }
                                        ·
                                        ${Number(pr.volumenMl)} ml
                                    </b>

                                    <small>
                                        ${a.label}
                                    </small>

                                </div>

                                <strong>
                                    ${money(pr.precio)}
                                </strong>

                            </div>
                        `;

      }).join('')
  }

            </div>

            <div class="detail-stock">
                ${esc(av.label)}
            </div>

            <div class="detail-buy">

                <div class="qty-box">

                    <button id="detailMinus">
                        −
                    </button>

                    <b id="detailQty">
                        ${detailQty}
                    </b>

                    <button id="detailPlus">
                        +
                    </button>

                </div>

                <button
                    class="btn primary"
                    id="addDetailCart"
                    ${av.ok ? '' : 'disabled'}
                >
                    Agregar · ${
      selectedPresentation
          ? money(
              Number(
                  selectedPresentation.precio
              ) *
              detailQty
          )
          : ''
  }
                </button>

            </div>

        </div>
    `;


  document
      .querySelectorAll('[data-pr]')
      .forEach(x => {

        x.onclick = () => {

          const pr =
              prs.find(
                  y =>
                      Number(y.id) ===
                      Number(x.dataset.pr)
              );

          if (
              pr &&
              availability(p, pr).ok
          ) {

            selectedPresentation =
                pr;

            detailQty = 1;

            renderProductDetail();
          }
        };
      });


  $('detailMinus').onclick = () => {

    detailQty =
        Math.max(
            1,
            detailQty - 1
        );

    renderProductDetail();
  };


  $('detailPlus').onclick = () => {

    if (selectedPresentation) {

      const a =
          availability(
              p,
              selectedPresentation
          );

      detailQty =
          Math.min(
              a.qty,
              detailQty + 1
          );
    }

    renderProductDetail();
  };


  $('addDetailCart').onclick =
      () =>
          addToCart(
              p,
              selectedPresentation,
              detailQty
          );
}


// ======================================================
// CARRITO
// ======================================================

function addToCart(p, pr, qty) {

  if (!pr) return;

  const av =
      availability(p, pr);

  const existing =
      cart.find(
          x =>
              Number(x.presentacionId) ===
              Number(pr.id)
      );

  const newQty =
      (existing?.cantidad || 0) +
      qty;

  if (newQty > av.qty) {

    toast(
        `Solo hay disponibilidad para ${av.qty} unidad(es) de esta presentación.`
    );

    return;
  }

  if (existing) {

    existing.cantidad =
        newQty;

  } else {

    cart.push({

      productoId:
      p.id,

      presentacionId:
      pr.id,

      nombre:
      p.nombre,

      marca:
      p.marca,

      imagenUrl:
      p.imagenUrl,

      tipo:
      pr.tipo,

      volumenMl:
          Number(pr.volumenMl),

      precio:
          Number(pr.precio),

      cantidad:
      qty
    });
  }

  saveCart();
  renderCart();
  closeProduct();
  openCart();

  toast(
      'Producto agregado al carrito'
  );
}


function saveCart() {

  localStorage.setItem(
      'myf_cart',
      JSON.stringify(cart)
  );
}


function renderCart() {

  $('cartCount').textContent =
      cart.reduce(
          (a, x) =>
              a + x.cantidad,
          0
      );


  if (!cart.length) {

    $('cartItems').innerHTML =
        '<div class="empty-state" style="padding:40px 10px">Tu carrito está vacío.</div>';

  } else {

    $('cartItems').innerHTML =
        cart.map((x, i) => `

                <div class="cart-row">

                    <div class="cart-thumb">

                        ${
            x.imagenUrl
                ? `<img src="${esc(assetUrl(x.imagenUrl))}">`
                : 'MYF'
        }

                    </div>

                    <div>

                        <h4>
                            ${esc(x.nombre)}
                        </h4>

                        <small>
                            ${
            x.tipo === 'BOTELLA'
                ? 'Botella'
                : 'Decant'
        }
                            ${x.volumenMl} ml
                            ·
                            ${money(x.precio)}
                        </small>

                        <div class="qty">

                            <button data-minus="${i}">
                                −
                            </button>

                            <span>
                                ${x.cantidad}
                            </span>

                            <button data-plus="${i}">
                                +
                            </button>

                            <button
                                class="remove"
                                data-remove="${i}"
                            >
                                Eliminar
                            </button>

                        </div>

                    </div>

                    <b>
                        ${money(
            x.precio *
            x.cantidad
        )}
                    </b>

                </div>

            `).join('');
  }


  const total =
      cart.reduce(
          (a, x) =>
              a +
              x.precio *
              x.cantidad,
          0
      );


  $('cartSubtotal').textContent =
      money(total);

  $('cartTotal').textContent =
      money(total);

  $('checkoutTotal').textContent =
      money(total);


  document
      .querySelectorAll('[data-minus]')
      .forEach(b => {

        b.onclick = () =>
            changeQty(
                Number(b.dataset.minus),
                -1
            );
      });


  document
      .querySelectorAll('[data-plus]')
      .forEach(b => {

        b.onclick = () =>
            changeQty(
                Number(b.dataset.plus),
                1
            );
      });


  document
      .querySelectorAll('[data-remove]')
      .forEach(b => {

        b.onclick = () => {

          cart.splice(
              Number(b.dataset.remove),
              1
          );

          saveCart();
          renderCart();
        };
      });
}


function changeQty(i, d) {

  const x =
      cart[i];

  const p =
      products.find(
          p =>
              Number(p.id) ===
              Number(x.productoId)
      );

  const pr =
      p?.presentaciones?.find(
          q =>
              Number(q.id) ===
              Number(x.presentacionId)
      );

  if (!p || !pr) return;

  const max =
      availability(p, pr).qty;

  x.cantidad =
      Math.max(
          1,
          Math.min(
              max,
              x.cantidad + d
          )
      );

  saveCart();
  renderCart();
}


function openCart() {

  renderCart();

  $('cartOverlay')
      .classList
      .add('open');

  $('cartPanel')
      .classList
      .add('open');

  $('cartPanel')
      .setAttribute(
          'aria-hidden',
          'false'
      );
}


function closeCart() {

  $('cartOverlay')
      .classList
      .remove('open');

  $('cartPanel')
      .classList
      .remove('open');

  $('cartPanel')
      .setAttribute(
          'aria-hidden',
          'true'
      );
}


$('openCart').onclick =
    openCart;

$('closeCart').onclick =
    closeCart;

$('cartOverlay').onclick =
    closeCart;


// ======================================================
// CHECKOUT
// ======================================================

$('checkoutButton').onclick = () => {

  if (!cart.length) {

    toast(
        'Tu carrito está vacío'
    );

    return;
  }

  closeCart();

  $('checkoutModal')
      .classList
      .add('open');

  $('checkoutModal')
      .setAttribute(
          'aria-hidden',
          'false'
      );

  renderCart();

  updateDeliveryFields();
  updatePaymentInfo();
};


$('closeCheckout').onclick = () => {

  $('checkoutModal')
      .classList
      .remove('open');

  $('checkoutModal')
      .setAttribute(
          'aria-hidden',
          'true'
      );
};


// ======================================================
// DELIVERY / RECOJO
// ======================================================

function updateDeliveryFields() {

  const deliverySelect =
      $('customerDelivery');

  const addressBox =
      $('deliveryAddressBox');

  const addressInput =
      $('customerAddress');

  if (!deliverySelect || !addressBox || !addressInput) {
    return;
  }

  const tipoEntrega =
      deliverySelect.value;


  if (tipoEntrega === 'DELIVERY') {

    addressBox.style.display =
        'block';

    addressInput.required =
        true;

  } else {

    addressBox.style.display =
        'none';

    addressInput.required =
        false;

    addressInput.value =
        '';
  }
}


const deliverySelect = $('customerDelivery');
if (deliverySelect) deliverySelect.onchange = () => {

  updateDeliveryFields();
  updatePaymentInfo();
};


// ======================================================
// MÉTODOS DE PAGO
// ======================================================

function updatePaymentInfo() {

    const paymentSelect = $('customerPayment');
    const deliverySelect = $('customerDelivery');
    const info = $('paymentInfo');

    const proofBox = $('paymentProofBox');
    const proofInput = $('paymentProof');
    const previewBox = $('paymentProofPreviewBox');
    const preview = $('paymentProofPreview');

    if (!paymentSelect || !deliverySelect || !info || !proofBox ||
        !proofInput || !previewBox || !preview) {
        return;
    }

    const metodo = paymentSelect.value;
    const tipoEntrega = deliverySelect.value;

    const total = cart.reduce(
        (suma, x) => suma + (x.precio * x.cantidad),
        0
    );


    // ======================================================
    // MOSTRAR / OCULTAR COMPROBANTE
    // ======================================================

    if (
        metodo === 'YAPE' ||
        metodo === 'PLIN' ||
        metodo === 'TRANSFERENCIA'
    ) {

        proofBox.style.display = 'block';

    } else {

        proofBox.style.display = 'none';

        proofInput.value = '';

        previewBox.style.display = 'none';

        preview.removeAttribute('src');
    }


    // ======================================================
    // YAPE
    // ======================================================

    if (metodo === 'YAPE') {

        info.innerHTML = `
      <div class="payment-message">

        <strong style="font-size:18px;">
          Pago con Yape
        </strong>

        <p>
          Envía el pago al siguiente número:
        </p>

        <div style="
          font-size:22px;
          font-weight:800;
          margin:10px 0;
        ">
          924 687 821
        </div>

        <div>
          Titular:
          <strong>Martin Cadiz</strong>
        </div>

        <div style="margin-top:12px;">
          Total a pagar:
          <strong>${money(total)}</strong>
        </div>

        <button
          type="button"
          class="btn"
          style="margin-top:12px;"
          onclick="copyPaymentData(
            '924687821',
            'Número de Yape copiado'
          )"
        >
          Copiar número
        </button>

        <p style="margin-top:14px;">
          <strong>
            Pago pendiente de verificación.
          </strong>
        </p>

        <small>
          Realiza el pago y adjunta tu comprobante debajo.
        </small>

      </div>
    `;


        // ======================================================
        // PLIN
        // ======================================================

    } else if (metodo === 'PLIN') {

        info.innerHTML = `
      <div class="payment-message">

        <strong style="font-size:18px;">
          Pago con Plin
        </strong>

        <p>
          Envía el pago al siguiente número:
        </p>

        <div style="
          font-size:22px;
          font-weight:800;
          margin:10px 0;
        ">
          924 687 821
        </div>

        <div>
          Titular:
          <strong>Martin Cadiz</strong>
        </div>

        <div style="margin-top:12px;">
          Total a pagar:
          <strong>${money(total)}</strong>
        </div>

        <button
          type="button"
          class="btn"
          style="margin-top:12px;"
          onclick="copyPaymentData(
            '924687821',
            'Número de Plin copiado'
          )"
        >
          Copiar número
        </button>

        <p style="margin-top:14px;">
          <strong>
            Pago pendiente de verificación.
          </strong>
        </p>

        <small>
          Realiza el pago y adjunta tu comprobante debajo.
        </small>

      </div>
    `;


        // ======================================================
        // TRANSFERENCIA BCP
        // ======================================================

    } else if (metodo === 'TRANSFERENCIA') {

        info.innerHTML = `
      <div class="payment-message">

        <strong style="font-size:18px;">
          Transferencia bancaria
        </strong>

        <p style="margin-bottom:6px;">
          Banco:
          <strong>BCP</strong>
        </p>

        <div>
          Titular:
          <strong>Martin Cadiz</strong>
        </div>

        <p style="margin-bottom:5px;">
          CCI:
        </p>

        <div style="
          font-size:18px;
          font-weight:800;
          word-break:break-all;
          margin-bottom:10px;
        ">
          00219110683805902857
        </div>

        <div style="margin-top:12px;">
          Total a pagar:
          <strong>${money(total)}</strong>
        </div>

        <button
          type="button"
          class="btn"
          style="margin-top:12px;"
          onclick="copyPaymentData(
            '00219110683805902857',
            'CCI copiado'
          )"
        >
          Copiar CCI
        </button>

        <p style="margin-top:14px;">
          <strong>
            Pago pendiente de verificación.
          </strong>
        </p>

        <small>
          Realiza la transferencia y adjunta tu comprobante debajo.
        </small>

      </div>
    `;


        // ======================================================
        // EFECTIVO
        // ======================================================

    } else {

        info.innerHTML = `
      <div class="payment-message">

        <strong style="font-size:18px;">
          Pago en efectivo
        </strong>

        <p>
          ${
            tipoEntrega === 'DELIVERY'
                ? 'Paga en efectivo al momento de recibir tu pedido.'
                : 'Paga en efectivo al momento de recoger tu pedido.'
        }
        </p>

        <div style="margin-top:10px;">
          Total a pagar:
          <strong>${money(total)}</strong>
        </div>

        <p style="margin-top:14px;">
          <small>
            El pago permanecerá pendiente hasta ser confirmado.
          </small>
        </p>

      </div>
    `;
    }

    info.style.display = 'block';
}


// ======================================================
// VISTA PREVIA DEL COMPROBANTE
// ======================================================

const paymentProofInput = $('paymentProof');
if (paymentProofInput) paymentProofInput.onchange = function () {

    const archivo = this.files[0];

    const previewBox =
        $('paymentProofPreviewBox');

    const preview =
        $('paymentProofPreview');

    if (!archivo) {

        previewBox.style.display =
            'none';

        preview.removeAttribute('src');

        return;
    }


    // Solo imágenes
    if (!archivo.type.startsWith('image/')) {

        toast(
            'El comprobante debe ser una imagen'
        );

        this.value = '';

        previewBox.style.display =
            'none';

        return;
    }


    // Máximo 5 MB
    if (archivo.size > 5 * 1024 * 1024) {

        toast(
            'La imagen no puede superar los 5 MB'
        );

        this.value = '';

        previewBox.style.display =
            'none';

        return;
    }


    preview.src =
        URL.createObjectURL(
            archivo
        );

    previewBox.style.display =
        'block';
};


// ======================================================
// COPIAR DATOS DE PAGO
// ======================================================

async function copyPaymentData(
    texto,
    mensaje
) {

    try {

        await navigator.clipboard.writeText(
            texto
        );

        toast(mensaje);

    } catch (e) {

        toast(
            'No se pudo copiar automáticamente'
        );
    }
}


const paymentSelect = $('customerPayment');
if (paymentSelect) {
    paymentSelect.onchange = updatePaymentInfo;
}


// ======================================================
// CONFIRMAR PEDIDO
// ======================================================

const checkoutForm = $('checkoutForm');
if (checkoutForm) checkoutForm.onsubmit = async e => {

    e.preventDefault();

    if (!cart.length) {
        return;
    }

    const deliverySelect = $('customerDelivery');
    const paymentSelect = $('customerPayment');
    if (!deliverySelect || !paymentSelect) {
        toast('El formulario de compra está desactualizado. Recarga la página para continuar.');
        return;
    }

    const tipoEntrega =
        deliverySelect.value;

    const metodoPago =
        paymentSelect.value;

    const proofInput =
        document.getElementById('paymentProof');

    const comprobante =
        proofInput && proofInput.files.length > 0
            ? proofInput.files[0]
            : null;

    const requiereComprobante =
        metodoPago === 'YAPE' ||
        metodoPago === 'PLIN' ||
        metodoPago === 'TRANSFERENCIA';

    if (requiereComprobante && !comprobante) {

        $('checkoutMsg').textContent =
            'Debes adjuntar el comprobante de pago antes de confirmar el pedido.';

        return;
    }


    // ======================================================
    // VALIDAR DIRECCIÓN SI ES DELIVERY
    // ======================================================

    if (
        tipoEntrega === 'DELIVERY' &&
        !$('customerAddress').value.trim()
    ) {

        $('checkoutMsg').textContent =
            'Ingresa la dirección para realizar el delivery.';

        return;
    }


    // ======================================================
    // BOTÓN PROCESANDO
    // ======================================================

    const btn =
        $('confirmOrder');

    btn.disabled = true;

    btn.textContent =
        'Procesando...';

    $('checkoutMsg').textContent = '';


    try {

        // ======================================================
        // DATOS DEL PEDIDO
        // ======================================================

        const body = {

            descuento: 0,

            metodoPago:
            metodoPago,

            tipoEntrega:
            tipoEntrega,

            observacion:
            $('customerNotes').value,

            canal:
                'WEB',

            cliente: {

                nombre:
                    $('customerName')
                        .value
                        .trim(),

                telefono:
                    $('customerPhone')
                        .value
                        .trim(),

                email:
                    $('customerEmail')
                        .value
                        .trim() || null,

                direccion:
                    tipoEntrega === 'DELIVERY'
                        ? $('customerAddress')
                            .value
                            .trim()
                        : null
            },

            items:
                cart.map(x => ({

                    productoId:
                    x.productoId,

                    presentacionId:
                    x.presentacionId,

                    cantidad:
                    x.cantidad

                }))
        };


        // ======================================================
        // 1. CREAR PEDIDO
        // ======================================================

        const sale =
            await post(
                '/api/ventas/checkout',
                body
            );


        // ======================================================
        // 2. SUBIR COMPROBANTE SI EXISTE
        // ======================================================

        let comprobanteSubido = false;
        let errorComprobante = false;

        if (
            comprobante &&
            (
                metodoPago === 'YAPE' ||
                metodoPago === 'PLIN' ||
                metodoPago === 'TRANSFERENCIA'
            )
        ) {

            const formData =
                new FormData();

            formData.append(
                'comprobante',
                comprobante
            );

            try {

                const respuesta =
                    await fetch(
                        API_BASE +
                        `/api/ventas/${sale.id}/comprobante`,
                        {
                            method: 'POST',
                            body: formData
                        }
                    );


                let datos = {};

                try {
                    datos =
                        await respuesta.json();
                } catch (e) {
                    datos = {};
                }


                if (!respuesta.ok) {

                    throw new Error(
                        datos.error ||
                        'No se pudo subir el comprobante'
                    );
                }

                comprobanteSubido = true;

            } catch (error) {

                console.error(
                    'Error al subir comprobante:',
                    error
                );

                errorComprobante = true;
            }
        }


        // ======================================================
        // 3. LIMPIAR CARRITO
        // ======================================================

        cart = [];

        saveCart();

        renderCart();


        // ======================================================
        // 4. LIMPIAR FORMULARIO
        // ======================================================

        $('checkoutForm').reset();

        $('paymentProofPreviewBox')
            .style.display = 'none';

        $('paymentProofPreview')
            .removeAttribute('src');

        updateDeliveryFields();

        updatePaymentInfo();


        // ======================================================
        // 5. CERRAR CHECKOUT
        // ======================================================

        $('checkoutModal')
            .classList
            .remove('open');

        $('checkoutModal')
            .setAttribute(
                'aria-hidden',
                'true'
            );


        // ======================================================
        // 6. MENSAJE FINAL
        // ======================================================

        if (errorComprobante) {

            toast(
                `Pedido ${sale.numeroVenta} registrado, pero no se pudo subir el comprobante`
            );

        } else if (comprobanteSubido) {

            toast(
                `Pedido ${sale.numeroVenta} registrado con comprobante`
            );

        } else {

            toast(
                `Pedido ${sale.numeroVenta} registrado correctamente`
            );
        }


        // ======================================================
        // 7. ACTUALIZAR CATÁLOGO
        // ======================================================

        await loadCatalog();


    } catch (err) {

        $('checkoutMsg').textContent =
            err.message;

    } finally {

        btn.disabled = false;

        btn.textContent =
            'Confirmar pedido';
    }
};

// ======================================================
// MENSAJES
// ======================================================

function toast(msg) {

    const t = $('toast');

    if (!t) {
        console.log(msg);
        return;
    }

    t.textContent = msg;

    t.classList.add('show');

    clearTimeout(window.__toast);

    window.__toast = setTimeout(
        () => t.classList.remove('show'),
        2800
    );
}

// ======================================================
// INICIAR
// ======================================================

updateDeliveryFields();
updatePaymentInfo();

loadCatalog();
renderCart();