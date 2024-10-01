import React, { useState, useEffect } from 'react';
import './HospitalListModal.css';
import DetailInfoModal from './DetailInfoModal';

const API_BASE_URL = 'http://whitex.iptime.org:4000';

const HospitalListModal = ({ onClose }) => {
  const [hospitals, setHospitals] = useState([]);
  const [selectedHospital, setSelectedHospital] = useState(null);
  const [loading, setLoading] = useState(false); // Loading should be initially false
  const [error, setError] = useState(null);
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [location, setLocation] = useState(''); // Input state for location

  // Fetch hospitals based on user-inputted location
  const fetchHospitals = async (inputLocation) => {
    setLoading(true); // Start loading
    try {
      const response = await fetch(`${API_BASE_URL}/hospital/search`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ location: inputLocation }), // Use inputLocation from the input field
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();

      // Filter hospitals with "서울" in the location and transform data
      const transformedData = data
        .filter((item) => item.location.includes('서울')) // Filter for 서울 only
        .map((item, index) => ({
          pid: item.pid,
          name: item.name,
          location: item.location,
          call: item.call,
          time: `${(index + 2) * 15}m`, // Example time format
        }));

      setHospitals(transformedData);
      setLoading(false);
    } catch (error) {
      setError(error.message);
      setLoading(false);
      setShowErrorModal(true); // Show error modal on failure
    }
  };

  // Handle search button click
  const handleSearchClick = () => {
    if (location.trim() !== '') {
      fetchHospitals(location);
    }
  };

  const handleInfoClick = (hospital) => {
    setSelectedHospital(hospital);
  };

  const closeDetailModal = () => {
    setSelectedHospital(null);
  };

  const closeErrorModal = () => {
    setShowErrorModal(false); // Close error modal
    onClose(); // Close the main modal
  };

  if (loading) return <p>Loading...</p>;

  // Render error modal if showErrorModal is true
  if (showErrorModal) {
    return (
      <div className="modal-overlay">
        <div className="error-modal">
          <div className="error-modal-content">
            <button className="close-button" onClick={closeErrorModal}>✖</button>
            <h2>서버 오류</h2>
            <p>서버로부터 응답이 없습니다.</p>
            <button className="retry-button" onClick={closeErrorModal}>확인</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="modal-overlay">
      {selectedHospital ? (
        <DetailInfoModal onClose={closeDetailModal} />
      ) : (
        <div className="modal-content">
          <button className="close-button" onClick={onClose}>✖</button>
          <h1>이송병원 현황</h1>
          <div className="current-location-container">
            <div className="current-location">
              <span className="location-label">현재 위치:</span>
              {/* Input field for location */}
              <input
                type="text"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                placeholder="예: 서울특별시 중구 필동로1길 30"
                className="location-input"
              />
            </div>
            <button className="refresh-button" onClick={handleSearchClick}>
              검색
            </button>
          </div>
          <div className="hospital-list">
            {hospitals.map((hospital) => (
              <div key={hospital.pid} className="hospital-item">
                <div className="hospital-name">{hospital.name}</div>
                <div className="hospital-info">
                  <div className="hospital-time">소요 시간: {hospital.time}</div>
                  <div className="hospital-location">위치: {hospital.location}</div>
                  <div className="hospital-call">전화번호: {hospital.call}</div>
                </div>
                <button className="info-button" onClick={() => handleInfoClick(hospital)}>ℹ️</button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default HospitalListModal;
