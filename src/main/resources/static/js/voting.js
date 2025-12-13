(() => {
  // --- Configuration ---
  const STYLE_ID = 'voting-radio-grid-styles';
  const DEFAULT_API_BASE = '/api';
  const API_BASE = (() => {
    const meta = document.querySelector('meta[name="api-base"]')?.content;
    const explicit = window.API_BASE || meta || DEFAULT_API_BASE;
    return explicit.endsWith('/') ? explicit.slice(0, -1) : explicit;
  })();
  const CATEGORIES = ['KING', 'QUEEN', 'PRINCE', 'PRINCESS', 'COUPLE'];

  // --- Utilities ---
  const $ = (sel, root = document) => root.querySelector(sel);
  const debounce = (fn, ms = 80) => {
    let t;
    return (...args) => {
      clearTimeout(t);
      t = setTimeout(() => fn(...args), ms);
    };
  };
  const capitalize = (s = '') => s.charAt(0).toUpperCase() + s.slice(1).toLowerCase();

  // device id + pin storage
  const getDeviceId = () => {
    let id = localStorage.getItem('deviceId');
    if (!id) {
      id = 'dev-' + Math.random().toString(36).slice(2);
      localStorage.setItem('deviceId', id);
    }
    return id;
  };
  const savePin = (pin) => localStorage.setItem('userPin', pin);
  const getStoredPin = () => localStorage.getItem('userPin');

  // selection storage keys
  const selectionValueKey = (category) => `sel:${category}:num`;
  const selectedObjectKey = (category) => `sel:${category}:obj`;
  const saveSelection = (category, obj) => {
    localStorage.setItem(selectionValueKey(category), String(obj?.candidateNumber || ''));
    localStorage.setItem(selectedObjectKey(category), JSON.stringify(obj || {}));
  };
  const loadSelection = (category) => {
    const obj = localStorage.getItem(selectedObjectKey(category));
    if (obj) {
      try { return JSON.parse(obj); } catch(_) {}
    }
    const num = localStorage.getItem(selectionValueKey(category));
    if (num) return { candidateNumber: parseInt(num, 10) };
    return null;
  };
  const clearSelections = () =>
  CATEGORIES.forEach(c => {
    localStorage.removeItem(selectionValueKey(c));
    localStorage.removeItem(selectedObjectKey(c));
  });

  // --- Skeleton helpers ---
  const pageSkeletons = new Set();
  const registerSkeleton = (id) => {
    const el = document.getElementById(id);
    if (el) pageSkeletons.add(el);
    return el;
  };
  const registerAllSkeletons = () => {
    // Register common skeleton IDs
    ['selection-loading', 'options-skeleton'].forEach(registerSkeleton);
  };
  const showSkeletons = () => pageSkeletons.forEach(el => el.classList.remove('hidden'));
  const hideSkeletons = () => pageSkeletons.forEach(el => el.classList.add('hidden'));

  // --- Keypad feedback ---
  let audioContext = null;
  const getAudioContext = () => {
    if (!audioContext) {
      audioContext = new (window.AudioContext || window.webkitAudioContext)();
    }
    return audioContext;
  };
  const playKeypadFeedback = (btn) => {
    if (btn) {
      btn.classList.add('keypad-pressed');
      setTimeout(() => btn.classList.remove('keypad-pressed'), 150);
    }
    try {
      const ctx = getAudioContext();
      const oscillator = ctx.createOscillator();
      const gainNode = ctx.createGain();
      oscillator.connect(gainNode);
      gainNode.connect(ctx.destination);
      oscillator.frequency.value = 1200;
      oscillator.type = 'sine';
      gainNode.gain.setValueAtTime(0.1, ctx.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.08);
      oscillator.start(ctx.currentTime);
      oscillator.stop(ctx.currentTime + 0.08);
    } catch (e) {}
  };

  // Sound and visual feedback for radio button selection
  const playRadioFeedback = (labelEl) => {
    // Visual scale effect
    if (labelEl) {
      labelEl.style.transform = 'scale(1.1)';
      labelEl.style.transition = 'transform 0.15s ease';
      setTimeout(() => {
        labelEl.style.transform = 'scale(1)';
      }, 150);
    }
    // Play selection sound (higher pitch, pleasant tone)
    try {
      const ctx = getAudioContext();
      const oscillator = ctx.createOscillator();
      const gainNode = ctx.createGain();
      oscillator.connect(gainNode);
      gainNode.connect(ctx.destination);
      oscillator.frequency.value = 880; // A5 note - pleasant selection sound
      oscillator.type = 'sine';
      gainNode.gain.setValueAtTime(0.08, ctx.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.1);
      oscillator.start(ctx.currentTime);
      oscillator.stop(ctx.currentTime + 0.1);
    } catch (e) {}
  };

  // --- API helpers ---
  const verifyPin = async (pin) => {
    const res = await fetch(`${API_BASE}/auth/verify-pin`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ pin }),
    });
    if (res.status === 404) return { valid: false, alreadyVoted: false };
    if (!res.ok) throw new Error(await res.text() || 'Unable to verify PIN');
    return res.json();
  };

  const fetchCandidates = async (category) => {
    // Ensure the path matches controller enum handling; use uppercase category to be explicit
    const res = await fetch(`${API_BASE}/candidates/${String(category).toUpperCase()}`);
    if (!res.ok) throw new Error(await res.text() || 'Failed to load candidates');
    return res.json();
  };

  const submitVotes = async ({ pin, deviceId, votes }) => {
    const res = await fetch(`${API_BASE}/voting/bulk-vote`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ pin, deviceId, votes }),
    });

    const contentType = res.headers.get('content-type') || '';
    const body = contentType.includes('application/json')
      ? await res.json()
      : await res.text();

    if (!res.ok) {
      const msg = body && body.message ? body.message : (body || 'Vote submission failed.');
      throw new Error(msg);
    }
    return body;
  };

  // --- RESPONSIVE CSS (FORCE ALWAYS 5 COLUMNS) ---
  function injectStyles() {
    if (document.head.querySelector(`#${STYLE_ID}`)) return;

    const css = `
      .radio-grid {
        display: grid !important;
        grid-template-columns: repeat(5, 1fr) !important;
        gap: 12px;
        padding: 8px;
      }
      .radio-label {
        display:flex;
        flex-direction:column;
        align-items:center;
        justify-content:center;
        width:100%;
        min-height:72px;
        padding:8px;
        box-sizing:border-box;
        border-radius:8px;
        cursor:pointer;
        user-select:none;
      }
      .radio-label input[type="radio"] {
        width:28px;
        height:28px;
        margin-top:8px;
      }
      .radio-grid .spacer {
        visibility:hidden;
        pointer-events:none;
      }

      /* BUTTONS MUST NEVER BE INSIDE GRID */
      #next-btn,
      #prev-btn {
        display: block !important;
        width: 100%;
        margin-top: 20px;
      }
    `;

    const style = document.createElement('style');
    style.id = STYLE_ID;
    style.appendChild(document.createTextNode(css));
    document.head.appendChild(style);
  }

  // --- ALWAYS 5 COLUMN DESKTOP LAYOUT ---
  function layoutOptions(container) {
    if (!container) return;
    injectStyles();

    container.style.gridTemplateColumns = `repeat(5, 1fr)`;

    // Remove old spacers
    Array.from(container.querySelectorAll('.spacer')).forEach(s => s.remove());

    const children = Array.from(container.children).filter(n => n.nodeType === 1);
    const total = children.length;

    if (total <= 5) return;

    const bottomCount = total - 5;
    if (bottomCount <= 0 || bottomCount >= 5) return;

    const offset = Math.floor((5 - bottomCount) / 2);
    const firstBottom = container.children[5] || null;

    for (let i = 0; i < offset; i++) {
      const spacer = document.createElement('div');
      spacer.className = 'spacer';
      container.insertBefore(spacer, firstBottom);
    }
  }

  const debouncedLayout = debounce(() => {
    const c = document.getElementById('options-container');
    if (c) layoutOptions(c);
  }, 100);

  window.addEventListener('resize', debouncedLayout);

  function observeContainer(id = 'options-container') {
    const container = document.getElementById(id);
    if (!container) return;

    layoutOptions(container);

    const mo = new MutationObserver(() => {
      clearTimeout(container.__layoutTimeout);
      container.__layoutTimeout = setTimeout(() => layoutOptions(container), 60);
    });

    mo.observe(container, { childList: true });
    container.__layoutObserver = mo;
  }

  // --- Selection Page Init ---
  async function initSelectionPage({ category, nextUrl, prevUrl } = {}) {
    const pin = getStoredPin();
    if (!pin) { window.location.href = '/pin'; return; }

    const container = document.getElementById('options-container');
    const errorModal = document.getElementById('selection-error-modal');
    const nextBtn = document.getElementById('next-btn');
    const prevBtn = document.getElementById('prev-btn');
    const candidateImage = document.getElementById('candidate-image');
    const candidateNumber = document.getElementById('candidate-number');
    const candidateName = document.getElementById('candidate-name');
    const candidateDepartment = document.getElementById('candidate-department');

    // Register all skeleton elements for this page
    registerAllSkeletons();

    // ⭐ NEW FIX: Ensure buttons NEVER live inside the grid container
    const ensureButtonsOutside = () => {
      if (container && nextBtn && container.contains(nextBtn)) {
        container.after(nextBtn);
      }
      if (container && prevBtn && container.contains(prevBtn)) {
        nextBtn.after(prevBtn);
      }
    };
    ensureButtonsOutside();

    showSkeletons();
    let candidates = [];
    try {
      candidates = await fetchCandidates(category);
    } catch (err) {
      if (container)
      container.innerHTML = `<p class="text-red-600 text-center">${err.message}</p>`;
      if (nextBtn) {
        nextBtn.disabled = true;
        nextBtn.classList.add('opacity-50', 'cursor-not-allowed');
      }
      hideSkeletons();
      return;
    }
    hideSkeletons();

    if (!Array.isArray(candidates) || candidates.length === 0) {
      if (container)
      container.innerHTML = `<p class="text-gray-700 text-center">No candidates available.</p>`;
      if (nextBtn) {
        nextBtn.disabled = true;
        nextBtn.classList.add('opacity-50', 'cursor-not-allowed');
      }
      return;
    }

    let selectedNumber = loadSelection(category)?.candidateNumber || null;

    const placeholderImage = (candidate) =>
    candidate?.imageUrl?.trim()
      ? candidate.imageUrl
      : 'https://placehold.co/300x400/f3f4f6/1e3a8a?text=Candidate';

    const updateCandidateDisplay = (candidate) => {
      if (!candidate) return;
      if (candidateImage) candidateImage.src = placeholderImage(candidate);
      if (candidateNumber) candidateNumber.textContent = `No ${candidate.candidateNumber}`;
      if (candidateName) candidateName.textContent = candidate.name || '';
      if (candidateDepartment)
      candidateDepartment.textContent = candidate.department || '';
    };

    const updateNextButton = () => {
      if (!nextBtn) return;

      if (selectedNumber) {
        nextBtn.style.opacity = '1';
        nextBtn.style.cursor = 'pointer';
        nextBtn.disabled = false;
      } else {
        nextBtn.style.opacity = '0.7';
        nextBtn.disabled = true;
      }
    };

    // Render Options
    if (container) {
      container.innerHTML = '';
      container.classList.add('radio-grid');

      candidates.forEach((candidate) => {
        const label = document.createElement('label');
        label.className = 'radio-label';
        label.htmlFor = `option-${candidate.candidateNumber}`;
        label.innerHTML = `
          <span class="text-sm font-medium text-gray-700">${candidate.candidateNumber}</span>
          <input type="radio" id="option-${candidate.candidateNumber}" name="selection" value="${candidate.candidateNumber}" />
        `;
        container.appendChild(label);
      });

      layoutOptions(container);

      container.querySelectorAll('input[name="selection"]').forEach((input) => {
        input.addEventListener('change', (evt) => {
          selectedNumber = parseInt(evt.target.value, 10);
          const cand = candidates.find(c => c.candidateNumber === selectedNumber);

          // Play sound and visual feedback
          const labelEl = evt.target.closest('.radio-label');
          playRadioFeedback(labelEl);

          if (cand) {
            saveSelection(category, cand);
            updateCandidateDisplay(cand);
          }
          updateNextButton();
        });
      });
    }

    // Restore saved selection or show first candidate
    if (selectedNumber) {
      const saved = candidates.find(c => c.candidateNumber === selectedNumber);
      if (saved) {
        const input = document.querySelector(
          `input[name="selection"][value="${selectedNumber}"]`
        );
        if (input) input.checked = true;
        updateCandidateDisplay(saved);
      } else selectedNumber = null;
    }

    // Show random candidate's data on initial load if no selection saved
    if (!selectedNumber && candidates.length > 0) {
      const randomIndex = Math.floor(Math.random() * candidates.length);
      updateCandidateDisplay(candidates[randomIndex]);
    }

    updateNextButton();

    if (nextBtn) {
      nextBtn.addEventListener('click', () => {
        if (!selectedNumber) {
          if (errorModal) errorModal.classList.remove('hidden');
          return;
        }

        const cand = candidates.find(c => c.candidateNumber === selectedNumber);
        if (cand) saveSelection(category, cand);

        if (nextUrl) window.location.href = nextUrl;
      });
    }

    if (prevBtn && prevUrl)
    prevBtn.addEventListener('click', () => (window.location.href = prevUrl));

    document.querySelectorAll('[data-close-modal]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-close-modal');
      const m = document.getElementById(id);
      if (m) m.classList.add('hidden');
    })
    );
  }

  // --- Init ---
  function initAuto() {
    injectStyles();
    observeContainer('options-container');
  }

  if (document.readyState === 'loading')
  document.addEventListener('DOMContentLoaded', initAuto);
  else initAuto();

  // --- Public API ---
  window.VotingApp = Object.assign(window.VotingApp || {}, {
    API_BASE,
    CATEGORIES,
    getDeviceId,
    getStoredPin,
    savePin,
    verifyPin,
    fetchCandidates,
    submitVotes,
    saveSelection,
    loadSelection,
    clearSelections,
    initSelectionPage,
    layoutOptions,
    playKeypadFeedback,
    playRadioFeedback,
  });
})();
