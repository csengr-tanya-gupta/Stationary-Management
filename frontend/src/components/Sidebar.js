import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Sidebar.css';

const ADMIN_LINKS = [
  { path: '/dashboard', label: 'Dashboard' },
  { path: '/inventory', label: 'Inventory' },
  { path: '/inventory/add', label: 'Add Item' },
  { path: '/requests/manage', label: 'Manage Requests' },
];

const STUDENT_LINKS = [
  { path: '/dashboard', label: 'Dashboard' },
  { path: '/inventory', label: 'Inventory' },
  { path: '/requests/new', label: 'New Request' },
  { path: '/requests/my', label: 'My Requests' },
];

const Sidebar = ({ open, onClose }) => {
  const { user, logout, isAdmin } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const links = isAdmin() ? ADMIN_LINKS : STUDENT_LINKS;
  const isActive = (path) => location.pathname === path;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <>
      <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
        <div className="sidebar-top">
          <Link to="/dashboard" className="sidebar-brand" onClick={onClose}>
            <div className="brand-text">
              <span className="brand-name">Stationery</span>
              <span className="brand-subtitle">Campus Supply Hub</span>
            </div>
          </Link>
          <button className="sidebar-close" onClick={onClose} aria-label="Close menu">
            Close
          </button>
        </div>

        <nav className="sidebar-nav">
          {links.map((link) => {
            const active = isActive(link.path);
            return (
              <Link
                key={link.path}
                to={link.path}
                className={`sidebar-link ${active ? 'active' : ''}`}
                onClick={onClose}
              >
                <span className="sidebar-link-bar" />
                <span>{link.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <span className="user-avatar">
              {user?.username?.charAt(0).toUpperCase() || 'U'}
            </span>
            <div className="user-details">
              <span className="user-name">{user?.username}</span>
              <span className={`chip chip-role-${user?.role?.toLowerCase()}`}>
                {user?.role}
              </span>
            </div>
          </div>
          <button className="sidebar-logout" onClick={handleLogout} title="Log out">
            <span>Log out</span>
          </button>
        </div>
      </aside>

      {open && <div className="sidebar-overlay" onClick={onClose} />}
    </>
  );
};

export default Sidebar;
