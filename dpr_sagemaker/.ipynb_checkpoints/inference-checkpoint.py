import os
import json

import subprocess
import sys



#!pip install -r requirements.txt

import torch
import transformers

def model_fn(model_dir):
    # Conda 패키지 설치
    subprocess.run("conda install faiss-gpu -c pytorch -y", shell=True, check=True)
    subprocess.run("conda install mkl=2021 -y", shell=True, check=True)

    # requirements.txt에 있는 패키지를 pip으로 설치
    subprocess.run(f"{sys.executable} -m pip install -r requirements.txt", shell=True, check=True)

    # KoBERT 토크나이저를 git에서 직접 설치
    subprocess.run(f"{sys.executable} -m pip install git+https://github.com/SKTBrain/KoBERT.git#egg=kobert_tokenizer&subdirectory=kobert_hf", shell=True, check=True)
    """
    모델 아티팩트를 로드합니다.
    """
    # 모델과 토크나이저 초기화
    
    # model = GPT2LMHeadModel.from_pretrained(model_dir)
    # tokenizer = GPT2Tokenizer.from_pretrained(model_dir)
    # return model, tokenizer
    from dpr_data import KorQuadDataset
    from retriever import KorDPRRetriever
    from indexers import DenseFlatIndexer
    from encoder import KobertBiEncoder
    model = KobertBiEncoder()
    model.load(f"{model_dir}/checkpoint/my_model.pt")
    model.eval()
    #valid_dataset = KorQuadDataset("dataset/KorQuAD_v1.0_dev.json")
    index = DenseFlatIndexer()
    index.deserialize(path=f"{model_dir}/2050iter_flat")
    valid_dataset = KorQuadDataset("dataset/KorQuAD_v1.0_dev.json")
    retriever = KorDPRRetriever(model=model, valid_dataset=valid_dataset, index=index)
    ## valdataset는 tokenizer load를 위해서 필요하니 지우지 말것.
    
    return {"model_dir": model_dir,"retriever":retriever}

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

def predict_fn(input_data, model_data):
    """
    모델을 사용하여 입력 데이터에 대한 예측을 수행합니다.
    """
    # model, tokenizer = model_and_tokenizer
    # inputs = tokenizer.encode(input_data, return_tensors='pt')
    # with torch.no_grad():
    #     outputs = model.generate(inputs, max_length=50)
    # return tokenizer.decode(outputs[0], skip_special_tokens=True)
    k=6 # TODO: BE랑 얘기하기.
    # query = input_data
    try:
        # subprocess를 사용하여 retriever.py 스크립트를 실행하고 결과를 캡처합니다.
        # result = subprocess.run(
        #     ['python', f'{model_dir_str}/retriever.py', '-q', query, '-k', str(k)],
        #     capture_output=True,
        #     text=True,
        #     check=True
        # )
        # # 스크립트의 출력을 반환합니다.
        # return result.stdout
        
        result=model_data["retriever"].retrieve(query=input_data, k=k)
        # subprocess.run(
        #     ['python', f'{model_data["model_dir"]}/retriever.py', '-q', query, '-k', str(k)],
        #     capture_output=True,
        #     text=True,
        #     check=True
        # )
        # 스크립트의 출력을 반환합니다.
        return result
    except subprocess.CalledProcessError as e:
        print(f"Error executing retriever.py: {e}")
        return None

def output_fn(prediction, content_type):
    """
    출력 데이터를 클라이언트에 반환할 형식으로 변환합니다.
    """
    if content_type == "application/json":
        return json.dumps(prediction)
    else:
        raise ValueError(f"Unsupported content type: {content_type}")
