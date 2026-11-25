// ============ NAVBAR ============
const navbar = document.getElementById('navbar');
const hero   = document.getElementById('hero');

function updateNavbar() {
  const heroBottom = hero ? hero.getBoundingClientRect().bottom : 0;
  if (heroBottom > 80) {
    navbar.classList.add('at-hero');
    navbar.classList.remove('scrolled');
  } else {
    navbar.classList.remove('at-hero');
    navbar.classList.add('scrolled');
  }
}
updateNavbar();
window.addEventListener('scroll', updateNavbar, { passive: true });

// ============ HAMBURGER ============
const hamburger = document.getElementById('hamburger');
const navLinks  = document.getElementById('navLinks');

if (hamburger && navLinks) {
  hamburger.addEventListener('click', () => {
    navLinks.classList.toggle('open');
  });

  document.querySelectorAll('.nav-links a, .nav-cta').forEach(link => {
    link.addEventListener('click', () => navLinks.classList.remove('open'));
  });
}

// ============ HERO SLIDESHOW ============
const slides = document.querySelectorAll('.hero-slide');
const dots   = document.querySelectorAll('.dot');
let current  = 0;
let timer;

function goToSlide(n) {
  if (!slides.length) return;
  slides[current].classList.remove('active');
  if (dots[current]) dots[current].classList.remove('active');
  current = (n + slides.length) % slides.length;
  slides[current].classList.add('active');
  if (dots[current]) dots[current].classList.add('active');
}

function nextSlide() {
  goToSlide(current + 1);
}

function startTimer() {
  if (slides.length) timer = setInterval(nextSlide, 5000);
}
startTimer();

dots.forEach(dot => {
  dot.addEventListener('click', () => {
    clearInterval(timer);
    goToSlide(parseInt(dot.dataset.slide));
    startTimer();
  });
});

// ============ SCROLL REVEAL ============
const revealEls = document.querySelectorAll(
  '.room-card, .offer-card, .amenity-card, .kids-card, .about-image-wrap, .about-text, .stat, .contact-info, .contact-form-wrap'
);
revealEls.forEach(el => el.classList.add('reveal'));

const revealObserver = new IntersectionObserver((entries) => {
  entries.forEach((entry, i) => {
    if (entry.isIntersecting) {
      setTimeout(() => entry.target.classList.add('visible'), i * 60);
      revealObserver.unobserve(entry.target);
    }
  });
}, { threshold: 0.1 });

revealEls.forEach(el => revealObserver.observe(el));

// ============ HELPER: READS FORM INPUTS EVEN WITHOUT NAME ATTRIBUTES ============
function getFieldValue(form, names, fallbackIndex) {
  for (const n of names) {
    const el = form.querySelector(`[name="${n}"]`);
    if (el && el.value.trim() !== '') return el.value.trim();
  }
  const fields = form.querySelectorAll('input:not([type="hidden"]), select, textarea');
  return fields[fallbackIndex] ? fields[fallbackIndex].value.trim() : '';
}

// ============ 1. BOOKING FORM -> SENDS TO hotel_api.php ============
const bookingForm = document.getElementById('bookingForm');
const formSuccess = document.getElementById('formSuccess');

if (bookingForm) {
  bookingForm.addEventListener('submit', async e => {
    e.preventDefault();

    const nameVal     = getFieldValue(bookingForm, ['name', 'guest_name'], 0);
    const emailVal    = getFieldValue(bookingForm, ['email', 'guest_email'], 1);
    const checkInVal  = getFieldValue(bookingForm, ['check_in', 'checkin'], 2);
    const checkOutVal = getFieldValue(bookingForm, ['check_out', 'checkout'], 3);
    const roomSelect  = bookingForm.querySelectorAll('select')[0];
    const roomVal     = roomSelect ? (roomSelect.value || roomSelect.options[roomSelect.selectedIndex].text.split('—')[0].trim()) : '';
    const guestsVal   = getFieldValue(bookingForm, ['guests', 'num_guests'], 5) || '1';
    const reqVal      = getFieldValue(bookingForm, ['requests', 'special_requests'], 6);

    const formData = new FormData();
    formData.append('action',    'book_room');
    formData.append('name',      nameVal);
    formData.append('email',     emailVal);
    formData.append('check_in',  checkInVal);
    formData.append('check_out', checkOutVal);
    formData.append('room',      roomVal);
    formData.append('guests',    guestsVal);
    formData.append('requests',  reqVal);

    try {
      const response = await fetch('hotel_api.php', { method: 'POST', body: formData });
      const result   = await response.json();

      if (result.status === 'success') {
        bookingForm.classList.add('hidden');
        formSuccess.classList.remove('hidden');

        sessionStorage.setItem('hotel_ref', result.reference);
        sessionStorage.setItem('hotel_email', result.email);

        // Displays Reference # and Guest Portal button even if index.html has old text
        formSuccess.innerHTML = `
          <span>✦</span>
          <h3>Your reservation has been confirmed!</h3>
          <p style="margin-bottom: 1.5rem;">
            <strong>Reference Number:</strong> <span style="color:var(--gold); font-size:1.4rem;">${result.reference}</span><br>
            <strong>Registered Email:</strong> ${result.email}<br>
            <strong>Total Estimated Cost:</strong> R ${result.total}<br><br>
            Use your email and reference number above to log into the Guest Portal.
          </p>
          <a href="guest.html" class="btn btn-primary">Open Guest Portal</a>
        `;
      } else {
        alert('Booking error: ' + (result.message || 'Could not save booking.'));
      }
    } catch (err) {
      alert('Cannot reach hotel_api.php. Make sure Apache & MySQL are running in XAMPP.');
    }
  });
}

// ============ 2. CONTACT FORM -> SENDS TO hotel_api.php ============
const contactForm    = document.getElementById('contactForm');
const contactSuccess = document.getElementById('contactSuccess');

if (contactForm) {
  contactForm.addEventListener('submit', async e => {
    e.preventDefault();
    const formData = new FormData();
    formData.append('action',  'send_message');
    formData.append('name',    getFieldValue(contactForm, ['name'], 0));
    formData.append('email',   getFieldValue(contactForm, ['email'], 1));
    formData.append('message', getFieldValue(contactForm, ['message'], 2));

    try {
      const response = await fetch('hotel_api.php', { method: 'POST', body: formData });
      const result   = await response.json();
      if (result.status === 'success') {
        if (contactSuccess) contactSuccess.classList.remove('hidden');
        contactForm.reset();
      } else {
        alert('Message error: ' + (result.message || 'Could not send message.'));
      }
    } catch (err) {
      alert('Cannot reach hotel_api.php. Make sure Apache is running in XAMPP.');
    }
  });
}

// ============ DINING TABS ============
const diningTabs  = document.querySelectorAll('.dtab');
const diningCards = document.querySelectorAll('.dining-card');
diningTabs.forEach(tab => {
  tab.addEventListener('click', () => {
    diningTabs.forEach(t => t.classList.remove('active'));
    tab.classList.add('active');
    const filter = tab.dataset.tab;
    diningCards.forEach(card => {
      card.classList.toggle('hidden-card', filter !== 'all' && card.dataset.category !== filter);
    });
  });
});

// ============ STAR RATINGS ============
const rateStars = document.querySelectorAll('.rate-star');
const fbRating  = document.getElementById('fbRating');

function highlightMainStars(count) {
  rateStars.forEach((s, i) => s.classList.toggle('active', i < count));
}

if (rateStars.length) {
  highlightMainStars(fbRating ? parseInt(fbRating.value || '5', 10) : 5);

  rateStars.forEach((star, index) => {
    const val = parseInt(star.dataset.val || (index + 1), 10);
    star.addEventListener('mouseover', () => highlightMainStars(val));
    star.addEventListener('mouseleave', () => {
      const saved = fbRating ? parseInt(fbRating.value || '5', 10) : 5;
      highlightMainStars(saved);
    });
    star.addEventListener('click', () => {
      if (fbRating) fbRating.value = String(val);
      highlightMainStars(val);
    });
  });
}

// Category mini-stars (Rooms, Dining, Service, Amenities)
document.querySelectorAll('.rating-cat').forEach(catBox => {
  const miniStars = catBox.querySelectorAll('.ms');
  let selectedCatVal = 5;

  const paintMini = count => {
    miniStars.forEach((s, i) => s.classList.toggle('active', i < count));
  };
  paintMini(selectedCatVal);

  miniStars.forEach((ms, idx) => {
    const val = parseInt(ms.dataset.val || (idx + 1), 10);
    ms.addEventListener('mouseover', () => paintMini(val));
    ms.addEventListener('mouseleave', () => paintMini(selectedCatVal));
    ms.addEventListener('click', () => {
      selectedCatVal = val;
      catBox.dataset.selectedRating = String(val);
      paintMini(selectedCatVal);
    });
  });
});


// ============ 3. FEEDBACK FORM -> SENDS TO hotel_api.php ============
const feedbackForm = document.getElementById('feedbackForm');
const fbSuccess    = document.getElementById('fbSuccess');

if (feedbackForm) {
  feedbackForm.addEventListener('submit', async e => {
    e.preventDefault();

    const breakdown = [];
    document.querySelectorAll('.rating-cat').forEach(catBox => {
      const catName = catBox.querySelector('label')?.textContent.trim();
      const catVal  = catBox.dataset.selectedRating || '5';
      if (catName) breakdown.push(`${catName}: ${catVal}/5`);
    });

    const baseReview = getFieldValue(feedbackForm, ['review'], 2);
    const fullReview = breakdown.length
      ? `${baseReview} [${breakdown.join(' | ')}]`
      : baseReview;

    const formData = new FormData();
    formData.append('action', 'submit_review');
    formData.append('name',   getFieldValue(feedbackForm, ['name'], 0));
    formData.append('email',  getFieldValue(feedbackForm, ['email'], 1));
    formData.append('rating', fbRating ? fbRating.value : '5');
    formData.append('review', fullReview);

    try {
      const response = await fetch('hotel_api.php', { method: 'POST', body: formData });
      const result   = await response.json();
      if (result.status === 'success') {
        if (fbSuccess) fbSuccess.classList.remove('hidden');
        feedbackForm.reset();
        highlightMainStars(5);
        if (fbRating) fbRating.value = '5';
        document.querySelectorAll('.rating-cat').forEach(catBox => {
          catBox.dataset.selectedRating = '5';
          catBox.querySelectorAll('.ms').forEach(s => s.classList.add('active'));
        });
        setTimeout(() => { if (fbSuccess) fbSuccess.classList.add('hidden'); }, 6000);
      } else {
        alert('Review error: ' + (result.message || 'Could not submit review.'));
      }
    } catch (err) {
      alert('Cannot reach hotel_api.php. Make sure Apache is running in XAMPP.');
    }
  });
}
