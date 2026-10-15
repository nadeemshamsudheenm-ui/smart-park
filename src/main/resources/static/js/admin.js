// SMART PARK — Admin Portal JavaScript

let occupancyChartInstance = null;
let revenueChartInstance = null;

async function initAdminDashboard() {
    try {
        const res = await apiFetch('/api/admin/dashboard');
        const d = res.data;

        document.getElementById('adminStatAreas').textContent = d.totalParkingAreas || 0;
        document.getElementById('adminStatSlots').textContent = d.totalSlots || 0;
        document.getElementById('adminStatAvailable').textContent = d.availableSlots || 0;
        document.getElementById('adminStatOccupied').textContent = d.occupiedSlots || 0;
        document.getElementById('adminStatReserved').textContent = d.reservedSlots || 0;
        document.getElementById('adminStatSessions').textContent = d.activeParkingSessions || 0;
        document.getElementById('adminStatBookings').textContent = d.totalCompletedBookings || 0;
        document.getElementById('adminStatRevenue').textContent = formatCurrency(d.totalRevenue);

        // Load charts
        loadOccupancyChart();
        loadRevenueChart();
    } catch (e) {
        showToast('Error loading admin dashboard: ' + e.message, 'danger');
    }
}

async function loadOccupancyChart() {
    const ctx = document.getElementById('adminOccupancyChart');
    if (!ctx) return;

    try {
        const res = await apiFetch('/api/admin/reports/occupancy');
        const data = res.data;

        if (occupancyChartInstance) {
            occupancyChartInstance.destroy();
        }

        occupancyChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [
                    {
                        label: 'Available Slots',
                        data: data.availableSlots,
                        backgroundColor: '#10b981'
                    },
                    {
                        label: 'Occupied/Reserved Slots',
                        data: data.occupiedSlots,
                        backgroundColor: '#ef4444'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { stacked: true },
                    y: { stacked: true, beginAtZero: true }
                }
            }
        });
    } catch (e) {
        console.error('Occupancy chart error:', e);
    }
}

async function loadRevenueChart() {
    const ctx = document.getElementById('adminRevenueChart');
    if (!ctx) return;

    try {
        const res = await apiFetch('/api/admin/reports/revenue');
        const data = res.data;

        if (revenueChartInstance) {
            revenueChartInstance.destroy();
        }

        revenueChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Daily Revenue (₹)',
                    data: data.amounts,
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });
    } catch (e) {
        console.error('Revenue chart error:', e);
    }
}

// ==========================================
// Areas Management
// ==========================================
async function initAreasView() {
    try {
        const res = await apiFetch('/api/parking-areas/all');
        const list = res.data || [];
        const table = document.getElementById('adminAreasTableBody');

        table.innerHTML = list.map(a => `
            <tr>
                <td><strong>${a.id}</strong></td>
                <td><span class="fw-bold">${a.name}</span><br><small class="text-muted">${a.address}</small></td>
                <td>${a.openingTime} – ${a.closingTime}</td>
                <td><span class="badge bg-primary">${a.availableSlots} / ${a.totalSlots} Avail</span></td>
                <td><span class="badge ${a.active ? 'bg-success' : 'bg-secondary'}">${a.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="openEditAreaModal(${JSON.stringify(a).replace(/"/g, '&quot;')})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteArea(${a.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading areas: ' + e.message, 'danger');
    }
}

async function saveArea(e) {
    e.preventDefault();
    const id = document.getElementById('areaId').value;
    const payload = {
        name: document.getElementById('areaName').value.trim(),
        address: document.getElementById('areaAddress').value.trim(),
        description: document.getElementById('areaDescription').value.trim(),
        openingTime: document.getElementById('areaOpeningTime').value + ':00',
        closingTime: document.getElementById('areaClosingTime').value + ':00',
        active: document.getElementById('areaActive').checked
    };

    try {
        if (id) {
            await apiFetch(`/api/parking-areas/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            showToast('Parking area updated successfully', 'success');
        } else {
            await apiFetch('/api/parking-areas', { method: 'POST', body: JSON.stringify(payload) });
            showToast('Parking area created successfully', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('areaModal')).hide();
        initAreasView();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

function openEditAreaModal(a) {
    document.getElementById('areaModalTitle').textContent = a ? 'Edit Parking Area' : 'New Parking Area';
    document.getElementById('areaId').value = a ? a.id : '';
    document.getElementById('areaName').value = a ? a.name : '';
    document.getElementById('areaAddress').value = a ? a.address : '';
    document.getElementById('areaDescription').value = a ? (a.description || '') : '';
    document.getElementById('areaOpeningTime').value = a ? a.openingTime.substring(0, 5) : '06:00';
    document.getElementById('areaClosingTime').value = a ? a.closingTime.substring(0, 5) : '23:00';
    document.getElementById('areaActive').checked = a ? a.active : true;

    new bootstrap.Modal(document.getElementById('areaModal')).show();
}

async function deleteArea(id) {
    if (!confirm('Are you sure you want to delete this parking area? All associated slots will also be removed.')) return;
    try {
        await apiFetch(`/api/parking-areas/${id}`, { method: 'DELETE' });
        showToast('Parking area deleted', 'success');
        initAreasView();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

// ==========================================
// Slots Management
// ==========================================
async function initSlotsView() {
    try {
        // Load areas for filter dropdown
        const areaRes = await apiFetch('/api/parking-areas/all');
        const areas = areaRes.data || [];
        const filter = document.getElementById('slotAreaFilter');
        filter.innerHTML = `<option value="">All Parking Areas</option>` + areas.map(a => `<option value="${a.id}">${a.name}</option>`).join('');

        const modalArea = document.getElementById('slotModalArea');
        modalArea.innerHTML = areas.map(a => `<option value="${a.id}">${a.name}</option>`).join('');

        loadSlotsTable();
        filter.addEventListener('change', loadSlotsTable);
    } catch (e) {
        showToast('Error initializing slots view: ' + e.message, 'danger');
    }
}

async function loadSlotsTable() {
    const areaId = document.getElementById('slotAreaFilter').value;
    const url = areaId ? `/api/parking-slots/area/${areaId}` : '/api/parking-slots';

    try {
        const res = await apiFetch(url);
        const slots = res.data || [];
        const table = document.getElementById('adminSlotsTableBody');

        table.innerHTML = slots.map(s => `
            <tr>
                <td><strong>${s.slotNumber}</strong></td>
                <td>${s.parkingAreaName}</td>
                <td>${s.vehicleType}</td>
                <td>₹${parseFloat(s.hourlyRate).toFixed(2)}/hr</td>
                <td><span class="badge ${getStatusBadge(s.status)}">${s.status}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="openEditSlotModal(${JSON.stringify(s).replace(/"/g, '&quot;')})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteSlot(${s.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading slots: ' + e.message, 'danger');
    }
}

function openEditSlotModal(s) {
    document.getElementById('slotModalTitle').textContent = s ? 'Edit Slot' : 'Add Parking Slot';
    document.getElementById('slotId').value = s ? s.id : '';
    document.getElementById('slotModalArea').value = s ? s.parkingAreaId : '';
    document.getElementById('slotNumberInput').value = s ? s.slotNumber : '';
    document.getElementById('slotVehicleType').value = s ? s.vehicleType : 'CAR';
    document.getElementById('slotHourlyRate').value = s ? s.hourlyRate : '5.00';
    document.getElementById('slotStatusSelect').value = s ? s.status : 'AVAILABLE';

    new bootstrap.Modal(document.getElementById('slotModal')).show();
}

async function saveSlot(e) {
    e.preventDefault();
    const id = document.getElementById('slotId').value;
    const payload = {
        parkingAreaId: parseInt(document.getElementById('slotModalArea').value),
        slotNumber: document.getElementById('slotNumberInput').value.trim(),
        vehicleType: document.getElementById('slotVehicleType').value,
        hourlyRate: parseFloat(document.getElementById('slotHourlyRate').value),
        status: document.getElementById('slotStatusSelect').value
    };

    try {
        if (id) {
            await apiFetch(`/api/parking-slots/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            showToast('Slot updated successfully', 'success');
        } else {
            await apiFetch('/api/parking-slots', { method: 'POST', body: JSON.stringify(payload) });
            showToast('Slot created successfully', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('slotModal')).hide();
        loadSlotsTable();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function deleteSlot(id) {
    if (!confirm('Are you sure you want to delete this parking slot?')) return;
    try {
        await apiFetch(`/api/parking-slots/${id}`, { method: 'DELETE' });
        showToast('Slot removed', 'success');
        loadSlotsTable();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

// ==========================================
// Users & Staff Management
// ==========================================
async function initUsersView() {
    try {
        const res = await apiFetch('/api/admin/users');
        const users = res.data || [];
        const table = document.getElementById('adminUsersTableBody');

        table.innerHTML = users.map(u => `
            <tr>
                <td><strong>${u.id}</strong></td>
                <td><span class="fw-bold">${u.fullName}</span></td>
                <td>${u.email}</td>
                <td>${u.phone || '—'}</td>
                <td>
                    <select class="form-select form-select-sm d-inline-block w-auto" onchange="changeUserRole(${u.id}, this.value)">
                        <option value="CUSTOMER" ${u.role === 'CUSTOMER' ? 'selected' : ''}>CUSTOMER</option>
                        <option value="STAFF" ${u.role === 'STAFF' ? 'selected' : ''}>STAFF</option>
                        <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                    </select>
                </td>
                <td>
                    <span class="badge ${u.enabled ? 'bg-success' : 'bg-danger'}">${u.enabled ? 'ACTIVE' : 'DISABLED'}</span>
                </td>
                <td>
                    <button class="btn btn-sm ${u.enabled ? 'btn-outline-danger' : 'btn-outline-success'}" onclick="toggleUserStatus(${u.id}, ${!u.enabled})">
                        ${u.enabled ? '<i class="bi bi-person-x"></i> Disable' : '<i class="bi bi-person-check"></i> Enable'}
                    </button>
                    ${u.role === 'STAFF' ? `
                        <button class="btn btn-sm btn-outline-primary" onclick="openStaffAssignModal(${u.id}, '${u.fullName}')">
                            <i class="bi bi-geo-alt"></i> Assign Area
                        </button>
                    ` : ''}
                </td>
            </tr>
        `).join('');

        // Preload areas into staff assign modal
        const areaRes = await apiFetch('/api/parking-areas/all');
        const areas = areaRes.data || [];
        document.getElementById('assignModalAreaSelect').innerHTML = areas.map(a => `<option value="${a.id}">${a.name}</option>`).join('');
    } catch (e) {
        showToast('Error loading users: ' + e.message, 'danger');
    }
}

async function toggleUserStatus(id, newStatus) {
    try {
        await apiFetch(`/api/admin/users/${id}/status?enabled=${newStatus}`, { method: 'PUT' });
        showToast(`User status updated to ${newStatus ? 'Active' : 'Disabled'}`, 'success');
        initUsersView();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

async function changeUserRole(id, newRole) {
    try {
        await apiFetch(`/api/admin/users/${id}/role?role=${newRole}`, { method: 'PUT' });
        showToast(`User role updated to ${newRole}`, 'success');
        initUsersView();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

function openStaffAssignModal(userId, name) {
    document.getElementById('assignStaffUserId').value = userId;
    document.getElementById('assignStaffUserName').textContent = name;
    new bootstrap.Modal(document.getElementById('staffAssignModal')).show();
}

async function submitStaffAssignment() {
    const staffId = document.getElementById('assignStaffUserId').value;
    const areaId = document.getElementById('assignModalAreaSelect').value;

    try {
        await apiFetch(`/api/admin/staff-assignments?staffUserId=${staffId}&parkingAreaId=${areaId}`, { method: 'POST' });
        showToast('Staff member assigned to parking area successfully', 'success');
        bootstrap.Modal.getInstance(document.getElementById('staffAssignModal')).hide();
    } catch (e) {
        showToast(e.message, 'danger');
    }
}

// ==========================================
// All Reservations View (Admin)
// ==========================================
async function initAdminReservationsView() {
    try {
        const res = await apiFetch('/api/admin/reservations');
        const list = res.data || [];
        const table = document.getElementById('adminReservationsTableBody');

        table.innerHTML = list.map(r => `
            <tr>
                <td><strong>${r.bookingReference}</strong></td>
                <td>${r.userFullName} (${r.userEmail})</td>
                <td>${r.vehicleRegistration} <span class="badge bg-secondary">${r.vehicleType}</span></td>
                <td>${r.parkingAreaName} — Slot ${r.slotNumber}</td>
                <td>${formatDateTime(r.startTime)}</td>
                <td>${formatDateTime(r.endTime)}</td>
                <td><span class="badge ${getStatusBadge(r.status)}">${r.status}</span></td>
                <td>${formatCurrency(r.actualFee || r.estimatedFee)}</td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading reservations: ' + e.message, 'danger');
    }
}

// ==========================================
// Audit Logs View (Admin)
// ==========================================
async function initAuditLogsView() {
    try {
        const res = await apiFetch('/api/admin/audit-logs');
        const list = res.data || [];
        const table = document.getElementById('auditLogsTableBody');

        table.innerHTML = list.map(l => `
            <tr>
                <td><strong>${l.id}</strong></td>
                <td>${l.user ? l.user.email : 'SYSTEM'}</td>
                <td><span class="badge bg-dark">${l.action}</span></td>
                <td>${l.details || '—'}</td>
                <td><small class="text-muted">${formatDateTime(l.createdAt)}</small></td>
            </tr>
        `).join('');
    } catch (e) {
        showToast('Error loading audit logs: ' + e.message, 'danger');
    }
}
