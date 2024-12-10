import React, { useEffect, useState } from 'react';
import { getApiBaseUrl } from '../Config';
import { Modal, Button, Container } from 'react-bootstrap';
import './DetailInfoModal.css';

const API_BASE_URL = getApiBaseUrl('modal');

const DetailInfoModal = ({ onClose }) => {
  const [hospitalDetails, setHospitalDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchHospitalDetails = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/hospital/emergency`, {
          headers: {
            'Content-Type': 'application/json',
          },
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        const hospitalData = data.find((item) => item.id === 1);
        setHospitalDetails(hospitalData);
        setLoading(false);
      } catch (error) {
        setError(error.message);
        setLoading(false);
      }
    };

    fetchHospitalDetails();
  }, []);

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error: {error}</p>;
  if (!hospitalDetails) return <p>병원 데이터를 찾을 수 없습니다.</p>;

  const equipment = [
    `응급실 성인 병상: 총 ${hospitalDetails.emergencyRoomDefault} 병상 중 ${hospitalDetails.emergencyRoomAvailable} 병상 이용 가능`,
    `응급실 소아 병상: 총 ${hospitalDetails.emergencyRoomChildDefault} 병상 중 ${hospitalDetails.emergencyRoomChildAvailable} 병상 이용 가능`,
    `일반 수술실: ${hospitalDetails.operatingRoom} 병상 이용 가능`,
    `신경외과 수술실: ${hospitalDetails.neurosurgeryRoom} 병상 이용 가능`,
    `신생아 중환자실: ${hospitalDetails.neonatalRoom} 병상 이용 가능`,
    `흉부 외과 중환자실: ${hospitalDetails.thoracicRoom} 병상 이용 가능`,
    `일반 병실: ${hospitalDetails.generalRoom} 병상 이용 가능`,
    `CT: ${hospitalDetails.ct ? '사용 가능' : '사용 불가'}`,
    `MRI: ${hospitalDetails.mri ? '사용 가능' : '사용 불가'}`,
    `혈관촬영기: ${hospitalDetails.angiography ? '사용 가능' : '사용 불가'}`,
    `인공호흡기: ${hospitalDetails.ventilator ? '사용 가능' : '사용 불가'}`,
    `CRRT: ${hospitalDetails.crrt ? '사용 가능' : '사용 불가'}`,
    `ECMO: ${hospitalDetails.ecmo ? '사용 가능' : '사용 불가'}`,
  ];

  return (
    <Modal show centered size="lg" onHide={onClose} className="custom-detail-modal">
      <Modal.Header closeButton>
        <Modal.Title>병원 상세 정보</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Container>
          <h5>병상 현황</h5>
          <ul className="detail-list">
            {equipment.slice(0, 7).map((item, index) => (
              <li key={index}>{item}</li>
            ))}
          </ul>
          
          <h5>장비 현황</h5>
          <ul className="detail-list">
            {equipment.slice(7).map((item, index) => (
              <li key={index}>{item}</li>
            ))}
          </ul>
        </Container>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onClose}>닫기</Button>
      </Modal.Footer>
    </Modal>
  );
};

export default DetailInfoModal;
