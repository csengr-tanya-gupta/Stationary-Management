import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import api from '../api/axiosConfig';
import StatusStamp from '../components/StatusStamp';
import './Requests.css';

/**
 * MyRequests Component
 * 
 * Purpose: This page allows a student to view the status and history of all their submitted requests.
 * User: Student
 * Workflow:
 * 1. Student navigates to this page.
 * 2. The component fetches requests specific to the logged-in student.
 * 3. The student can filter their requests by status.
 * 4. The student can click "New Request" to navigate to the request creation page.
 */
const MyRequests = () => {
  // --- State Variables ---
  // `requests`: Stores the array of requests made by the user. Displayed in the table.
  const [requests, setRequests] = useState([]);
  
  // `status`: Tracks the current filter status selected by the student (All, Pending, Approved, Rejected).
  const [status, setStatus] = useState('');
  
  // `loading`: Indicates whether the app is currently fetching requests from the API.
  const [loading, setLoading] = useState(false);
  
  // `error`: Stores any error message encountered during data fetching to display to the user.
  const [error, setError] = useState('');

  /**
   * API Call: Fetch User's Requests
   * Backend Service: GET /api/requests/my
   * Why needed: To retrieve only the requests that belong to the authenticated student.
   * Expected data: Array of request objects associated with the user.
   * Success: Populates the `requests` state.
   * Failure: Sets an error message to notify the user.
   */
  const loadRequests = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await api.get('/api/requests/my', {
        params: status ? { status } : {},
      });
      setRequests(response.data || []);
    } catch (err) {
      setError('Failed to load your requests.');
      setRequests([]);
    } finally {
      setLoading(false);
    }
  };

  /**
   * useEffect Hook: Data Loading
   * Why data is loaded: Automatically fetches the user's data when they arrive on the page.
   * When it runs: On initial component mount and whenever the student changes the `status` filter.
   * What happens if removed: No request data would ever load, leaving an empty table.
   */
  useEffect(() => {
    loadRequests();
  }, [status]);

  return (
    <div className="page-card">
      <div className="page-header">
        <div>
          <h1>My Requests</h1>
          <p className="page-subtitle">Track requests you have submitted and their current status.</p>
        </div>
        <div className="page-actions">
          {/* Action button leading to the Create Request workflow */}
          <Link to="/requests/new" className="btn btn-primary">
            New Request
          </Link>
        </div>
      </div>

      {/* Filter Form: Allows the student to narrow down their request list */}
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

      {/* Error state display */}
      {error && <div className="alert alert-error">{error}</div>}

      {/* 
        Requests Table
        Displayed data:
        - Request ID: For referencing specific requests.
        - Status: Shows if pending, approved, or rejected (includes reason if rejected).
        - Items: Shows the exact stationery items and quantities requested.
        - Reviewed By: Admin user who approved/rejected the request.
        - Updated: The date and time of the last status change.
      */}
      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Request ID</th>
              <th>Status</th>
              <th>Items</th>
              <th>Reviewed By</th>
              <th>Updated</th>
            </tr>
          </thead>
          <tbody>
            {requests.length ? (
              requests.map((request) => (
                <tr key={request.id}>
                  <td className="cell-id">{request.requestId}</td>
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
                  <td>{request.adminUsername || '—'}</td>
                  <td>{request.updatedAt ? new Date(request.updatedAt).toLocaleString() : '—'}</td>
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
                      Submit a new request to see it tracked here.
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

export default MyRequests;
