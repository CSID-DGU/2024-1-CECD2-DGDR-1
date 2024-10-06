import 'bootstrap/dist/css/bootstrap.min.css';
import { Button, Navbar, Container } from 'react-bootstrap';
import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Sidebar from './Mainbody/Sidebar';
import ManualSection from './Mainbody/ManualSection';
import ConversationBox from './Mainbody/ConversationBox';
import HospitalListModal from './Modal/HospitalListModal';
import LoginForm from './Login/Login';
import SignUpForm from './Login/Signup';
import CallHistory from './Store/CallHistory';
import NavbarForm from './Navbar'; // CustomNavbar 임포트

function App() {
  const [showBedsModal, setShowBedsModal] = useState(false);
  const [savedManuals, setSavedManuals] = useState([]);
  const [selectedManual, setSelectedManual] = useState(null);
  const [userName, setUserName] = useState('정재욱'); // 사용자 이름 추가 (예시)
  const [callID, setCallID] = useState(null); // callID 상태 추가

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

  // ConversationBox에서 callID를 업데이트하는 함수
  const handleCallIDUpdate = (newCallID) => {
    setCallID(newCallID);
  };

  return (
    <Router>
      {/* 네비게이션 바를 모든 페이지에서 표시 */}
      <NavbarForm userName={userName} /> {/* 사용자 이름 전달 */}
      <Routes>
        {/* /login 경로로 이동하면 Login 컴포넌트를 렌더링 */}
        <Route path="/login" element={<LoginForm />} />
        <Route path="/signup" element={<SignUpForm />} />
        <Route path="/history" element={<CallHistory />} />
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
                callID={callID}
              />
              <ConversationBox onCallIDUpdate={handleCallIDUpdate} /> {/* ConversationBox에서 callID 업데이트 */}
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
