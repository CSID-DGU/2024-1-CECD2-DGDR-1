import json

# 텍스트 파일 읽기 및 처리
def read_and_process_text_file(file_path):
    lines = []
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            stripped_line = line.strip()
            if stripped_line:  # 비어있지 않은 줄만 리스트에 추가
                lines.append(stripped_line)
    return lines

# JSON 파일로 저장
def save_to_json(data, json_path):
    with open(json_path, 'w', encoding='utf-8') as json_file:
        json.dump({"text": data}, json_file, ensure_ascii=False, indent=4)

# JSON 파일 읽기 (테스트용)
def read_json_file(json_path):
    with open(json_path, 'r', encoding='utf-8') as json_file:
        data = json.load(json_file)
    return data['text']

if __name__ == "__main__":

    # 사용 예
    input_text_path = 'dataset/question.txt'  # 입력 텍스트 파일 경로
    output_json_path = 'dataset/question.json'  # 출력 JSON 파일 경로

    # 파일 처리
    processed_lines = read_and_process_text_file(input_text_path)
    save_to_json(processed_lines, output_json_path)

    # 저장된 JSON 파일 읽기 및 출력
    loaded_text = read_json_file(output_json_path)
    print(loaded_text[:10])
