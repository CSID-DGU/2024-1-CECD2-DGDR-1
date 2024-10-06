import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Sidebar from './Mainbody/Sidebar';
import ManualSection from './Mainbody/ManualSection';
import ConversationBox from './Mainbody/ConversationBox';
import HospitalListModal from './Modal/HospitalListModal';
import LoginForm from './Login/Login'; // Login 컴포넌트를 임포트

function App() {
  const [showBedsModal, setShowBedsModal] = useState(false);
  const [savedManuals, setSavedManuals] = useState([]);
  const [selectedManual, setSelectedManual] = useState(null);

  const handleShowBedsModal = () => {
    setShowBedsModal(true);
  };

  const handleCloseBedsModal = () => {
    setShowBedsModal(false);
  };

  const handleSaveManual = (manual) => {
    if (!savedManuals.some(item => item.title === manual.title)) {
      setSavedManuals([...savedManuals, manual]);
    }
  };

  const handleSelectManual = (manual) => {
    setSelectedManual(manual);
  };

  return (
    <Router>
      <Routes>
        {/* /login 경로로 이동하면 Login 컴포넌트를 렌더링 */}
        <Route path="/login" element={<LoginForm />} />

        {/* 기본 경로는 현재 App 컴포넌트를 렌더링 */}
        <Route
          path="/"
          element={
            <div className="app-container">
              <Sidebar savedManuals={savedManuals} onSelectManual={handleSelectManual} />
              <ManualSection
                onShowBedsModal={handleShowBedsModal}
                onSaveManual={handleSaveManual}
                selectedManual={selectedManual}
              />
              <ConversationBox />
              {showBedsModal && <HospitalListModal onClose={handleCloseBedsModal} />}
            </div>
          }
        />

        {/* 그 외의 모든 경로를 기본 경로로 리다이렉트 */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;
