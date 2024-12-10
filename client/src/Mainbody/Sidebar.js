import React, { useState, useEffect } from 'react';
import './Sidebar.css';

const Sidebar = ({ savedManuals, onSelectManual }) => {
  const [protocols, setProtocols] = useState([
    { id: 1, text: '환자 상태 확인', completed: false },
    { id: 2, text: '현장 조건 파악', completed: false },
    { id: 3, text: '초동조치 지도', completed: false },
    { id: 4, text: '출동 확인 완료', completed: false },
  ]);

  const toggleProtocol = (id) => {
    setProtocols(protocols.map(protocol =>
      protocol.id === id ? { ...protocol, completed: !protocol.completed } : protocol
    ));
  };

  useEffect(() => {
    const handleKeydown = (e) => {
      if (e.altKey) {
        const protocolNumber = parseInt(e.key);
        if (protocolNumber >= 1 && protocolNumber <= protocols.length) {
          toggleProtocol(protocolNumber);
        }
      }
    };

    window.addEventListener('keydown', handleKeydown);
    return () => {
      window.removeEventListener('keydown', handleKeydown);
    };
  }, [protocols]);

  return (
    <div className="sidebar">
      <div className="manual-list">
        <h3 className="top-title">열람 매뉴얼 목록</h3>
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
