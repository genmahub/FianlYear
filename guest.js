let activeBooking = null;
let daysSinceRegistration = 0;

window.addEventListener('DOMContentLoaded', () => {
  const savedEmail = sessionStorage.getItem('hotel_email');
  const savedRef   = sessionStorage.getItem('hotel_ref');
  if (savedEmail) document.getElementById('loginEmail').value = savedEmail;
  if (savedRef)   document.getElementById('loginRef').value   = savedRef;
});

document.getElementById('guestLoginForm').addEventListener('submit', async e => {
  e.preventDefault();
  const formData = new FormData();
  formData.append('action', 'guest_login');
  formData.append('email', document.getElementById('loginEmail').value.trim());
  formData.append('reference', document.getElementById('loginRef').value.trim().toUpperCase());

  try {
    const res = await fetch('hotel_api.php', { method: 'POST', body: formData });
    const data = await res.json();

    if (data.status === 'success') {
      activeBooking = data.booking;
      daysSinceRegistration = data.days_since_booking;
      renderDashboard();
    } else {
      alert(data.message || 'Could not find reservation.');
    }
  } catch (err) {
    alert('Connection error: Ensure Apache & MySQL are running in XAMPP.');
  }
});

// Toggle Card vs Bank Transfer fields
const paySelect  = document.getElementById('paymentMethodSelect');
const cardFields = document.getElementById('cardFields');
const eftFields  = document.getElementById('eftFields');

paySelect.addEventListener('change', () => {
  cardFields.classList.toggle('hidden', paySelect.value !== 'Credit / Debit Card');
  eftFields.classList.toggle('hidden', paySelect.value !== 'Bank Transfer');
});

// Auto-format Card Number (0000 1234 1234 0000) & MM/YY
document.getElementById('cardNumber').addEventListener('input', e => {
  let digits = e.target.value.replace(/\D/g, '').slice(0, 16);
  e.target.value = digits.replace(/(\d{4})(?=\d)/g, '$1 ');
});

document.getElementById('cardExpiry').addEventListener('input', e => {
  let digits = e.target.value.replace(/\D/g, '').slice(0, 4);
  if (digits.length >= 3) digits = digits.slice(0, 2) + '/' + digits.slice(2);
  e.target.value = digits;
});

// Auto-fill South African universal branch code when bank is selected
document.getElementById('bankName').addEventListener('change', e => {
  const selectedOpt = e.target.options[e.target.selectedIndex];
  const branch = selectedOpt.getAttribute('data-branch');
  if (branch) document.getElementById('bankBranch').value = branch;
});

// Save Payment Choice to Database
document.getElementById('savePaymentBtn').addEventListener('click', async () => {
  if (!activeBooking || activeBooking.status === 'Cancelled') return;

  let chosenMethod = paySelect.value;

  if (chosenMethod === 'Credit / Debit Card') {
    const cardNum = document.getElementById('cardNumber').value.replace(/\s+/g, '');
    const expiry  = document.getElementById('cardExpiry').value.trim();
    const cvv     = document.getElementById('cardCvv').value.trim();

    if (cardNum.length < 12 || expiry.length < 5 || cvv.length < 3) {
      alert('Please enter a valid Card number, Expiration date (MM/YY), and CVV.');
      return;
    }
    chosenMethod = `Card (•••• ${cardNum.slice(-4)} | Exp: ${expiry})`;
  } else if (chosenMethod === 'Bank Transfer') {
    const holder  = document.getElementById('bankHolder').value.trim();
    const bank    = document.getElementById('bankName').value;
    const accNum  = document.getElementById('bankAccount').value.replace(/\s+/g, '');
    const branch  = document.getElementById('bankBranch').value.trim();

    if (!holder || !bank || accNum.length < 6 || branch.length < 4) {
      alert('Please enter the Account holder name, Bank name, Account number, and Branch code.');
      return;
    }
    chosenMethod = `Bank Transfer (${bank} • Holder: ${holder} • Acc ••••${accNum.slice(-4)} • Branch ${branch})`;
  }

  const formData = new FormData();
  formData.append('action', 'update_payment');
  formData.append('reference', activeBooking.booking_id);
  formData.append('email', activeBooking.guest_email);
  formData.append('payment_method', chosenMethod);

  const res = await fetch('hotel_api.php', { method: 'POST', body: formData });
  const data = await res.json();

  if (data.status === 'success') {
    activeBooking.payment_method = data.payment_method;
    renderDashboard();
    alert(`Payment confirmed via: ${data.payment_method}`);
  } else {
    alert(data.message || 'Could not process payment.');
  }
});

function renderDashboard() {
  document.getElementById('loginCard').classList.add('hidden');
  document.getElementById('guestDashboard').classList.remove('hidden');

  const total   = parseFloat(activeBooking.total_amount);
  const extras  = parseFloat(activeBooking.extras_total);
  const roomSub = total - extras;

  document.getElementById('dashGuestName').textContent   = `Welcome, ${activeBooking.guest_name}`;
  document.getElementById('dashRef').textContent         = activeBooking.booking_id;
  document.getElementById('dashRoom').textContent        = activeBooking.room_name;
  document.getElementById('dashRate').textContent        = `R ${parseFloat(activeBooking.room_rate).toFixed(2)} / night`;
  document.getElementById('dashDates').textContent       = `${activeBooking.check_in} to ${activeBooking.check_out}`;
  document.getElementById('dashGuests').textContent      = `${activeBooking.guests} Guest(s)`;
  document.getElementById('dashPayment').textContent     = activeBooking.payment_method || 'Pay at Hotel';
  document.getElementById('dashRoomSub').textContent     = `R ${roomSub.toFixed(2)}`;
  document.getElementById('dashExtrasTotal').textContent = `R ${extras.toFixed(2)}`;
  document.getElementById('dashGrandTotal').textContent  = `R ${total.toFixed(2)}`;

  document.getElementById('dashExtrasList').innerHTML = activeBooking.extras_summary
    ? '• ' + activeBooking.extras_summary.replace(/; /g, '<br>• ')
    : 'No food or activities booked yet.';

  const estEl      = document.getElementById('cancellationEstimate');
  const cancelBtn  = document.getElementById('cancelBookingBtn');
  const extrasCard = document.getElementById('extrasBookingCard');
  const paySection = document.getElementById('paymentSection');

  if (activeBooking.status === 'Cancelled') {
    document.getElementById('statusBadge').textContent = `STATUS: CANCELLED (Fee Charged: R ${parseFloat(activeBooking.cancellation_fee).toFixed(2)})`;
    cancelBtn.classList.add('hidden');
    paySection.classList.add('hidden');
    extrasCard.style.opacity = '0.5';
    extrasCard.style.pointerEvents = 'none';
    estEl.textContent = 'This reservation has already been cancelled.';
  } else {
    document.getElementById('statusBadge').textContent = 'STATUS: ACTIVE RESERVATION';
    if (daysSinceRegistration <= 5) {
      estEl.textContent = `Registered ${daysSinceRegistration} day(s) ago — Eligible for FREE Cancellation (R 0.00 fee).`;
    } else {
      const fee = (total * 0.25).toFixed(2);
      estEl.textContent = `Registered ${daysSinceRegistration} day(s) ago — Cancellation fee applies: R ${fee} (25%).`;
    }
  }
}

async function bookExtra(itemName, itemPrice) {
  if (!activeBooking || activeBooking.status === 'Cancelled') return;

  const formData = new FormData();
  formData.append('action', 'add_guest_extra');
  formData.append('reference', activeBooking.booking_id);
  formData.append('email', activeBooking.guest_email);
  formData.append('item_name', itemName);
  formData.append('item_price', itemPrice);
  formData.append('qty', 1);

  const res = await fetch('hotel_api.php', { method: 'POST', body: formData });
  const data = await res.json();

  if (data.status === 'success') {
    activeBooking.extras_total   = data.extras_total;
    activeBooking.total_amount   = data.total_amount;
    activeBooking.extras_summary = data.extras_summary;
    renderDashboard();
    alert(`Added "${itemName}" to your reservation!`);
  } else {
    alert(data.message);
  }
}

document.getElementById('cancelBookingBtn').addEventListener('click', async () => {
  if (!activeBooking) return;
  const fee = daysSinceRegistration <= 5 ? 0 : (parseFloat(activeBooking.total_amount) * 0.25).toFixed(2);
  const confirmMsg = fee == 0
    ? 'Confirm FREE cancellation of your reservation?'
    : `More than 5 days have passed since registration. A cancellation fee of R ${fee} will apply. Proceed?`;

  if (!confirm(confirmMsg)) return;

  const formData = new FormData();
  formData.append('action', 'cancel_booking');
  formData.append('reference', activeBooking.booking_id);
  formData.append('email', activeBooking.guest_email);

  const res = await fetch('hotel_api.php', { method: 'POST', body: formData });
  const data = await res.json();

  if (data.status === 'success') {
    activeBooking.status = 'Cancelled';
    activeBooking.cancellation_fee = data.cancellation_fee;
    renderDashboard();
    alert(data.message);
  } else {
    alert(data.message);
  }
});