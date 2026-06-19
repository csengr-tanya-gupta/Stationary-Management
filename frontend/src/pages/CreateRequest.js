import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Plus, Trash2, Send, ClipboardList } from 'lucide-react';
import api from '../api/axiosConfig';
import './FormPage.css';

/**
 * CreateRequest Component
 * 
 * Purpose: This page allows students to select items from inventory and create a new stationery request.
 * User: Student
 * Workflow:
 * 1. Student navigates to "Create Request".
 * 2. System fetches available inventory to populate the item dropdowns.
 * 3. Student adds one or more rows of items and specifies quantities.
 * 4. System validates the request (e.g., quantities must be > 0 and <= available stock).
 * 5. Student submits the request to the backend.
 * 6. On success, student is redirected back to the "My Requests" page.
 */
const CreateRequest = () => {
  // --- State Variables ---
  // `items`: Stores all available inventory items fetched from the backend. Used to populate the dropdown select options.
  const [items, setItems] = useState([]);
  
  // `requestItems`: An array of objects representing the rows the user is adding to their request (e.g., [{itemId: 1, itemName: 'Pen', quantity: 2}]).
  const [requestItems, setRequestItems] = useState([]);
  
  // `error`: Stores any validation or API errors to show to the user.
  const [error, setError] = useState('');
  
  // `success`: Stores a success message when the request is created.
  const [success, setSuccess] = useState('');
  
  // `loading`: Disables buttons and form inputs while data is fetching or submitting to prevent duplicate submissions.
  const [loading, setLoading] = useState(false);
  
  // `navigate`: React Router hook to programmatically redirect the user to another page (e.g., after successful submission).
  const navigate = useNavigate();

  /**
   * useEffect Hook: Inventory Data Loading
   * Why data is loaded: We need to know what stationery items exist so the user can choose from them.
   * When it runs: Only once when the component initially mounts (empty dependency array []).
   * What happens if removed: Dropdowns would have no options, making it impossible to create a request.
   */
  useEffect(() => {
    const loadItems = async () => {
      try {
        // Fetch inventory to show in item dropdowns
        const response = await api.get('/api/inventory', { params: { page: 0, size: 100, sortBy: 'name' } });
        setItems(response.data.content || []);
      } catch (err) {
        setError('Failed to load inventory items.');
      }
    };

    loadItems();
  }, []);

  /**
   * Helper Function: findItem
   * Purpose: Finds the full item object from the `items` state based on its ID.
   */
  const findItem = (itemId) => items.find((item) => String(item.id) === String(itemId));

  /**
   * Event Handler: Add Item Row
   * User action: Clicks the "Add Item" button.
   * Business process: Adds a new, blank row to the request form.
   */
  const addItem = () => {
    setRequestItems((prev) => [...prev, { itemId: '', itemName: '', quantity: 1 }]);
  };

  /**
   * Event Handler: Handle Item Selection or Quantity Change
   * User action: Selects an item from the dropdown or changes the quantity input.
   * Business process: Updates the specific row in the `requestItems` array. Also auto-fills the itemName for easier payload creation.
   */
  const handleItemChange = (index, field, value) => {
    setRequestItems((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      
      // Auto-fill item name when an item ID is selected
      if (field === 'itemId') {
        const selected = findItem(value);
        next[index].itemName = selected?.name || '';
      }
      return next;
    });
  };

  /**
   * Event Handler: Remove Item Row
   * User action: Clicks the trash can icon next to a row.
   * Business process: Removes that specific row from the form.
   */
  const removeItem = (index) => {
    setRequestItems((prev) => prev.filter((_, idx) => idx !== index));
  };

  /**
   * Form Validation Logic: Check Stock
   * Purpose: Ensures a user cannot request more of an item than what is currently available in the inventory.
   */
  const exceedsStock = (row) => {
    const item = findItem(row.itemId);
    return !!item && Number(row.quantity || 0) > item.availableQuantity;
  };

  /**
   * API Call & Event Handler: Submit Request Form
   * User action: Clicks "Submit Request".
   * Validation logic: 
   * 1. Must have at least one item.
   * 2. All items must have an ID and a valid quantity >= 1.
   * 3. Requested quantity must not exceed available stock.
   * Backend Service: POST /api/requests
   * Expected data: Payload array containing item IDs and quantities.
   * Success: Shows a success message and redirects to the MyRequests page.
   * Failure: Displays error from the backend.
   */
  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevents default form browser refresh behavior
    setError('');
    setSuccess('');

    // Validation: Empty request
    if (!requestItems.length) {
      setError('Add at least one item to create a request.');
      return;
    }

    // Build the payload for the API
    const payload = requestItems.map((row) => ({
      itemId: Number(row.itemId),
      itemName: row.itemName,
      quantity: Number(row.quantity),
    }));

    // Validation: Incomplete rows or invalid quantities
    if (payload.some((row) => !row.itemId || row.quantity < 1)) {
      setError('Select valid items and quantities.');
      return;
    }

    // Validation: Stock availability
    if (requestItems.some(exceedsStock)) {
      setError('One or more items exceed the currently available stock. Adjust quantities to continue.');
      return;
    }

    setLoading(true);
    try {
      await api.post('/api/requests', { items: payload });
      setSuccess('Request created successfully.');
      // Wait a moment for the user to read the success message, then navigate away
      setTimeout(() => navigate('/requests/my'), 1200);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create request.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-card form-card">
      <div className="page-header">
        <div>
          <h1>Create Request</h1>
          <p className="page-subtitle">Submit a new stationery request from available inventory.</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="request-toolbar">
        <button type="button" className="btn btn-secondary" onClick={addItem}>
          <Plus /> Add Item
        </button>
      </div>

      {/* Form Section: Allows the user to construct their request */}
      <form onSubmit={handleSubmit}>
        {requestItems.length ? (
          <div className="request-rows">
            {requestItems.map((row, index) => {
              const selected = findItem(row.itemId);
              const warning = exceedsStock(row);
              return (
                <div className="request-row" key={index}>
                  <div>
                    {/* Item Selection Dropdown */}
                    <select
                      className="form-select"
                      value={row.itemId}
                      onChange={(e) => handleItemChange(index, 'itemId', e.target.value)}
                      disabled={loading}
                    >
                      <option value="">Select item</option>
                      {items.map((item) => (
                        <option key={item.id} value={item.id}>
                          {item.name} ({item.availableQuantity} {item.unit} available)
                        </option>
                      ))}
                    </select>
                    {/* Display stock warnings dynamically if the user requests too many */}
                    {selected && (
                      <div className={`request-stock-note ${warning ? 'is-warning' : ''}`}>
                        {warning
                          ? `Only ${selected.availableQuantity} ${selected.unit} available`
                          : `${selected.availableQuantity} ${selected.unit} in stock`}
                      </div>
                    )}
                  </div>
                  
                  {/* Quantity Input */}
                  <input
                    type="number"
                    min="1"
                    className="form-input"
                    value={row.quantity}
                    onChange={(e) => handleItemChange(index, 'quantity', e.target.value)}
                    disabled={loading}
                    placeholder="Qty"
                  />
                  
                  {/* Remove Row Button */}
                  <button
                    type="button"
                    className="btn btn-danger btn-icon"
                    disabled={loading}
                    onClick={() => removeItem(index)}
                    aria-label="Remove item"
                  >
                    <Trash2 />
                  </button>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="request-empty">
            <ClipboardList style={{ width: 28, height: 28, color: 'var(--lav-400)', marginBottom: '0.5rem' }} />
            <strong>No items added yet</strong>
            Click "Add Item" above to start building your request.
          </div>
        )}

        <div className="form-actions" style={{ marginTop: '1.25rem' }}>
          <button type="submit" className="btn btn-primary" disabled={loading || !requestItems.length}>
            <Send /> {loading ? 'Submitting...' : 'Submit Request'}
          </button>
          <Link to="/requests/my" className="btn btn-ghost">Cancel</Link>
        </div>
      </form>
    </div>
  );
};

export default CreateRequest;
