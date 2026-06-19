import React, { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ArrowLeft, Save } from 'lucide-react';
import api from '../api/axiosConfig';
import './FormPage.css';

/**
 * INVENTORY MANAGEMENT OVERVIEW:
 * Editing an item allows administrators to adjust stock numbers manually or update
 * metadata like categories and names. Keeping minimum and available quantities accurate
 * ensures the low-stock monitoring systems work correctly.
 */

const CATEGORIES = ['PAPER', 'PEN', 'PENCIL', 'NOTEBOOK', 'ERASER', 'MARKER', 'FOLDER', 'STAPLER', 'OTHER'];

const formatCategory = (value) =>
  value ? value.charAt(0) + value.slice(1).toLowerCase() : value;

/**
 * PAGE COMPONENT: EditItem
 * Purpose: Provides a pre-filled form for administrators to modify an existing stationery item.
 * User: Restricted to administrators.
 * Workflow: Extracts the item ID from the URL, fetches its details from the backend, populates the form,
 * allows user edits, and submits the changes to the server.
 */
const EditItem = () => {
  // useParams: Extracts dynamic segments from the URL (e.g., getting the 'id' from /inventory/edit/:id)
  const { id } = useParams();
  const navigate = useNavigate();

  /**
   * STATE VARIABLES:
   * - form: Object holding input values for the item being edited. Pre-populated by the API.
   * - error: Displays issues like failed fetches or submission validation errors.
   * - success: Success banner content shown upon successful update.
   * - loading: Boolean indicating if the update is currently submitting. Disables form elements.
   * - fetching: Boolean indicating if the initial data is being loaded. Used to show a loading spinner instead of an empty form.
   */
  const [form, setForm] = useState({
    name: '',
    category: '',
    unit: '',
    availableQuantity: '',
    minimumQuantity: '',
    description: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);

  /**
   * USE_EFFECT HOOK:
   * Why data is loaded: We need the existing item details to pre-fill the edit form.
   * When it runs: Runs once when the component mounts or if the 'id' parameter changes.
   * What happens if removed: The form would be blank, and the user wouldn't know what they are editing.
   */
  useEffect(() => {
    /**
     * API CALL: Fetch item details
     * Backend service: Inventory Service (GET /api/inventory/{id})
     * Why needed: To retrieve the specific item's data to populate the form fields.
     */
    const loadItem = async () => {
      setFetching(true);
      setError('');
      try {
        const response = await api.get(`/api/inventory/${id}`);
        const item = response.data;
        // Populating the form state with data from the API
        setForm({
          name: item.name || '',
          category: item.category || '',
          unit: item.unit || '',
          availableQuantity: item.availableQuantity ?? '',
          minimumQuantity: item.minimumQuantity ?? '',
          description: item.description || '',
        });
      } catch (err) {
        setError('Unable to load item details.');
      } finally {
        setFetching(false);
      }
    };

    loadItem();
  }, [id]);

  /**
   * EVENT HANDLER: handleChange
   * User action: Typing into any form field.
   * Business process: Updates the respective field in the local 'form' state.
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  /**
   * EVENT HANDLER: handleSubmit
   * User action: Clicking the 'Update Item' button.
   * Business process: Validates that required fields are present, then sends a PUT request
   * with the updated data to the server.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // FORM VALIDATION LOGIC: Ensures quantities are numbers and required strings are not empty.
    if (!form.name || !form.category || !form.unit || form.availableQuantity === '' || form.minimumQuantity === '') {
      setError('Please fill in all required fields.');
      return;
    }

    setLoading(true);
    try {
      /**
       * API CALL: Update Item
       * Backend service: Inventory Service (PUT /api/inventory/{id})
       * Why needed: Sends the modified properties back to the database.
       * Success handling: Displays success message and navigates back to inventory.
       * Failure handling: Displays server-provided error message or generic failure.
       */
      await api.put(`/api/inventory/${id}`, {
        name: form.name,
        category: form.category,
        unit: form.unit,
        availableQuantity: Number(form.availableQuantity),
        minimumQuantity: Number(form.minimumQuantity),
        description: form.description,
      });
      setSuccess('Item updated successfully.');
      setTimeout(() => navigate('/inventory'), 1000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update item.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-card form-card">
      <div className="page-header">
        <div>
          <Link to="/inventory" className="action-link" style={{ marginBottom: '0.6rem' }}>
            <ArrowLeft style={{ width: 14, height: 14 }} /> Back to inventory
          </Link>
          <h1>Edit Inventory Item</h1>
          <p className="page-subtitle">Update the details of this stationery item.</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {/* Conditionally render a loading spinner or the form based on fetching state */}
      {fetching ? (
        <div className="page-loading"><span className="spin-dot" /> Loading item...</div>
      ) : (
        /**
         * FORM: Edit Item Form
         * Purpose: Provides input fields to modify the state properties. Submits data on completion.
         */
        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="form-field">
            <label htmlFor="name">Name <span className="required">*</span></label>
            <input id="name" className="form-input" name="name" value={form.name} onChange={handleChange} disabled={loading} />
          </div>

          <div className="form-field">
            <label htmlFor="category">Category <span className="required">*</span></label>
            <select id="category" className="form-select" name="category" value={form.category} onChange={handleChange} disabled={loading}>
              <option value="">Select a category</option>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>{formatCategory(c)}</option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="unit">Unit <span className="required">*</span></label>
            <input id="unit" className="form-input" name="unit" value={form.unit} onChange={handleChange} disabled={loading} />
          </div>

          <div className="form-field" />

          <div className="form-field">
            <label htmlFor="availableQuantity">Available Quantity <span className="required">*</span></label>
            <input id="availableQuantity" className="form-input" name="availableQuantity" type="number" min="0" value={form.availableQuantity} onChange={handleChange} disabled={loading} />
          </div>

          <div className="form-field">
            <label htmlFor="minimumQuantity">Minimum Quantity <span className="required">*</span></label>
            <input id="minimumQuantity" className="form-input" name="minimumQuantity" type="number" min="0" value={form.minimumQuantity} onChange={handleChange} disabled={loading} />
            <span className="form-hint">Items at or below this level will show a low-stock flag.</span>
          </div>

          <div className="form-field full-width">
            <label htmlFor="description">Description</label>
            <textarea id="description" className="form-textarea" name="description" value={form.description} onChange={handleChange} disabled={loading} />
          </div>

          <div className="form-actions full-width">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              <Save /> {loading ? 'Updating...' : 'Update Item'}
            </button>
            <Link to="/inventory" className="btn btn-ghost">Cancel</Link>
          </div>
        </form>
      )}
    </div>
  );
};

export default EditItem;
