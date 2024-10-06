import React, { useState, useEffect } from 'react';
import './ManualSection.css';
import { Button, Card, Alert } from 'react-bootstrap'; // Bootstrap 컴포넌트 추가
import { getApiBaseUrl } from '../Config';

const API_BASE_URL = getApiBaseUrl();

const ManualSection = ({ onShowBedsModal, onSaveManual, selectedManual, callID }) => { // callID를 추가로 받음
  const [manual, setManual] = useState(null);
  const [cards, setCards] = useState([]);
  const [selectedCardId, setSelectedCardId] = useState(null);
  const [loading, setLoading] = useState(false); // 초기 로딩 상태를 false로 설정
  const [error, setError] = useState(null);

  useEffect(() => {
    if (selectedManual) {
      setManual(selectedManual);
      setSelectedCardId(selectedManual.id);
    }
  }, [selectedManual]);

  // JSON 데이터를 정제하는 함수
  const fetchAndProcessData = async (callID) => {
    try {
      setLoading(true); // 로딩 상태 활성화
      const response = await fetch(`${API_BASE_URL}/manual/${callID}`, { // callID를 경로에 포함
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true', // ngrok 경고를 무시하는 설정
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch data from the API');
      }

      const data = await response.json(); // 응답 데이터를 JSON으로 변환
      const processedData = processData(data); // 데이터를 처리하는 함수 호출
      setCards(processedData); // 상태에 처리된 데이터 저장
      setError(null); // 에러 상태 초기화
    } catch (error) {
      console.error('Fetch error:', error);
      setError(error.message); // 오류 발생 시 에러 상태 저장
    } finally {
      setLoading(false); // 로딩 상태를 종료
    }
  };

  // JSON 데이터를 정제하는 함수
  const processData = (data) => {
    let result = [];
    // Object.keys를 사용하여 data의 모든 passage 키들을 처리
    Object.keys(data).forEach((key, index) => {
      const passageData = data[key]; // 각 passage{i} 데이터를 가져옴
      result.push({
        id: index + 1, // id는 1부터 증가
        passage: passageData.병명, // 병명을 passage로 사용
        script: {
          clinicalFeatures: passageData["임상적 특징"], // 임상적 특징
          patientAssessment: passageData["환자평가 필수항목"], // 환자평가 필수항목
        },
      });
    });
    return result;
  };

  const handleManualButtonClick = () => {
    fetchAndProcessData(callID); // 데이터 가져오기 및 처리
  };

  const handleCardClick = (card) => {
    setSelectedCardId(card.id);
    const selected = {
      title: card.passage,
      content: {
        clinicalFeatures: card.script.clinicalFeatures, // 이미 처리된 script의 임상적 특징
        patientAssessment: card.script.patientAssessment, // 환자평가 필수항목
      },
    };
    setManual(selected);
    onSaveManual(selected);
  };

  const handleDeleteCard = (id) => {
    setCards(cards.filter(card => card.id !== id));
    if (id === selectedCardId) {
      setManual(null);
      setSelectedCardId(null);
    }
  };

  const handleCloseContent = () => {
    setManual(null);
    setSelectedCardId(null);
  };

  return (
    <div className="manual-section">
      <div className="manual-section-header">
        <h2 className="manual-title">
          {manual ? manual.title : '아직 불러온 매뉴얼이 없습니다.'}
        </h2>
        <Button variant="secondary" className="close-button2" onClick={handleCloseContent}>
          닫기
        </Button>
      </div>
      {manual ? (
        <div className="manual-content">
          <h4>임상적 특징</h4>
          <p>{manual.content.clinicalFeatures}</p>
          <div className="manual-content-spacing"></div>
          <h4>환자평가 필수항목</h4>
          <p>{manual.content.patientAssessment}</p>
        </div>
      ) : (
        <Alert variant="info" className="manual-placeholder">
          아직 불러온 매뉴얼이 없습니다.
        </Alert>
      )}
      <div className="buttons">
        <Button variant="primary" onClick={handleManualButtonClick}>
          매뉴얼 확인하기
        </Button>
        <Button variant="danger" onClick={onShowBedsModal}>
          응급 병상 확인하기
        </Button>
      </div>
      <div className="cards">
        {cards.map(card => (
          <Card
            key={card.id}
            className={`card ${card.id === selectedCardId ? 'selected' : ''}`}
            onClick={() => handleCardClick(card)}
          >
          <Card.Body>
            <Card.Title className="card-title">{card.passage}</Card.Title>
            <div className="card-buttons">
              <Button
                variant="outline-danger"
                size="sm"
                onClick={(e) => { e.stopPropagation(); handleDeleteCard(card.id); }}
              >
                <i className="fas fa-trash-alt"></i> 삭제하기
              </Button>
            </div>
          </Card.Body>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default ManualSection;
