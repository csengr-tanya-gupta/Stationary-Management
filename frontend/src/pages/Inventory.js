import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, X, Plus, Pencil, Trash2, AlertTriangle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';
import './Inventory.css';

/**
 * INVENTORY MANAGEMENT OVERVIEW:
 * This page acts as the central hub for stock tracking. It allows users to browse the catalog,
 * while administrators can monitor low-stock items and manage (add/edit/delete) inventory records.
 */

const CATEGORIES = ['PAPER', 'PEN', 'PENCIL', 'NOTEBOOK', 'ERASER', 'MARKER', 'FOLDER', 'STAPLER', 'OTHER'];

// Formats category strings for display (e.g., "PAPER" becomes "Paper")
const formatCategory = (value) =>
  value ? value.charAt(0) + value.slice(1).toLowerCase() : value;

/**
 * PAGE COMPONENT: Inventory
 * Purpose: Displays the inventory catalog with search, filter, and pagination.
 * User: Admins can manage items. Normal users can only view.
 * Workflow: Data is loaded on mount. Users can search or filter, updating the displayed list.
 */
const Inventory = () => {
  const { isAdmin } = useAuth();
  const admin = isAdmin();

  /**
   * STATE VARIABLES:
   * Why they exist & UI impact:
   * - items: Array of objects representing the stationery items. Rendered in the table.
   * - search: Text currently typed in the search input field.
   * - appliedSearch: The actual search term submitted. Triggers data reloading.
   * - category: Selected category filter. Alters the API endpoint called to fetch specific items.
   * - sortBy: Field to sort by. Controls the order of the table rows.
   * - page: Current page number (0-indexed). Controls which slice of data is shown.
   * - size: Number of items per page.
   * - total: Total items available. Used to disable "Next" button when at the end.
   * - loading: Boolean showing if an API request is in progress. Shows a loading message in the table.
   * - error, actionError, actionSuccess: Strings for displaying feedback banners to the user.
   */
  const [items, setItems] = useState([]);
  const [search, setSearch] = useState('');
  const [appliedSearch, setAppliedSearch] = useState('');
  const [category, setCategory] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');

  /**
   * API CALL: loadItems
   * Backend service: Interacts with the Inventory Service.
   * Why needed: To fetch the current state of stock from the database.
   * Expected data: An array or a paginated object of items.
   * Success/failure handling: On success, populates the 'items' state. On failure, sets the 'error' state to show a message.
   */
  const loadItems = async () => {
    setLoading(true);
    setError('');

    try {
      if (appliedSearch) {
        // Fetch items matching the search keyword
        const response = await api.get('/api/inventory/search', { params: { keyword: appliedSearch } });
        setItems(response.data || []);
        setTotal(response.data?.length ?? 0);
      } else if (category) {
        // Fetch items filtered by category
        const response = await api.get(`/api/inventory/category/${category}`, { params: { page, size } });
        setItems(response.data?.content || []);
        setTotal(response.data?.totalElements ?? 0);
      } else {
        // Fetch all items with pagination and sorting
        const response = await api.get('/api/inventory', { params: { page, size, sortBy } });
        setItems(response.data?.content || []);
        setTotal(response.data?.totalElements ?? 0);
      }
    } catch (err) {
      setError('Failed to load inventory.');
      setItems([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  };

  /**
   * USE_EFFECT HOOK:
   * Why data is loaded: Automatically triggers a fetch whenever filtering, sorting, or pagination changes.
   * When it runs: On initial component mount and whenever dependencies (page, size, category, sortBy, appliedSearch) change.
   * What happens if removed: The table would never populate with data, rendering the page empty.
   */
  useEffect(() => {
    loadItems();
  }, [page, size, category, sortBy, appliedSearch]);

  /**
   * EVENT HANDLER: handleSearch
   * User action: Submitting the search form.
   * Business process: Resets pagination and category filters, then sets the appliedSearch state to trigger a fresh data load.
   */
  const handleSearch = (e) => {
    e.preventDefault();
    if (!search.trim()) return;
    setCategory('');
    setPage(0);
    setAppliedSearch(search.trim());
  };

  /**
   * EVENT HANDLER: clearSearch
   * User action: Clicking the clear button next to the search bar.
   * Business process: Removes search filters and resets pagination to show the default list again.
   */
  const clearSearch = () => {
    setSearch('');
    setAppliedSearch('');
    setPage(0);
  };

  const handleCategoryChange = (e) => {
    setCategory(e.target.value);
    setSearch('');
    setAppliedSearch('');
    setPage(0);
  };

  const handleSortChange = (e) => {
    setSortBy(e.target.value);
    setPage(0);
  };

  /**
   * EVENT HANDLER: handleDelete
   * User action: Admin clicking the 'Delete' button on a specific item.
   * Business process: Prompts for confirmation. If confirmed, sends a DELETE request to the backend.
   * If successful, reloads the table data to reflect the removal.
   */
  const handleDelete = async (item) => {
    if (!window.confirm(`Delete "${item.name}"? This action cannot be undone.`)) return;
    setActionError('');
    setActionSuccess('');
    try {
      await api.delete(`/api/inventory/${item.id}`);
      setActionSuccess(`"${item.name}" was deleted.`);
      loadItems(); // Refresh the list
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to delete item.');
    }
  };

  // Helper to determine if an item is low on stock
  const isLow = (item) => item.availableQuantity <= item.minimumQuantity;
  
  // Admins see extra columns for Minimum Quantity and Actions
  const columnCount = admin ? 6 : 4;

  return (
    <div className="page-card">
      <div className="page-header">
        <div>
          <h1>Inventory</h1>
          <p className="page-subtitle">
            {admin
              ? 'Browse, filter, and manage the stationery catalog.'
              : 'Browse available stationery items in the catalog.'}
          </p>
        </div>
        {admin && (
          <div className="page-actions">
            <Link to="/inventory/add" className="btn btn-primary">
              <Plus /> Add New Item
            </Link>
          </div>
        )}
      </div>

      {/**
       * FORM: Toolbar Form
       * Purpose: Wraps the search input and submit button so pressing Enter triggers the search.
       * Validation: Empty searches are ignored in handleSearch.
       */}
      <form className="toolbar" onSubmit={handleSearch}>
        <div className="input-search-wrap">
          <Search />
          <input
            type="text"
            placeholder="Search items by name"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="input-search"
          />
        </div>
        <button type="submit" className="btn btn-secondary">Search</button>
        {appliedSearch && (
          <button type="button" className="btn btn-ghost btn-sm" onClick={clearSearch}>
            <X /> Clear
          </button>
        )}

        <div className="field-control">
          <label htmlFor="category-filter">Category</label>
          <select
            id="category-filter"
            className="select-control"
            value={category}
            onChange={handleCategoryChange}
          >
            <option value="">All Categories</option>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>{formatCategory(c)}</option>
            ))}
          </select>
        </div>

        <div className="field-control">
          <label htmlFor="sort-by">Sort By</label>
          <select
            id="sort-by"
            className="select-control"
            value={sortBy}
            onChange={handleSortChange}
            disabled={!!category || !!appliedSearch}
            title={category || appliedSearch ? 'Clear filters to change sort order' : undefined}
          >
            <option value="name">Name</option>
            <option value="category">Category</option>
            <option value="availableQuantity">Availability</option>
          </select>
        </div>
      </form>

      {error && <div className="alert alert-error">{error}</div>}
      {actionError && <div className="alert alert-error">{actionError}</div>}
      {actionSuccess && <div className="alert alert-success">{actionSuccess}</div>}

      {/**
       * TABLE: Data Table
       * Displayed data: Lists the stationery items fetched from the backend.
       * Column importance: Name and Category identify the item. Available shows stock tracking.
       * Min Qty (Admin only) is crucial for low-stock monitoring.
       */}
      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Item</th>
              <th>Category</th>
              <th>Available</th>
              {admin && <th>Min Qty</th>}
              <th>Description</th>
              {admin && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {items.length ? (
              items.map((item) => (
                <tr key={item.id}>
                  <td className="cell-strong">{item.name}</td>
                  <td><span className="chip chip-muted">{formatCategory(item.category)}</span></td>
                  <td>
                    {item.availableQuantity} {item.unit}
                    {/* Low-stock monitoring alert for admins */}
                    {admin && isLow(item) && (
                      <span className="low-stock-flag"><AlertTriangle /> Low</span>
                    )}
                  </td>
                  {admin && <td>{item.minimumQuantity}</td>}
                  <td>{item.description || '—'}</td>
                  {admin && (
                    <td className="action-cell">
                      <Link to={`/inventory/edit/${item.id}`} className="action-link">
                        <Pencil style={{ width: 14, height: 14 }} /> Edit
                      </Link>
                      <button
                        type="button"
                        className="action-link"
                        style={{ color: 'var(--clay-600)' }}
                        onClick={() => handleDelete(item)}
                      >
                        <Trash2 style={{ width: 14, height: 14 }} /> Delete
                      </button>
                    </td>
                  )}
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={columnCount} className="empty-row">
                  {loading ? (
                    'Loading items...'
                  ) : (
                    <>
                      <strong>No items found</strong>
                      Try a different search term or category.
                    </>
                  )}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination controls for navigating through pages of data */}
      {!appliedSearch && (
        <div className="pagination-controls">
          <button
            className="pagination-btn"
            disabled={page === 0}
            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
          >
            Previous
          </button>
          <span>Page {page + 1} • {total} items</span>
          <button
            className="pagination-btn"
            disabled={(page + 1) * size >= total}
            onClick={() => setPage((prev) => prev + 1)}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default Inventory;
