import React, { useEffect, useState } from 'react';

import api from '../api/axiosConfig';
import StatusStamp from '../components/StatusStamp';
import './Requests.css';

/**
 * ManageRequests Component
 * 
 * Purpose: This page allows administrators to manage all stationery requests submitted by students.
 * User: Admin
 * Workflow:
 * 1. Admin views the list of pending requests.
 * 2. Admin can filter requests by status.
 * 3. Admin reviews the requested items.
 * 4. Admin clicks "Approve" to accept the request, or "Reject" to deny it.
 * 5. The backend updates the request status and inventory (if approved).
 * 6. The UI refreshes to show the updated list.
 */
const ManageRequests = () => {
  // --- State Variables ---
  // `requests`: Array storing the list of requests fetched from the backend. Displayed in the table.
  const [requests, setRequests] = useState([]);
  
  // `status`: Tracks the current filter status selected by the admin (All, Pending, Approved, Rejected).
  const [status, setStatus] = useState('');
  
  // `loading`: Boolean flag indicating if requests are currently being fetched. Disables UI to prevent duplicate actions.
  const [loading, setLoading] = useState(false);
  
  // `error`: Stores any error messages (e.g., failed to load, failed to approve). Displayed to the admin.
  const [error, setError] = useState('');
  
  // `message`: Stores success messages (e.g., "Request approved successfully"). Displayed to the admin.
  const [message, setMessage] = useState('');
  
  // `rejectingId`: Stores the ID of the request currently being rejected. Triggers the display of the rejection reason input field.
  const [rejectingId, setRejectingId] = useState(null);
  
  // `rejectionReason`: Stores the reason typed by the admin for rejecting a request.
  const [rejectionReason, setRejectionReason] = useState('');
  
  // `busyId`: Stores the ID of the request currently being processed (approved/rejected) to disable buttons and prevent double-clicks.
  const [busyId, setBusyId] = useState(null);

  /**
   * API Call: Fetch requests
   * Backend Service: GET /api/requests
   * Why needed: To display the list of requests to the admin.
   * Expected data: Array of request objects.
   * Success: Updates `requests` state.
   * Failure: Sets `error` state to inform the admin.
   */
  const loadRequests = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await api.get('/api/requests', {
        params: status ? { status } : {},
      });
      setRequests(response.data || []);
    } catch (err) {
      setError('Failed to load requests.');
      setRequests([]);
    } finally {
      setLoading(false);
    }
  };

  /**
   * useEffect Hook: Data Loading
   * Why data is loaded: We need to show requests when the page loads or when the filter changes.
   * When it runs: On initial component mount and whenever the `status` filter changes.
   * What happens if removed: The page would be blank and never fetch the list of requests.
   */
  useEffect(() => {
    loadRequests();
  }, [status]);

  /**
   * API Call / Event Handler: Update request status
   * Backend Service: PUT /api/requests/{id}/{action} (action is 'approve' or 'reject')
   * Why needed: To persist the admin's decision to the database.
   * Success: Shows a success message, clears rejection states, and reloads the request list to reflect changes.
   * Failure: Shows an error message if the backend rejects the action (e.g., insufficient inventory).
   */
  const updateRequest = async (id, action, body = {}) => {
    setMessage('');
    setError('');
    setBusyId(id);
    try {
      await api.put(`/api/requests/${id}/${action}`, body);
      setMessage(`Request ${action}ed successfully.`);
      setRejectingId(null);
      setRejectionReason('');
      loadRequests(); // Refresh UI after successful update
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${action} request.`);
    } finally {
      setBusyId(null);
    }
  };

  /**
   * Event Handler: Start Rejection Process
   * User action: Admin clicks "Reject" button.
   * Business process: Opens an inline form to ask for a rejection reason before finalizing.
   */
  const startReject = (id) => {
    setRejectingId(id);
    setRejectionReason('');
    setMessage('');
    setError('');
  };

  /**
   * Event Handler: Confirm Rejection
   * User action: Admin clicks "Confirm" after typing a reason.
   * Business process: Calls `updateRequest` with the 'reject' action and the provided reason.
   */
  const confirmReject = (id) => {
    updateRequest(id, 'reject', { rejectionReason: rejectionReason.trim() || 'Rejected by admin' });
  };

  return (
    <div className="page-card">
      <div className="page-header">
        <div>
          <h1>Manage Requests</h1>
          <p className="page-subtitle">Review and approve or reject student stationery requests.</p>
        </div>
      </div>

      {/* Filter Form: Allows admin to view requests by specific status */}
      <div className="toolbar">
        <div className="field-control">
          <label htmlFor="status-filter">Filter by status</label>
          <select id="status-filter" className="select-control" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">All</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>
      </div>

      {/* Status Messages */}
      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-error">{error}</div>}

      {/* 
        Requests Table
        Displayed data:
        - Request ID: Unique identifier for tracking.
        - Student: Who requested the items.
        - Status: Current state of the request.
        - Items: List of stationery items and quantities requested.
        - Actions: Buttons to approve or reject (only visible for PENDING requests).
      */}
      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Request ID</th>
              <th>Student</th>
              <th>Status</th>
              <th>Items</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {requests.length ? (
              requests.map((request) => (
                <tr key={request.id}>
                  <td className="cell-id">{request.requestId}</td>
                  <td className="cell-strong">{request.studentUsername}</td>
                  <td>
                    <StatusStamp status={request.status} />
                    {request.status === 'REJECTED' && request.rejectionReason && (
                      <span className="request-meta">"{request.rejectionReason}"</span>
                    )}
                  </td>
                  <td>
                    <div className="chip-list">
                      {request.items?.map((item, idx) => (
                        <span className="chip chip-muted" key={idx}>{item.itemName} × {item.quantity}</span>
                      ))}
                    </div>
                  </td>
                  <td className="action-cell">
                    {rejectingId === request.id ? (
                      /* Inline form for rejection reason validation */
                      <div className="reject-form">
                        <input
                          type="text"
                          placeholder="Reason for rejection"
                          value={rejectionReason}
                          onChange={(e) => setRejectionReason(e.target.value)}
                          autoFocus
                        />
                        <button className="btn btn-sm btn-danger" disabled={busyId === request.id} onClick={() => confirmReject(request.id)}>
                          Confirm
                        </button>
                        <button className="btn btn-sm btn-ghost" disabled={busyId === request.id} onClick={() => setRejectingId(null)}>
                          Cancel
                        </button>
                      </div>
                    ) : request.status === 'PENDING' ? (
                      <>
                        {/* Approve and Reject Buttons trigger business workflow actions */}
                        <button className="btn btn-sm btn-accent" disabled={busyId === request.id} onClick={() => updateRequest(request.id, 'approve')}>
                          Approve
                        </button>
                        <button className="btn btn-sm btn-danger" disabled={busyId === request.id} onClick={() => startReject(request.id)}>
                          Reject
                        </button>
                      </>
                    ) : (
                      <span style={{ color: 'var(--text-muted)' }}>—</span>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="empty-row">
                  {loading ? (
                    'Loading requests...'
                  ) : (
                    <>
                      <strong>No requests found</strong>
                      Requests submitted by students will appear here.
                    </>
                  )}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ManageRequests;
