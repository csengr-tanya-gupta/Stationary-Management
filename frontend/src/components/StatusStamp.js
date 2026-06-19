import React from 'react';

const CONFIG = {
  PENDING: { className: 'stamp-pending', label: 'Pending' },
  APPROVED: { className: 'stamp-approved', label: 'Approved' },
  REJECTED: { className: 'stamp-rejected', label: 'Rejected' },
};

const StatusStamp = ({ status }) => {
  const config = CONFIG[status] || { className: '', label: status || 'Unknown' };
  const { className, label } = config;

  return <span className={`stamp ${className}`}>{label}</span>;
};

export default StatusStamp;
