import React, { useState } from 'react';
import './Sidebar.css';

const Sidebar = ({ savedManuals, onSelectManual }) => {
  const [protocols, setProtocols] = useState([
    { id: 1, text: 'Text', completed: false },
    { id: 2, text: 'Text', completed: false },
    { id: 3, text: 'Text', completed: false },
    { id: 4, text: 'Text', completed: false },
  ]);

  const toggleProtocol = (id) => {
    setProtocols(protocols.map(protocol =>
      protocol.id === id ? { ...protocol, completed: !protocol.completed } : protocol
    ));
  };

  return (
    <div className="sidebar">
      <div className="manual-list">
        <h3>열람 매뉴얼 목록</h3>
        {savedManuals.map((manual, index) => (
          <div
            key={index}
            className="saved-manual-item"
            onClick={() => onSelectManual(manual)}
          >
            {manual.title}
          </div>
        ))}
      </div>
      <div className="protocol-list">
        <h3>수보 프로토콜 목록</h3>
        {protocols.map(protocol => (
          <div
            key={protocol.id}
            className={`protocol-item ${protocol.completed ? 'completed' : 'pending'}`}
            onClick={() => toggleProtocol(protocol.id)}
          >
            {protocol.text}
          </div>
        ))}
      </div>
    </div>
  );
};

export default Sidebar;
