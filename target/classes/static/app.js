const state = { customers: [], products: [], orders: [], cart: [] };
const $ = (id) => document.getElementById(id);
const money = (n) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(n || 0));
const dateTime = (value) => value ? new Date(value).toLocaleString() : '';

async function api(path, options = {}) {
  const response = await fetch(path, {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.error || `Request failed: ${response.status}`);
  }
  if (response.status === 204) return null;
  return response.json();
}

function flash(message, isError = false) {
  const el = $('message');
  el.textContent = message;
  el.className = `message${isError ? ' error' : ''}`;
  clearTimeout(flash.timer);
  flash.timer = setTimeout(() => el.className = 'message hidden', 3500);
}

async function loadAll() {
  try {
    const [dashboard, customers, products, orders] = await Promise.all([
      api('/api/dashboard'), api('/api/customers'), api('/api/products'), api('/api/orders')
    ]);
    state.customers = customers;
    state.products = products;
    state.orders = orders.sort((a,b) => b.id - a.id);
    renderDashboard(dashboard);
    renderCustomers();
    renderProducts();
    renderOrders();
    renderOrderOptions();
    renderCart();
  } catch (err) { flash(err.message, true); }
}

function renderDashboard(d) {
  $('customerCount').textContent = d.customers;
  $('productCount').textContent = d.products;
  $('orderCount').textContent = d.orders;
  $('revenue').textContent = money(d.revenue);
}

function renderCustomers() {
  $('customersBody').innerHTML = state.customers.map(c => `
    <tr><td>${escapeHtml(c.name)}</td><td>${escapeHtml(c.email)}</td><td>${escapeHtml(c.phone || '—')}</td>
    <td><button class="danger small" onclick="deleteCustomer(${c.id})">Delete</button></td></tr>`).join('') ||
    '<tr><td colspan="4" class="muted">No customers yet.</td></tr>';
}

function renderProducts() {
  $('productsBody').innerHTML = state.products.map(p => `
    <tr><td>${escapeHtml(p.name)}</td><td>${escapeHtml(p.sku)}</td><td>${money(p.price)}</td><td>${p.stockQuantity}</td>
    <td><button class="danger small" onclick="deleteProduct(${p.id})">Delete</button></td></tr>`).join('') ||
    '<tr><td colspan="5" class="muted">No products yet.</td></tr>';
}

function renderOrders() {
    const statuses = ['NEW', 'PROCESSING', 'SHIPPED', 'COMPLETED', 'CANCELLED'];

    $('ordersBody').innerHTML = state.orders.map(o => {
        const items = o.items
            .map(i => `${i.quantity}× ${escapeHtml(i.product.name)}`)
            .join(', ');

        const options = statuses.map(s =>
            `<option value="${s}" ${s === o.status ? 'selected' : ''}>${s}</option>`
        ).join('');

        return `
            <tr>
                <td>#${o.id}</td>
                <td>${escapeHtml(o.customer.name)}</td>
                <td>${items}</td>
                <td>${money(o.total)}</td>

                <td>
                    <select id="status-${o.id}">
                        ${options}
                    </select>
                </td>

                <td>${dateTime(o.createdAt)}</td>

                <td>
                    <button
                        class="small"
                        onclick="updateOrderStatus(${o.id})">
                        Update
                    </button>

                    <button
                        class="danger small"
                        onclick="deleteOrder(${o.id})">
                        Delete
                    </button>
                </td>
            </tr>
        `;
    }).join('') || 'No orders yet.';
}

function renderOrderOptions() {
  $('orderCustomer').innerHTML = state.customers.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join('');
  $('orderProduct').innerHTML = state.products
    .filter(p => p.stockQuantity > 0)
    .map(p => `<option value="${p.id}">${escapeHtml(p.name)} — ${money(p.price)} (${p.stockQuantity} in stock)</option>`).join('');
}

function renderCart() {
  const el = $('cart');
  if (!state.cart.length) {
    el.className = 'cart empty';
    el.textContent = 'No items added.';
    $('cartTotal').textContent = '$0.00';
    return;
  }
  el.className = 'cart';
  el.innerHTML = state.cart.map((item, index) => {
    const product = state.products.find(p => p.id === item.productId);
    return `<div class="cart-row"><span>${item.quantity}× ${escapeHtml(product.name)}</span><span>${money(product.price * item.quantity)} <button class="danger small" onclick="removeCartItem(${index})">Remove</button></span></div>`;
  }).join('');
  const total = state.cart.reduce((sum, item) => {
    const product = state.products.find(p => p.id === item.productId);
    return sum + Number(product.price) * item.quantity;
  }, 0);
  $('cartTotal').textContent = money(total);
}

$('customerForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    await api('/api/customers', { method: 'POST', body: JSON.stringify({
      name: $('customerName').value.trim(), email: $('customerEmail').value.trim(), phone: $('customerPhone').value.trim()
    })});
    event.target.reset(); flash('Customer added.'); await loadAll();
  } catch (err) { flash(err.message, true); }
});

$('productForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    await api('/api/products', { method: 'POST', body: JSON.stringify({
      name: $('productName').value.trim(), sku: $('productSku').value.trim(),
      price: Number($('productPrice').value), stockQuantity: Number($('productStock').value)
    })});
    event.target.reset(); flash('Product added.'); await loadAll();
  } catch (err) { flash(err.message, true); }
});

$('addToCartBtn').addEventListener('click', () => {
  const productId = Number($('orderProduct').value);
  const quantity = Number($('orderQuantity').value);
  const product = state.products.find(p => p.id === productId);
  if (!product || quantity < 1) return flash('Choose a product and valid quantity.', true);
  const existing = state.cart.find(i => i.productId === productId);
  const requested = quantity + (existing?.quantity || 0);
  if (requested > product.stockQuantity) return flash(`Only ${product.stockQuantity} ${product.name} available.`, true);
  if (existing) existing.quantity = requested; else state.cart.push({ productId, quantity });
  $('orderQuantity').value = 1;
  renderCart();
});

$('placeOrderBtn').addEventListener('click', async () => {
  const customerId = Number($('orderCustomer').value);
  if (!customerId) return flash('Add or choose a customer first.', true);
  if (!state.cart.length) return flash('Add at least one item.', true);
  try {
    await api('/api/orders', { method: 'POST', body: JSON.stringify({ customerId, items: state.cart }) });
    state.cart = []; flash('Order placed.'); await loadAll();
  } catch (err) { flash(err.message, true); }
});

$('refreshBtn').addEventListener('click', loadAll);
window.removeCartItem = (index) => { state.cart.splice(index, 1); renderCart(); };
window.updateOrderStatus = async (id) => {
    const status = $(`status-${id}`).value;

    try {
        await api(`/api/orders/${id}/status`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });

        flash(`Order #${id} updated to ${status}.`);
        await loadAll();

    } catch (err) {
        flash(err.message, true);
        await loadAll();
    }
};
window.deleteOrder = async (id) => {
  if (!confirm(`Delete order #${id}? Inventory will be restored when appropriate.`)) return;
  try { await api(`/api/orders/${id}`, { method:'DELETE' }); flash('Order deleted.'); await loadAll(); } catch (err) { flash(err.message, true); }
};
window.deleteCustomer = async (id) => {
  if (!confirm('Delete this customer?')) return;
  try { await api(`/api/customers/${id}`, { method:'DELETE' }); flash('Customer deleted.'); await loadAll(); } catch (err) { flash(err.message, true); }
};
window.deleteProduct = async (id) => {
  if (!confirm('Delete this product?')) return;
  try { await api(`/api/products/${id}`, { method:'DELETE' }); flash('Product deleted.'); await loadAll(); } catch (err) { flash(err.message, true); }
};

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));
}

loadAll();
