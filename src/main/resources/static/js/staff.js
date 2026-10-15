// SMART PARK — Staff Portal JavaScript

let verifiedReservation = null;

async function initStaffDashboard() {
    try {
        const res = await apiFetch('/api/staff/dashboard-stats');
        const s = res.data;

        document.getElementById('staffMetricToday').textContent = s.todayReservations || 0;
        document.getElementById('staffMetricArrivals').textContent = s.expectedArrivals || 0;
        document.getElementById('staffMetricParked').textContent = s.currentlyParked || 0;
        document.getElementById('staffMetricCompleted').textContent = s.completedSessions || 0;

        loadActiveSessions();
    } catch (e) {
        showToast('Error loading staff dashboard: ' + e.message, 'danger');
    }
}

async function verifyBooking(identifierOverride) {
    const input = document.getElementById('searchBookingInput');
    const identifier = identifierOverride || (input ? input.value.trim() : '');

    if (!identifier) {
        showToast('Please enter a booking reference or QR token', 'warning');
        return;
    }

    const card = document.getElementById('verificationResultCard');
    if (card) card.classList.remove('d-none');
    
    const detailsContainer = document.getElementById('verificationDetails');
    detailsContainer.innerHTML = `<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div><div class="mt-2 text-muted">Verifying booking with server...</div></div>`;

    try {
        const res = await apiFetch('/api/staff/verify-booking', {
            method: 'POST',
            body: JSON.stringify({ identifier: identifier })
        });

        verifiedReservation = res.data;
        renderVerificationDetails(verifiedReservation);
    } catch (e) {
        verifiedReservation = null;
        detailsContainer.innerHTML = `
            <div class="alert alert-danger text-center">
                <i class="bi bi-x-octagon-fill fs-3 d-block mb-2"></i>
                <h5 class="fw-bold">Verification Failed</h5>
                <p class="mb-0">${e.message}</p>
            </div>
        `;
    }
}

function renderVerificationDetails(r) {
    const detailsContainer = document.getElementById('verificationDetails');
    const isEligibleForEntry = (r.status === 'CONFIRMED' || r.status === 'PENDING') && !r.checkedIn;
    const isEligibleForExit = r.status === 'ACTIVE';

    let actionBtn = '';
    if (isEligibleForEntry) {
        actionBtn = `
            <button class="btn btn-success btn-lg w-100 mt-3" onclick="executeCheckIn('${r.bookingReference}')">
                <i class="bi bi-box-arrow-in-right me-1"></i> Record Vehicle Entry (Check-In)
            </button>
        `;
    } else if (isEligibleForExit) {
        actionBtn = `
            <button class="btn btn-primary btn-lg w-100 mt-3" onclick="executeCheckOut(${r.id})">
                <i class="bi bi-box-arrow-right me-1"></i> Record Vehicle Exit (Check-Out & Bill)
            </button>
        `;
    } else if (r.status === 'COMPLETED') {
        actionBtn = `<div class="alert alert-info mt-3 text-center mb-0"><i class="bi bi-check-circle-fill me-1"></i> Booking already completed. Checked out at ${formatDateTime(r.exitTime)}.</div>`;
    } else if (r.status === 'CANCELLED') {
        actionBtn = `<div class="alert alert-danger mt-3 text-center mb-0"><i class="bi bi-slash-circle-fill me-1"></i> This reservation has been cancelled. Entry denied.</div>`;
    }

    detailsContainer.innerHTML = `
        <div class="row g-3">
            <div class="col-md-6">
                <div class="p-3 bg-light rounded-3">
                    <div class="text-muted small">Booking Reference</div>
                    <div class="fs-5 fw-bold text-primary">${r.bookingReference}</div>
                    
                    <div class="text-muted small mt-2">Customer</div>
                    <div class="fw-semibold">${r.userFullName} (${r.userEmail})</div>
                    <div><small class="text-muted"><i class="bi bi-telephone"></i> ${r.userPhone || '—'}</small></div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="p-3 bg-light rounded-3">
                    <div class="text-muted small">Vehicle Details</div>
                    <div class="fs-5 fw-bold text-dark">${r.vehicleRegistration}</div>
                    <div class="badge bg-secondary mb-2">${r.vehicleType}</div>

                    <div class="text-muted small">Assigned Location</div>
                    <div class="fw-bold">${r.parkingAreaName} — Slot ${r.slotNumber}</div>
                </div>
            </div>
            <div class="col-md-12">
                <div class="d-flex justify-content-between align-items-center p-3 border rounded-3 bg-white">
                    <div>
                        <div class="small text-muted">Reservation Status</div>
                        <span class="badge ${getStatusBadge(r.status)} fs-6">${r.status}</span>
                    </div>
                    <div>
                        <div class="small text-muted">Scheduled Window</div>
                        <div class="small fw-semibold">${formatDateTime(r.startTime)} to ${formatDateTime(r.endTime)}</div>
                    </div>
                    <div class="text-end">
                        <div class="small text-muted">Rate / Est. Fee</div>
                        <div class="fw-bold fs-5 text-success">₹${r.hourlyRate}/hr (${formatCurrency(r.estimatedFee)})</div>
                    </div>
                </div>
            </div>
        </div>
        ${actionBtn}
    `;
}

async function executeCheckIn(identifier) {
    try {
        const res = await apiFetch('/api/staff/sessions/check-in', {
            method: 'POST',
            body: JSON.stringify({ identifier: identifier })
        });
        showToast('Vehicle entry recorded successfully! Slot marked OCCUPIED.', 'success');
        renderVerificationDetails(res.data);
        loadActiveSessions();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function executeCheckOut(reservationId) {
    try {
        const res = await apiFetch('/api/staff/sessions/check-out', {
            method: 'POST',
            body: JSON.stringify({ reservationId: reservationId })
        });
        const r = res.data;
        showToast(`Vehicle checked out! Duration: ${r.durationMinutes} mins. Final fee: ${formatCurrency(r.actualFee)}`, 'success');
        renderVerificationDetails(r);
        loadActiveSessions();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function loadActiveSessions() {
    const table = document.getElementById('activeSessionsTableBody');
    if (!table) return;

    try {
        const res = await apiFetch('/api/staff/sessions/active');
        const sessions = res.data || [];

        if (sessions.length === 0) {
            table.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4"><i class="bi bi-check2-circle fs-3 d-block mb-1"></i>No vehicles currently parked in your assigned areas.</td></tr>`;
            return;
        }

        table.innerHTML = sessions.map(s => `
            <tr>
                <td><strong>${s.bookingReference}</strong></td>
                <td>${s.vehicleRegistration} <span class="badge bg-secondary">${s.vehicleType}</span></td>
                <td>${s.parkingAreaName} (${s.slotNumber})</td>
                <td>${s.userFullName}</td>
                <td>${formatDateTime(s.entryTime)}</td>
                <td><span class="badge bg-success">PARKED</span></td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="executeCheckOut(${s.id})">
                        <i class="bi bi-box-arrow-right"></i> Check-Out
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        table.innerHTML = `<tr><td colspan="7" class="text-danger text-center">Failed to load active sessions: ${e.message}</td></tr>`;
    }
}
