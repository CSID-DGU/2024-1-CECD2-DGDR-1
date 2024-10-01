import React, { useState, useEffect } from 'react';
import './ManualSection.css';

const ManualSection = ({ onShowBedsModal, onSaveManual, selectedManual }) => {
  const [manual, setManual] = useState(null);
  const [cards, setCards] = useState([]);
  const [selectedCardId, setSelectedCardId] = useState(null);

  useEffect(() => {
    if (selectedManual) {
      setManual(selectedManual);
      setSelectedCardId(selectedManual.id);
    }
  }, [selectedManual]);

  const handleManualButtonClick = () => {
    const dummyCards = [
      { id: 1, title: '추천 매뉴얼 1', keywords: '키워드1, 키워드2' },
      { id: 2, title: '추천 매뉴얼 2', keywords: '키워드3, 키워드4' },
      { id: 3, title: '추천 매뉴얼 3', keywords: '키워드5, 키워드6' },
    ];
    setCards(dummyCards);
  };

  const handleCardClick = (card) => {
    setSelectedCardId(card.id);
    const selected = {
      title: card.title,
      keywords: card.keywords,
      content: '이것은 더미 매뉴얼 내용입니다.',
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
      <h2>{manual ? manual.title : '아직 불러온 매뉴얼이 없습니다.'}</h2>
      <button className="close-button2" onClick={handleCloseContent}>닫기</button>
      {manual ? (
        <div className="manual-content">
          <p className="manual-keywords"><strong>핵심 키워드:</strong> {manual.keywords}</p>
          <p>{manual.content}</p>
        </div>
      ) : (
        <div className="manual-placeholder">
          <p>아직 불러온 매뉴얼이 없습니다.</p>
        </div>
      )}
      <div className="buttons">
        <button onClick={handleManualButtonClick}>매뉴얼 확인하기</button>
        <button onClick={onShowBedsModal}>응급 병상 확인하기</button>
      </div>
      <div className="cards">
        {cards.map(card => (
          <div
            key={card.id}
            className={`card ${card.id === selectedCardId ? 'selected' : ''}`}
            onClick={() => handleCardClick(card)}
          >
            <h3>{card.title}</h3>
            <p>{card.keywords}</p>
            <div className="card-buttons">
              <button onClick={(e) => { e.stopPropagation(); handleDeleteCard(card.id); }}>
                <i className="fas fa-trash-alt"></i> 삭제하기
              </button>
              <button onClick={(e) => { e.stopPropagation(); onSaveManual(card); }}>
                <i className="fas fa-save"></i> 저장하기
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ManualSection;
