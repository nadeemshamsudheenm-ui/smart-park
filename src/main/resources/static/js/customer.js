// SMART PARK — Customer Portal JavaScript

let selectedSlotId = null;
let selectedSlotRate = 0;
let parkingAreasList = [];

// ==========================================
// 1. Customer Dashboard Logic
// ==========================================
async function initCustomerDashboard() {
    try {
        const res = await apiFetch('/api/reservations/my');
        const reservations = res.data || [];

        // Calculate summary metrics
        const total = reservations.length;
        const upcoming = reservations.filter(r => r.status === 'CONFIRMED' || r.status === 'PENDING').length;
        const active = reservations.filter(r => r.status === 'ACTIVE').length;
        const completed = reservations.filter(r => r.status === 'COMPLETED').length;

        document.getElementById('metricTotalBookings').textContent = total;
        document.getElementById('metricUpcoming').textContent = upcoming;
        document.getElementById('metricActiveSessions').textContent = active;
        document.getElementById('metricCompleted').textContent = completed;

        // Render upcoming reservations table
        const upcomingTable = document.getElementById('upcomingReservationsBody');
        if (upcomingTable) {
            const upList = reservations.filter(r => r.status === 'CONFIRMED' || r.status === 'ACTIVE');
            if (upList.length === 0) {
                upcomingTable.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4"><i class="bi bi-inbox fs-3 d-block mb-1"></i>No upcoming reservations.</td></tr>`;
            } else {
                upcomingTable.innerHTML = upList.map(r => `
                    <tr>
                        <td><span class="fw-bold">${r.bookingReference}</span></td>
                        <td>${r.parkingAreaName} <br><small class="text-muted">Slot: ${r.slotNumber}</small></td>
                        <td>${r.vehicleRegistration} (${r.vehicleType})</td>
                        <td>${formatDateTime(r.startTime)}</td>
                        <td><span class="badge ${getStatusBadge(r.status)}">${r.status}</span></td>
                        <td>
                            <button class="btn btn-sm btn-outline-primary" onclick="showQrModal('${r.bookingReference}', '${r.qrCodeBase64}', '${r.slotNumber}', '${r.parkingAreaName}')">
                                <i class="bi bi-qr-code"></i> View QR
                            </button>
                            <a href="/customer/receipt/${r.id}" class="btn btn-sm btn-outline-secondary">
                                <i class="bi bi-receipt"></i>
                            </a>
                        </td>
                    </tr>
                `).join('');
            }
        }

        // Render recent history
        const historyTable = document.getElementById('recentHistoryBody');
        if (historyTable) {
            if (reservations.length === 0) {
                historyTable.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4"><i class="bi bi-inbox fs-3 d-block mb-1"></i>No booking history yet.</td></tr>`;
            } else {
                historyTable.innerHTML = reservations.slice(0, 5).map(r => `
                    <tr>
                        <td><strong>${r.bookingReference}</strong></td>
                        <td>${r.parkingAreaName} (${r.slotNumber})</td>
                        <td>${formatDateTime(r.startTime)}</td>
                        <td><span class="badge ${getStatusBadge(r.status)}">${r.status}</span></td>
                        <td>${formatCurrency(r.actualFee || r.estimatedFee)}</td>
                    </tr>
                `).join('');
            }
        }
    } catch (e) {
        showToast('Failed to load dashboard: ' + e.message, 'danger');
    }
}

// ==========================================
// 2. Interactive Slot Booking Page Logic
// ==========================================
async function initBookingPage() {
    try {
        // Load Parking Areas
        const areaRes = await apiFetch('/api/parking-areas');
        parkingAreasList = areaRes.data || [];
        const areaSelect = document.getElementById('bookingAreaSelect');
        areaSelect.innerHTML = parkingAreasList.map(a => `
            <option value="${a.id}">${a.name} — ${a.address}</option>
        `).join('');

        // Load Customer Vehicles
        const vehRes = await apiFetch('/api/vehicles');
        const vehicles = vehRes.data || [];
        const vehSelect = document.getElementById('bookingVehicleSelect');
        if (vehicles.length === 0) {
            vehSelect.innerHTML = `<option value="">No vehicles found! Please register a vehicle first.</option>`;
        } else {
            vehSelect.innerHTML = vehicles.map(v => `
                <option value="${v.id}" data-type="${v.vehicleType}">${v.registrationNumber} (${v.vehicleType})</option>
            `).join('');
        }

        // Set default start/end times
        const now = new Date();
        now.setMinutes(now.getMinutes() + 10);
        const startTimeStr = formatInputDate(now);
        const endTimeStr = formatInputDate(new Date(now.getTime() + 2 * 60 * 60 * 1000));

        document.getElementById('bookingStartTime').value = startTimeStr;
        document.getElementById('bookingEndTime').value = endTimeStr;

        // Fetch slots
        loadAvailableSlots();

        // Attach listeners
        document.getElementById('bookingAreaSelect').addEventListener('change', loadAvailableSlots);
        document.getElementById('bookingVehicleSelect').addEventListener('change', loadAvailableSlots);
        document.getElementById('bookingStartTime').addEventListener('change', () => {
            loadAvailableSlots();
            updateEstimatedFee();
        });
        document.getElementById('bookingEndTime').addEventListener('change', () => {
            loadAvailableSlots();
            updateEstimatedFee();
        });
    } catch (e) {
        showToast('Error loading booking components: ' + e.message, 'danger');
    }
}

async function loadAvailableSlots() {
    const areaId = document.getElementById('bookingAreaSelect').value;
    if (!areaId) return;

    const vehSelect = document.getElementById('bookingVehicleSelect');
    const selectedVehOpt = vehSelect.options[vehSelect.selectedIndex];
    const vehicleType = selectedVehOpt ? selectedVehOpt.getAttribute('data-type') : null;

    const startTime = document.getElementById('bookingStartTime').value;
    const endTime = document.getElementById('bookingEndTime').value;

    const gridContainer = document.getElementById('parkingSlotGrid');
    gridContainer.innerHTML = `<div class="col-12 text-center py-4"><div class="spinner-border text-primary" role="status"></div><div class="mt-2 text-muted">Checking real-time slot availability...</div></div>`;

    selectedSlotId = null;
    document.getElementById('btnConfirmReservation').disabled = true;

    try {
        let url = `/api/parking-slots/available?areaId=${areaId}`;
        if (vehicleType) url += `&vehicleType=${vehicleType}`;
        if (startTime) url += `&startTime=${startTime}:00`;
        if (endTime) url += `&endTime=${endTime}:00`;

        const res = await apiFetch(url);
        const slots = res.data || [];

        if (slots.length === 0) {
            gridContainer.innerHTML = `<div class="col-12 text-center py-5 text-muted"><i class="bi bi-x-circle fs-2 d-block mb-2"></i>No slots available matching your vehicle type and time criteria.</div>`;
            return;
        }

        gridContainer.innerHTML = slots.map(s => {
            let statusClass = 'slot-available';
            let statusText = 'AVAILABLE';
            let clickable = true;

            if (s.status === 'INACTIVE') {
                statusClass = 'slot-inactive';
                statusText = 'INACTIVE';
                clickable = false;
            } else if (s.status === 'OCCUPIED') {
                statusClass = 'slot-occupied';
                statusText = 'OCCUPIED';
                clickable = false;
            } else if (s.status === 'RESERVED' || !s.currentlyAvailable) {
                statusClass = 'slot-reserved';
                statusText = 'RESERVED';
                clickable = false;
            }

            return `
                <div class="slot-item ${statusClass}" ${clickable ? `onclick="selectSlot(${s.id}, '${s.slotNumber}', ${s.hourlyRate}, this)"` : ''}>
                    <div class="slot-number">${s.slotNumber}</div>
                    <div class="slot-type"><i class="bi ${getVehicleIcon(s.vehicleType)}"></i> ${s.vehicleType}</div>
                    <div class="slot-price">₹${s.hourlyRate}/hr</div>
                    <span class="slot-badge">${statusText}</span>
                </div>
            `;
        }).join('');
    } catch (e) {
        gridContainer.innerHTML = `<div class="col-12 text-danger text-center py-4">Failed to load slots: ${e.message}</div>`;
    }
}

function selectSlot(slotId, slotNumber, hourlyRate, el) {
    document.querySelectorAll('.slot-item').forEach(item => item.classList.remove('selected'));
    el.classList.add('selected');
    selectedSlotId = slotId;
    selectedSlotRate = hourlyRate;

    document.getElementById('selectedSlotDisplay').textContent = slotNumber;
    document.getElementById('btnConfirmReservation').disabled = false;
    updateEstimatedFee();
}

function updateEstimatedFee() {
    if (!selectedSlotId || !selectedSlotRate) {
        document.getElementById('estimatedFeeDisplay').textContent = '₹0.00';
        return;
    }

    const start = new Date(document.getElementById('bookingStartTime').value);
    const end = new Date(document.getElementById('bookingEndTime').value);

    if (isNaN(start) || isNaN(end) || end <= start) {
        document.getElementById('estimatedFeeDisplay').textContent = '₹0.00';
        return;
    }

    const durationMinutes = (end - start) / (1000 * 60);
    const hours = Math.max(1, Math.ceil(durationMinutes / 60));
    const total = (hours * selectedSlotRate).toFixed(2);
    document.getElementById('estimatedFeeDisplay').textContent = `₹${total} (${hours} hr${hours > 1 ? 's' : ''})`;
}

async function confirmReservation() {
    const vehicleId = document.getElementById('bookingVehicleSelect').value;
    const startTime = document.getElementById('bookingStartTime').value;
    const endTime = document.getElementById('bookingEndTime').value;

    if (!vehicleId) {
        showToast('Please select a registered vehicle', 'warning');
        return;
    }
    if (!selectedSlotId) {
        showToast('Please select an available parking slot from the grid', 'warning');
        return;
    }

    const payload = {
        vehicleId: parseInt(vehicleId),
        parkingSlotId: selectedSlotId,
        startTime: startTime,
        endTime: endTime
    };

    const btn = document.getElementById('btnConfirmReservation');
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Reserving...`;

    try {
        const res = await apiFetch('/api/reservations', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        const r = res.data;
        showToast('Reservation confirmed successfully!', 'success');
        showQrModal(r.bookingReference, r.qrCodeBase64, r.slotNumber, r.parkingAreaName, r.id);
        loadAvailableSlots();
    } catch (e) {
        showToast(e.message, 'danger');
    } finally {
        btn.disabled = false;
        btn.innerHTML = `<i class="bi bi-check-circle-fill me-1"></i> Confirm Reservation`;
    }
}

// ==========================================
// 3. Customer Reservations View
// ==========================================
async function initReservationsView() {
    try {
        const res = await apiFetch('/api/reservations/my');
        const list = res.data || [];
        const table = document.getElementById('allReservationsTableBody');

        if (list.length === 0) {
            table.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-5"><i class="bi bi-inbox fs-2 d-block mb-2"></i>You have no parking reservations yet.</td></tr>`;
            return;
        }

        table.innerHTML = list.map(r => `
            <tr>
                <td><strong>${r.bookingReference}</strong></td>
                <td>${r.parkingAreaName}<br><small class="text-muted">Slot: ${r.slotNumber}</small></td>
                <td>${r.vehicleRegistration}</td>
                <td>${formatDateTime(r.startTime)}</td>
                <td>${formatDateTime(r.endTime)}</td>
                <td><span class="badge ${getStatusBadge(r.status)}">${r.status}</span></td>
                <td>
                    <span class="badge ${r.paymentStatus === 'COMPLETED' ? 'bg-success' : 'bg-warning text-dark'}">
                        ${r.paymentStatus || 'PENDING'}
                    </span>
                    <div class="small fw-bold">${formatCurrency(r.actualFee || r.estimatedFee)}</div>
                </td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary" onclick="showQrModal('${r.bookingReference}', '${r.qrCodeBase64}', '${r.slotNumber}', '${r.parkingAreaName}', ${r.id})">
                            <i class="bi bi-qr-code"></i>
                        </button>
                        <a href="/customer/receipt/${r.id}" class="btn btn-outline-secondary" title="View Receipt">
                            <i class="bi bi-receipt"></i>
                        </a>
                        ${(r.status === 'CONFIRMED' || r.status === 'PENDING') ? `
                            <button class="btn btn-outline-danger" onclick="cancelReservation(${r.id})" title="Cancel Booking">
                                <i class="bi bi-x-circle"></i>
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading reservations: ' + e.message, 'danger');
    }
}

async function cancelReservation(id) {
    if (!confirm('Are you sure you want to cancel this reservation? The slot will be released.')) {
        return;
    }

    try {
        await apiFetch(`/api/reservations/${id}/cancel`, { method: 'POST' });
        showToast('Reservation cancelled successfully', 'success');
        initReservationsView();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

// ==========================================
// 4. Vehicles View
// ==========================================
async function initVehiclesView() {
    try {
        const res = await apiFetch('/api/vehicles');
        const list = res.data || [];
        const table = document.getElementById('vehiclesTableBody');

        if (list.length === 0) {
            table.innerHTML = `<tr><td colspan="4" class="text-center text-muted py-4"><i class="bi bi-car-front fs-2 d-block mb-1"></i>No vehicles registered yet. Add one below.</td></tr>`;
            return;
        }

        table.innerHTML = list.map(v => `
            <tr>
                <td><strong>${v.registrationNumber}</strong></td>
                <td><i class="bi ${getVehicleIcon(v.vehicleType)}"></i> ${v.vehicleType}</td>
                <td>${formatDateTime(v.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteVehicle(${v.id})">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading vehicles: ' + e.message, 'danger');
    }
}

async function addVehicle(e) {
    e.preventDefault();
    const regNum = document.getElementById('vehRegistration').value.trim();
    const vehType = document.getElementById('vehType').value;

    if (!regNum) {
        showToast('Please enter registration number', 'warning');
        return;
    }

    try {
        await apiFetch('/api/vehicles', {
            method: 'POST',
            body: JSON.stringify({ registrationNumber: regNum, vehicleType: vehType })
        });
        showToast('Vehicle registered successfully', 'success');
        document.getElementById('addVehicleForm').reset();
        initVehiclesView();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function deleteVehicle(id) {
    if (!confirm('Are you sure you want to delete this vehicle?')) return;
    try {
        await apiFetch(`/api/vehicles/${id}`, { method: 'DELETE' });
        showToast('Vehicle removed successfully', 'success');
        initVehiclesView();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

// ==========================================
// 5. Payments View
// ==========================================
async function initPaymentsView() {
    try {
        const res = await apiFetch('/api/payments/my');
        const list = res.data || [];
        const table = document.getElementById('paymentsTableBody');

        if (list.length === 0) {
            table.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-5"><i class="bi bi-credit-card fs-2 d-block mb-2"></i>No payment transactions found.</td></tr>`;
            return;
        }

        table.innerHTML = list.map(p => `
            <tr>
                <td><strong>${p.transactionReference}</strong></td>
                <td>${p.reservation ? p.reservation.bookingReference : '—'}</td>
                <td>${formatCurrency(p.amount)}</td>
                <td>${p.paymentMethod}</td>
                <td>
                    <span class="badge ${p.paymentStatus === 'COMPLETED' ? 'bg-success' : 'bg-warning text-dark'}">
                        ${p.paymentStatus}
                    </span>
                </td>
                <td>
                    ${p.paymentStatus !== 'COMPLETED' ? `
                        <button class="btn btn-sm btn-primary" onclick="payDemo(${p.reservation ? p.reservation.id : null}, ${p.amount})">
                            <i class="bi bi-credit-card"></i> Pay Now (Demo)
                        </button>
                    ` : `<span class="text-muted small">${formatDateTime(p.paidAt)}</span>`}
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading payments: ' + e.message, 'danger');
    }
}

async function payDemo(reservationId, amount) {
    if (!reservationId) return;
    try {
        await apiFetch('/api/payments/demo', {
            method: 'POST',
            body: JSON.stringify({
                reservationId: reservationId,
                paymentMethod: 'DEMO_PAYMENT',
                amount: amount
            })
        });
        showToast('Payment successful! (Simulated)', 'success');
        initPaymentsView();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

// ==========================================
// Modal & Utility Helpers
// ==========================================
function showQrModal(bookingRef, qrBase64, slotNum, areaName, reservationId) {
    let modalEl = document.getElementById('qrModal');
    if (!modalEl) {
        const modalHtml = `
            <div class="modal fade" id="qrModal" tabindex="-1">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title"><i class="bi bi-qr-code text-primary me-2"></i>Parking Pass & QR Code</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body text-center py-4">
                            <h4 id="qrModalRef" class="fw-bold text-primary mb-2"></h4>
                            <p class="text-muted mb-3" id="qrModalLocation"></p>
                            <div class="qr-container mb-3 shadow-sm">
                                <img id="qrModalImage" src="" alt="QR Code" class="img-fluid">
                            </div>
                            <div class="alert alert-info py-2 small mb-0">
                                <i class="bi bi-info-circle me-1"></i> Present this QR code or booking reference to staff at the parking gate for seamless entry.
                            </div>
                        </div>
                        <div class="modal-footer justify-content-between">
                            <a id="qrModalReceiptLink" href="#" class="btn btn-outline-secondary">
                                <i class="bi bi-printer me-1"></i> View Receipt
                            </a>
                            <button type="button" class="btn btn-primary" data-bs-dismiss="modal">Done</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        modalEl = document.getElementById('qrModal');
    }

    document.getElementById('qrModalRef').textContent = bookingRef;
    document.getElementById('qrModalLocation').textContent = `${areaName} — Slot ${slotNum}`;
    document.getElementById('qrModalImage').src = qrBase64 ? `data:image/png;base64,${qrBase64}` : '';
    const receiptLink = document.getElementById('qrModalReceiptLink');
    if (reservationId) {
        receiptLink.href = `/customer/receipt/${reservationId}`;
        receiptLink.classList.remove('d-none');
    } else {
        receiptLink.classList.add('d-none');
    }

    const modal = new bootstrap.Modal(modalEl);
    modal.show();
}

function getStatusBadge(status) {
    switch (status) {
        case 'CONFIRMED': return 'bg-primary';
        case 'ACTIVE': return 'bg-success';
        case 'COMPLETED': return 'bg-info text-dark';
        case 'CANCELLED': return 'bg-danger';
        case 'PENDING': return 'bg-warning text-dark';
        default: return 'bg-secondary';
    }
}

function getVehicleIcon(type) {
    switch (type) {
        case 'CAR': return 'bi-car-front-fill';
        case 'EV': return 'bi-lightning-charge-fill';
        case 'MOTORCYCLE': return 'bi-bicycle';
        case 'TRUCK': return 'bi-truck';
        default: return 'bi-car-front';
    }
}

function formatInputDate(d) {
    const pad = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
