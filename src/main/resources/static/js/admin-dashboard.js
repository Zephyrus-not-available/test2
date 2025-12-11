(function () {
    const ADMIN_PIN = "99999";
    const POLL_INTERVAL_MS = 5000;

    // DOM refs
    const pinForm = document.getElementById('pinForm');
    const pinInput = document.getElementById('pinInput');
    const pinMessage = document.getElementById('pinMessage');
    const pinSection = document.getElementById('pinSection');

    const dashboard = document.getElementById('dashboard');
    const resultsTableBody = document.querySelector('#results-table tbody');
    const lastUpdatedEl = document.getElementById('lastUpdated');
    const manualRefreshBtn = document.getElementById('manualRefreshBtn');

    const createCategory = document.getElementById('createCategory');
    const createNumber = document.getElementById('createNumber');
    const createName = document.getElementById('createName');
    const createDept = document.getElementById('createDept');
    const createBtn = document.getElementById('createBtn');
    const refreshCandidatesBtn = document.getElementById('refreshCandidatesBtn');
    const createMsg = document.getElementById('createMsg');
    const candidatesList = document.getElementById('candidatesList');

    let pollHandle = null;
    let unlockedPin = null;

    // On admin dashboard load, remove any leftover voter PIN to avoid accidental navigation to selection pages
    try { localStorage.removeItem('votingPin'); } catch (e) { /* ignore */ }
    console.log('admin-dashboard initialized');

    // Helpers
    function showPinError(msg) {
        pinMessage.style.display = 'block';
        pinMessage.textContent = msg;
    }
    function clearPinError() {
        pinMessage.style.display = 'none';
        pinMessage.textContent = '';
    }

    function enableDashboard(pin) {
        unlockedPin = pin;
        // Persist admin PIN only in sessionStorage (do not touch votingPin to avoid voter flow redirects)
        try { sessionStorage.setItem('adminPin', pin); } catch (e) { /* ignore */ }
        pinSection.style.display = 'none';
        dashboard.style.display = '';
        startPolling();
        refreshCandidatesList();
    }

    function validatePinInput(val) {
        return /^[0-9]{5}$/.test(val) && val === ADMIN_PIN;
    }

    // Fetch and render results
    async function fetchAndRenderResults() {
        if (!unlockedPin) return;
        try {
            const res = await fetch(`/api/admin/results?pin=${encodeURIComponent(unlockedPin)}`, { cache: 'no-store' });
            if (!res.ok) {
                console.warn('Server rejected pin or error:', res.status);
                // lock UI if server rejects (fail closed)
                lockDashboard();
                return;
            }
            const data = await res.json();
            renderResults(data);
            lastUpdatedEl.textContent = 'Last updated: ' + new Date().toLocaleTimeString();
        } catch (err) {
            console.error('Error fetching admin results:', err);
        }
    }

    function renderResults(list) {
        // list is expected to be an array of candidate DTOs sorted descending by votes
        resultsTableBody.innerHTML = '';
        if (!Array.isArray(list)) return;
        list.forEach((c, idx) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="p-2 border align-top">${c.candidateNumber != null ? c.candidateNumber : ''}</td>
                <td class="p-2 border align-top">${escapeHtml(c.name || '')}</td>
                <td class="p-2 border align-top">${escapeHtml(c.department || '')}</td>
                <td class="p-2 border align-top">${c.id != null && c.id.toString().includes('-') ? '' : (c.category || '')}</td>
                <td class="p-2 border align-top font-semibold">${c.voteCount != null ? c.voteCount : 0}</td>
            `;
            resultsTableBody.appendChild(tr);
        });
    }

    // Simple escaping helper
    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, function (m) {
            return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[m];
        });
    }

    // Lock dashboard
    function lockDashboard() {
        unlockedPin = null;
        // Remove persisted admin PIN
        try { sessionStorage.removeItem('adminPin'); } catch (e) { /* ignore */ }
        dashboard.style.display = 'none';
        pinSection.style.display = '';
        if (pollHandle) {
            clearInterval(pollHandle);
            pollHandle = null;
        }
    }

    function startPolling() {
        if (pollHandle) clearInterval(pollHandle);
        // initial immediate fetch
        fetchAndRenderResults();
        pollHandle = setInterval(fetchAndRenderResults, POLL_INTERVAL_MS);
    }

    // PIN form handling
    pinForm.addEventListener('submit', function (ev) {
        ev.preventDefault();
        clearPinError();
        const val = (pinInput.value || '').trim();
        if (!/^[0-9]{5}$/.test(val)) {
            showPinError('PIN must be exactly 5 digits.');
            return;
        }
        if (val !== ADMIN_PIN) {
            showPinError('Incorrect PIN. Access denied.');
            return;
        }
        // Valid client-side PIN: reveal and start polling, per requirements pass client PIN to server
        enableDashboard(val);
    });

    // Also handle the unlock button click (form prevented from default submission in template)
    const pinSubmitBtn = document.getElementById('pinSubmit');
    if (pinSubmitBtn) {
        pinSubmitBtn.addEventListener('click', function () {
            const val = (pinInput.value || '').trim();
            pinForm.dispatchEvent(new Event('submit', { cancelable: true }));
        });
    }

    manualRefreshBtn.addEventListener('click', function () {
        fetchAndRenderResults();
        refreshCandidatesList();
    });

    // -------------------------------
    // Candidate management (CRUD)
    // -------------------------------

    // We will use admin endpoints secured by adminPin query parameter
    const ADMIN_PIN_QUERY = '?adminPin=' + encodeURIComponent(ADMIN_PIN);

    // Create
    createBtn.addEventListener('click', async function () {
        createMsg.style.display = 'none';
        const payload = {
            category: (createCategory.value || '').toUpperCase(),
            candidateNumber: createNumber.value ? parseInt(createNumber.value, 10) : null,
            name: createName.value || '',
            department: createDept.value || '',
            imageUrl: null,
            voteCount: 0
        };
        // Basic client validation
        if (!payload.name || !payload.candidateNumber || !payload.category) {
            createMsg.style.display = 'block';
            createMsg.className = 'text-sm text-red-600';
            createMsg.textContent = 'Please provide category, candidate number and name.';
            return;
        }
        try {
            const res = await fetch('/api/admin/candidates' + ADMIN_PIN_QUERY, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) {
                const txt = await res.text();
                createMsg.style.display = 'block';
                createMsg.className = 'text-sm text-red-600';
                createMsg.textContent = 'Create failed: ' + (txt || res.status);
                return;
            }
            const created = await res.json();
            createMsg.style.display = 'block';
            createMsg.className = 'text-sm text-green-600';
            createMsg.textContent = 'Candidate created (ID: ' + (created.id || 'N/A') + ')';
            // Clear form
            createNumber.value = '';
            createName.value = '';
            createDept.value = '';
            // refresh list
            refreshCandidatesList();
            // Also refresh live results
            fetchAndRenderResults();
        } catch (err) {
            createMsg.style.display = 'block';
            createMsg.className = 'text-sm text-red-600';
            createMsg.textContent = 'Create error';
            console.error(err);
        }
    });

    refreshCandidatesBtn.addEventListener('click', function () {
        refreshCandidatesList();
    });

    // Refresh candidate list for management UI.
    // Because there is no single "GET all candidates" admin endpoint in base project, we derive candidates from the public /api/results/all endpoint,
    // which returns categories with candidate details. This is purely client-side convenience to list candidates for update/delete actions.
    async function refreshCandidatesList() {
        candidatesList.innerHTML = 'Loading...';
        try {
            const res = await fetch('/api/results/all', { cache: 'no-store' });
            if (!res.ok) {
                candidatesList.innerHTML = 'Failed to load candidates.';
                return;
            }
            const categories = await res.json(); // list of ResultDTO
            // Flatten candidates to a list
            const flat = [];
            categories.forEach(cat => {
                if (Array.isArray(cat.candidates)) {
                    cat.candidates.forEach(c => {
                        flat.push({
                            id: c.id,
                            candidateNumber: c.candidateNumber,
                            name: c.name,
                            department: c.department,
                            voteCount: c.voteCount,
                            category: cat.category || c.category || ''
                        });
                    });
                }
            });

            // Render
            if (flat.length === 0) {
                candidatesList.innerHTML = '<div class="text-sm text-gray-600">No candidates found.</div>';
                return;
            }
            candidatesList.innerHTML = '';
            flat.forEach(c => {
                const div = document.createElement('div');
                div.className = 'flex items-center justify-between gap-2 p-2 border rounded';
                div.innerHTML = `
                    <div class="flex-1">
                        <div class="font-semibold">${escapeHtml(c.name || '')} <span class="text-xs text-gray-500">#${c.candidateNumber || ''} (${c.category || ''})</span></div>
                        <div class="text-xs text-gray-600">Dept: ${escapeHtml(c.department || '')} • Votes: ${c.voteCount != null ? c.voteCount : 0}</div>
                    </div>
                    <div class="flex gap-2">
                        <button class="editBtn px-2 py-1 bg-yellow-100 text-yellow-800 rounded" data-id="${c.id}">Edit</button>
                        <button class="delBtn px-2 py-1 bg-red-100 text-red-700 rounded" data-id="${c.id}">Delete</button>
                    </div>
                `;
                candidatesList.appendChild(div);
            });

            // Attach handlers
            candidatesList.querySelectorAll('.delBtn').forEach(btn => {
                btn.addEventListener('click', async function () {
                    const id = this.getAttribute('data-id');
                    if (!id) return;
                    if (!confirm('Delete candidate id ' + id + '?')) return;
                    try {
                        const r = await fetch('/api/admin/candidates/' + encodeURIComponent(id) + ADMIN_PIN_QUERY, {
                            method: 'DELETE'
                        });
                        if (!r.ok) {
                            alert('Delete failed: ' + r.status);
                            return;
                        }
                        // refresh lists
                        refreshCandidatesList();
                        fetchAndRenderResults();
                    } catch (err) {
                        console.error('Delete error', err);
                        alert('Delete error');
                    }
                });
            });

            candidatesList.querySelectorAll('.editBtn').forEach(btn => {
                btn.addEventListener('click', function () {
                    const id = this.getAttribute('data-id');
                    if (!id) return;
                    // Show a small prompt-based update UX (keeps page simple)
                    const newName = prompt('Enter new name for candidate ID ' + id + ':');
                    if (newName === null) return; // cancelled
                    const newDept = prompt('Enter department (leave blank to keep):');
                    // Build payload
                    const payload = {};
                    if (newName !== null && newName !== '') payload.name = newName;
                    if (newDept !== null && newDept !== '') payload.department = newDept;
                    // Send PUT
                    (async () => {
                        try {
                            const r = await fetch('/api/admin/candidates/' + encodeURIComponent(id) + ADMIN_PIN_QUERY, {
                                method: 'PUT',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(payload)
                            });
                            if (!r.ok) {
                                alert('Update failed: ' + r.status);
                                return;
                            }
                            // refresh lists
                            refreshCandidatesList();
                            fetchAndRenderResults();
                        } catch (err) {
                            console.error('Update error', err);
                            alert('Update error');
                        }
                    })();
                });
            });

        } catch (err) {
            console.error('Error refreshing candidates', err);
            candidatesList.innerHTML = 'Error loading candidates.';
        }
    }

    // Start locked
    (function(){
        // hide dashboard initially
        if (dashboard) dashboard.style.display = 'none';
        if (pinSection) pinSection.style.display = '';
    })();

})();
