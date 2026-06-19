/**
 * Dashboard.js
 * 
 * Component Purpose: 
 * The main landing page after a user logs in. It provides an at-a-glance overview of system metrics.
 * 
 * Target User: 
 * Both Students and Admins, but the content dynamically changes based on their Role (Role-based access).
 * 
 * General Workflow:
 * 1. Component mounts and immediately triggers the `useEffect` hook.
 * 2. `useEffect` fetches aggregate statistics from various backend API endpoints.
 * 3. The UI renders high-level summary cards (Dashboard Metrics).
 * 4. Admins see an additional table showing items that are low in stock.
 */
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';

const Dashboard = () => {
  // Authentication context provides current user details and a helper to check if they are an admin
  const { user, isAdmin } = useAuth();
  
  // --- State Variables ---
  // `loading`: True while initial API calls are fetching dashboard metrics. Displays a loading spinner.
  const [loading, setLoading] = useState(true);
  
  // `error`: Stores API failure messages. Displays an alert box if something goes wrong.
  const [error, setError] = useState('');
  
  // `stats`: A unified object containing key numerical metrics displayed in the summary table.
  // Useful because we gather multiple disparate numbers from different API endpoints into one UI construct.
  const [stats, setStats] = useState({
    totalItems: 0,
    lowStock: 0,
    totalRequests: 0,
    pendingRequests: 0,
    myRequests: 0,
  });
  
  // `lowStockItems`: For admins only, stores the detailed list of inventory items that need restocking.
  const [lowStockItems, setLowStockItems] = useState([]);

  /**
   * useEffect Hook
   * 
   * Purpose: Automatically run code when the component first appears (mounts) or if dependencies change.
   * When it runs: Runs once on component mount, and re-runs if `user?.role` changes.
   * Why data is loaded here: A dashboard needs its statistics immediately to be useful. By putting API calls
   * in a useEffect, we ensure the fetch happens automatically without the user having to click anything.
   * What happens if removed: The dashboard would remain blank/show zeros forever, because the data fetch 
   * would never trigger.
   */
  useEffect(() => {
    const loadStats = async () => {
      setLoading(true);
      setError('');

      try {
        // --- API Call 1: Total Inventory Items ---
        // Fetch just 1 item from the inventory but look at the `totalElements` meta-data
        // to determine total items in the database without downloading the entire list.
        const inventoryResponse = await api.get('/api/inventory', {
          params: { page: 0, size: 1, sortBy: 'name' },
        });

        const totalItems = inventoryResponse.data?.totalElements ?? 0;
        let lowStock = 0;
        let lowItems = [];
        let totalRequests = 0;
        let pendingRequests = 0;
        let myRequests = 0;

        // --- Role-Based Data Loading ---
        // We branch logic based on the user's role. 
        // Admins need global request counts and low-stock data.
        // Students only need the count of their own requests.
        if (user?.role === 'ADMIN') {
          // --- API Call 2: Low Stock (Admins) ---
          const lowResponse = await api.get('/api/inventory/low-stock');
          lowItems = lowResponse.data || [];
          lowStock = lowItems.length;

          // --- API Call 3: All Requests (Admins) ---
          const allRequests = await api.get('/api/requests');
          totalRequests = Number(allRequests.data?.length ?? 0);
          pendingRequests = Number(
            allRequests.data?.filter((request) => request.status === 'PENDING')?.length ?? 0
          );
        } else {
          // --- API Call 4: My Requests (Students) ---
          const myResponse = await api.get('/api/requests/my');
          myRequests = Number(myResponse.data?.length ?? 0);
          pendingRequests = Number(
            myResponse.data?.filter((request) => request.status === 'PENDING')?.length ?? 0
          );
        }

        // Update state with all gathered metrics
        setStats({ totalItems, lowStock, totalRequests, pendingRequests, myRequests });
        setLowStockItems(lowItems);
      } catch (err) {
        // Handle failure to fetch dashboard stats
        setError('Unable to load dashboard stats. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    loadStats();
  }, [user?.role]); // Dependency Array: If user role changes, re-fetch the appropriate stats.

  // --- UI Render ---
  return (
    <div className="page-card">
      <div className="page-header">
        <div>
          {/* Greets the user using the username from the JWT token stored in AuthContext */}
          <h1>Welcome back{user?.username ? `, ${user.username}` : ''}</h1>
          <p className="page-subtitle">
            {isAdmin()
              ? 'Here is the current state of inventory and request activity.'
              : 'Here is a quick look at inventory and your requests.'}
          </p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* --- Dashboard Metrics Section ---
          Provides immediate usefulness by summarizing complex data into quick, readable numbers. */}
      <div className="summary-table-wrapper">
        <table className="summary-table">
          <tbody>
            <tr>
              <th>Inventory items</th>
              <td>{stats.totalItems}</td>
            </tr>
            {/* Conditional Rendering: Admins see global metrics, students see personal metrics */}
            {isAdmin() ? (
              <>
                <tr>
                  <th>Low stock items</th>
                  <td>{stats.lowStock}</td>
                </tr>
                <tr>
                  <th>Total requests</th>
                  <td>{stats.totalRequests}</td>
                </tr>
              </>
            ) : (
              <tr>
                <th>My requests</th>
                <td>{stats.myRequests}</td>
              </tr>
            )}
            <tr>
              <th>Pending requests</th>
              <td>{stats.pendingRequests}</td>
            </tr>
          </tbody>
        </table>
      </div>

      {loading && <div className="page-loading"><span className="spin-dot" /> Loading dashboard...</div>}

      {/* --- Low Stock Table (Admins Only) ---
          Alerts administrators to immediate actions needed directly from the dashboard. */}
      {!loading && isAdmin() && (
        <>
          <div className="section-label">Needs Restocking</div>
          {lowStockItems.length ? (
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Item</th>
                    <th>Category</th>
                    <th>Available</th>
                    <th>Minimum</th>
                  </tr>
                </thead>
                <tbody>
                  {lowStockItems.map((item) => (
                    <tr key={item.id}>
                      <td className="cell-strong">{item.name}</td>
                      <td><span className="chip chip-muted">{item.category}</span></td>
                      <td>{item.availableQuantity} {item.unit}</td>
                      <td>
                        {item.minimumQuantity}
                        <span className="low-stock-flag">
                          Low
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-row">
              <strong>All stocked up</strong>
              Every item is currently above its minimum quantity.
            </div>
          )}
        </>
      )}

      {/* --- Quick Actions ---
          Provides easy navigation routes to the most commonly used parts of the app. */}
      <div className="section-label">Quick Actions</div>
      <div className="page-actions">
        {isAdmin() ? (
          <>
            <Link to="/inventory/add" className="btn btn-primary">
              Add item
            </Link>
            <Link to="/requests/manage" className="btn btn-secondary">
              Manage requests
            </Link>
            <Link to="/inventory" className="btn btn-ghost">
              View inventory
            </Link>
          </>
        ) : (
          <>
            <Link to="/requests/new" className="btn btn-primary">
              New request
            </Link>
            <Link to="/requests/my" className="btn btn-secondary">
              My requests
            </Link>
            <Link to="/inventory" className="btn btn-ghost">
              Browse inventory
            </Link>
          </>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
