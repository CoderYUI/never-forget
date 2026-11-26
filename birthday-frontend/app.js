/**
 * Birthday Wishes Application — Frontend Script
 * Handles backend health polling, registration submission, dynamic metrics, and theme toggling.
 */

// --------------------------------------------------------------------------
// 1. Backend URL Configuration
// When deploying, replace with your Render URL: e.g. "https://birthday-backend-xxxx.onrender.com"
// --------------------------------------------------------------------------
const BACKEND_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
  ? 'http://localhost:8080'
  : 'https://birthday-backend-render-url.onrender.com'; // Update this after deploying to Render

// DOM Elements
const root = document.documentElement;
const themeToggleBtn = document.getElementById('theme-toggle');
const themeIcon = document.getElementById('theme-icon');
const themeLabel = document.getElementById('theme-label');

const backendStatusEl = document.getElementById('backend-status');
const statusTextEl = document.getElementById('status-text');
const userCountEl = document.getElementById('user-count');
const dailyQuoteEl = document.getElementById('daily-quote');

const registerForm = document.getElementById('register-form');
const submitBtn = document.getElementById('submit-btn');
const btnText = document.getElementById('btn-text');
const btnLoader = document.getElementById('btn-loader');
const successCard = document.getElementById('success-card');
const successMessage = document.getElementById('success-message');
const resetFormBtn = document.getElementById('reset-form-btn');
const toastEl = document.getElementById('toast');

// --------------------------------------------------------------------------
// 2. Theme Switching Logic (Persisted in localStorage)
// --------------------------------------------------------------------------
function initTheme() {
  const savedTheme = localStorage.getItem('theme');
  const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  const initialTheme = savedTheme || (systemPrefersDark ? 'dark' : 'light');

  applyTheme(initialTheme);

  themeToggleBtn.addEventListener('click', () => {
    const currentTheme = root.dataset.theme;
    const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
    applyTheme(nextTheme);
  });
}

function applyTheme(theme) {
  root.dataset.theme = theme;
  localStorage.setItem('theme', theme);
  if (theme === 'dark') {
    themeIcon.textContent = '☼';
    themeLabel.textContent = 'Light';
  } else {
    themeIcon.textContent = '◐';
    themeLabel.textContent = 'Dark';
  }
}

// --------------------------------------------------------------------------
// 3. Backend Health & User Count Polling
// --------------------------------------------------------------------------
async function checkBackendHealth() {
  try {
    const response = await fetch(`${BACKEND_URL}/api/health`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });

    if (response.ok) {
      const data = await response.json();

      // Update Header Status Pill
      backendStatusEl.className = 'ui-status ui-status-live ui-status-success';
      statusTextEl.textContent = 'Memory bank ready ♡';

      // Update Total Registered Birthdays
      if (data.totalUsers !== undefined) {
        userCountEl.textContent = data.totalUsers.toLocaleString();
      }

      // Update Daily Cute Quote
      if (data.quote) {
        dailyQuoteEl.textContent = `"${data.quote}"`;
      }
    } else {
      throw new Error(`HTTP ${response.status}`);
    }
  } catch (error) {
    console.warn('Backend ping:', error.message);
    backendStatusEl.className = 'ui-status ui-status-warning';
    statusTextEl.textContent = 'Waking up memory...';
  }
}

// --------------------------------------------------------------------------
// 4. Registration Form Submission
// --------------------------------------------------------------------------
registerForm.addEventListener('submit', async (e) => {
  e.preventDefault();

  const username = document.getElementById('username').value.trim();
  const email = document.getElementById('email').value.trim();
  const country = document.getElementById('country').value.trim();
  const dateOfBirth = document.getElementById('dateOfBirth').value;

  // Basic Validation
  if (!username || !email || !country || !dateOfBirth) {
    showToast('Please fill in all the fields so I don’t forget!', 'error');
    return;
  }

  // Set Loading State
  setSubmitting(true);

  try {
    const response = await fetch(`${BACKEND_URL}/api/users/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify({
        username,
        email,
        country,
        dateOfBirth
      })
    });

    const result = await response.json();

    if (response.ok && result.success) {
      // Success!
      showToast('Yay! Saved forever! Check your inbox for a tiny hello ♡', 'success');

      // Show Success View
      registerForm.classList.add('hidden');
      successMessage.textContent = `Yay ${username}! I've locked your birthday (${dateOfBirth}) into my memory and sent a welcome hello note to ${email}. You will never be forgotten!`;
      successCard.classList.remove('hidden');

      // Refresh memory counter
      checkBackendHealth();
    } else {
      // API returned error message (e.g. email already exists)
      const errorMsg = result.message || 'Registration failed. Please try again.';
      showToast(errorMsg, 'error');
    }
  } catch (error) {
    console.error('Registration network error:', error);
    showToast('Our little birthday bear is still getting dressed for the party. Please try again in a moment!', 'error');
  } finally {
    setSubmitting(false);
  }
});

// Reset Form to register another
resetFormBtn.addEventListener('click', () => {
  registerForm.reset();
  successCard.classList.add('hidden');
  registerForm.classList.remove('hidden');
});

function setSubmitting(isSubmitting) {
  submitBtn.disabled = isSubmitting;
  if (isSubmitting) {
    btnText.textContent = 'Saving into my memory...';
    btnLoader.classList.remove('hidden');
  } else {
    btnText.innerHTML = 'Lock In My Birthday &nbsp;♡';
    btnLoader.classList.add('hidden');
  }
}

// --------------------------------------------------------------------------
// 5. Toast Notification System
// --------------------------------------------------------------------------
let toastTimer = null;
function showToast(message, type = 'info') {
  clearTimeout(toastTimer);
  toastEl.textContent = message;
  toastEl.className = `ui-toast ${type}`;
  toastEl.classList.remove('hidden');

  toastTimer = setTimeout(() => {
    toastEl.classList.add('hidden');
  }, 4500);
}

// --------------------------------------------------------------------------
// 6. Application Initialization
// --------------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  checkBackendHealth();

  // Poll health every 30 seconds while the page stays open
  setInterval(checkBackendHealth, 30000);
});
