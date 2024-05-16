import os
import json
import torch
from transformers import GPT2LMHeadModel, GPT2Tokenizer

def model_fn(model_dir):
    """
    모델 아티팩트를 로드합니다.
    """
    # 모델과 토크나이저 초기화
    
    model = GPT2LMHeadModel.from_pretrained(model_dir)
    tokenizer = GPT2Tokenizer.from_pretrained(model_dir)
    return model, tokenizer

def input_fn(request_body, request_content_type):
    """
    입력 데이터를 처리합니다.
    """
    if request_content_type == 'application/json':
        try:
            data = json.loads(request_body)
            return data['text']
        except KeyError:
            raise ValueError("JSON input does not have 'text' key")
        except json.JSONDecodeError:
            raise ValueError("Input is not valid JSON")
    else:
        raise ValueError(f"Unsupported content type: {request_content_type}")

def predict_fn(input_data, model_and_tokenizer):
    """
    모델을 사용하여 입력 데이터에 대한 예측을 수행합니다.
    """
    model, tokenizer = model_and_tokenizer
    inputs = tokenizer.encode(input_data, return_tensors='pt')
    with torch.no_grad():
        outputs = model.generate(inputs, max_length=50)
    return tokenizer.decode(outputs[0], skip_special_tokens=True)

def output_fn(prediction, content_type):
    """
    출력 데이터를 클라이언트에 반환할 형식으로 변환합니다.
    """
    if content_type == "application/json":
        return json.dumps({'generated_text': prediction})
    else:
        raise ValueError(f"Unsupported content type: {content_type}")
