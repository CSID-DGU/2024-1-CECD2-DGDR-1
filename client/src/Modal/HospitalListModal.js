import React, { useState, useEffect } from 'react';
import { getApiBaseUrl } from '../Config';
import './HospitalListModal.css';
import DetailInfoModal from './DetailInfoModal';
import { Container, Row, Col, Button, Form, Modal, Spinner } from 'react-bootstrap';

const API_BASE_URL = getApiBaseUrl('modal');

const HospitalListModal = ({ onClose }) => {
  const [hospitals, setHospitals] = useState([]);
  const [selectedHospital, setSelectedHospital] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [location, setLocation] = useState('');

  const fetchHospitals = async (inputLocation) => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/hospital/search`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ location: inputLocation }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      const transformedData = data
        .filter((item) => item.location.includes('서울'))
        .map((item, index) => ({
          pid: item.pid,
          name: item.name,
          location: item.location,
          call: item.call,
          time: `${(index + 2) * 15}m`,
        }));

      setHospitals(transformedData);
      setLoading(false);
    } catch (error) {
      setError(error.message);
      setLoading(false);
      setShowErrorModal(true);
    }
  };

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
    setShowErrorModal(false);
    onClose();
  };

  if (loading) return <Spinner animation="border" />;

  if (showErrorModal) {
    return (
      <Modal show={showErrorModal} onHide={closeErrorModal} centered>
        <Modal.Header closeButton>
          <Modal.Title>서버 오류</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>서버로부터 응답이 없습니다.</p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="danger" onClick={closeErrorModal}>
            확인
          </Button>
        </Modal.Footer>
      </Modal>
    );
  }

  return (
    <Modal show centered size="lg" onHide={onClose} className="custom-hospital-modal" backdrop="static">
      <Modal.Header closeButton>
        <Modal.Title>이송병원 현황</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Container>
          <Row className="mb-3">
            <Col xs={9}>
              <Form.Control
                type="text"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                placeholder="예: 서울특별시 중구 필동로1길 30"
                className="custom-location-input"
              />
            </Col>
            <Col xs={3}>
              <Button variant="primary" onClick={handleSearchClick} block className="custom-search-btn">
                검색
              </Button>
            </Col>
          </Row>
          <Row>
            {hospitals.length > 0 ? (
              hospitals.map((hospital) => (
                <Col xs={12} className="mb-3 custom-hospital-item" key={hospital.pid}>
                  <div className="hospital-item">
                    <div className="hospital-name">{hospital.name}</div>
                    <div className="hospital-info">
                      <span>위치: {hospital.location}</span>
                      <br />
                      <span>전화번호: {hospital.call}</span>
                    </div>
                    <Button variant="info" onClick={() => handleInfoClick(hospital)} className="info-button">
                      ℹ️
                    </Button>
                  </div>
                </Col>
              ))
            ) : (
              <p>병원을 검색해주세요.</p>
            )}
          </Row>
        </Container>
      </Modal.Body>

      {/* 상세 정보 모달 */}
      {selectedHospital && <DetailInfoModal onClose={closeDetailModal} />}
    </Modal>
  );
};

export default HospitalListModal;
