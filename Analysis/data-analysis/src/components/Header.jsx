// src/components/Header.jsx
import React from 'react';

const Header = ({ title }) => {
  return (
    <header style={{ padding: '20px 0', marginBottom: '20px' }}>
      <h1>{title}</h1>
    </header>
  );
};

export default Header;
