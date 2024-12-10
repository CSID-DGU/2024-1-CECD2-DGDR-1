// App.js
import 'bootstrap/dist/css/bootstrap.min.css';
import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Sidebar from './Mainbody/Sidebar';
import ManualSection from './Mainbody/ManualSection';
import ConversationBox from './Mainbody/ConversationBox';
import HospitalListModal from './Modal/HospitalListModal';
import LoginForm from './Login/Login';
import SignUpForm from './Login/Signup';
import CallHistory from './Store/CallHistory';
import NavbarForm from './Navbar';

function App() {
  const [showBedsModal, setShowBedsModal] = useState(false);
  const [savedManuals, setSavedManuals] = useState([]);
  const [selectedManual, setSelectedManual] = useState(null);
  const [callID, setCallID] = useState(null); // callID 상태 추가
  const [userName, setUserName] = useState('정재욱');

  const handleShowBedsModal = () => setShowBedsModal(true);
  const handleCloseBedsModal = () => setShowBedsModal(false);

  const handleSaveManual = (manual) => {
    if (!savedManuals.some(item => item.title === manual.title)) {
      setSavedManuals([...savedManuals, manual]);
    }
  };

  const handleSelectManual = (manual) => {
    setSelectedManual(manual);
  };

  // onCallIDUpdate 함수 정의: callID 상태를 업데이트
  const onCallIDUpdate = (newCallID) => {
    setCallID(newCallID);
  };

  // 단축키 기능 추가 (Ctrl + 1: 매뉴얼 확인, Ctrl + 2: 응급 병상 확인)
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.ctrlKey && event.key === '1') {
        document.getElementById('manual-check-button').click();
      } else if (event.ctrlKey && event.key === '2') {
        handleShowBedsModal();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <Router>
      <NavbarForm userName={userName} onShowBedsModal={handleShowBedsModal} />
      <Routes>
        <Route path="/login" element={<LoginForm />} />
        <Route path="/signup" element={<SignUpForm />} />
        <Route path="/history" element={<CallHistory />} />
        <Route
          path="/"
          element={
            <div className="app-container">
              <Sidebar savedManuals={savedManuals} onSelectManual={handleSelectManual} />
              <ManualSection
                onSaveManual={handleSaveManual}
                selectedManual={selectedManual}
                callID={callID} // callID를 ManualSection에 전달
              />
              <ConversationBox onCallIDUpdate={onCallIDUpdate} /> {/* onCallIDUpdate를 ConversationBox에 전달 */}
              {showBedsModal && <HospitalListModal onClose={handleCloseBedsModal} />}
            </div>
          }
        />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;
