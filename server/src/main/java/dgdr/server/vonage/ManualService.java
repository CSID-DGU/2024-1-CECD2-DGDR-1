package dgdr.server.vonage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;
import software.amazon.awssdk.services.sagemakerruntime.model.SageMakerRuntimeException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManualService {
    private final CallService callService;

    public Map<String, Object> getManual(Long callId) {
        List<CallRecordDto> conversations = callService.getCallRecord(callId);
        String formattedTranscript = formatConversations(conversations);

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(Constants.AWS_ACCESS_KEY, Constants.AWS_SECRET_KEY);
        SageMakerRuntimeClient sagemakerRuntimeClient = SageMakerRuntimeClient.builder()
                .region(Constants.REGION)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        String jsonInput = String.format("{\"text\": \"%s\"}", formattedTranscript);
//        System.out.println("jsonInput = " + formattedTranscript);

        InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                .endpointName(Constants.ENDPOINT_NAME)
                .contentType("application/json")
                .body(SdkBytes.fromString(jsonInput, StandardCharsets.UTF_8))
                .build();

        InvokeEndpointResponse response;
        try {
            response = sagemakerRuntimeClient.invokeEndpoint(request);
        } catch (SageMakerRuntimeException e) {
            throw new RuntimeException("Error invoking SageMaker endpoint", e);
        }

        String responseBody = response.body().asUtf8String();
        Map<String, Object> result = parseResponse(responseBody);
//        String decodedResponseBody = StringEscapeUtils.unescapeJava(responseBody);
//        System.out.println("result = " + decodedResponseBody);

        // Save parsed response to a JSON file
        saveResponseToJsonFile(result, "response.json");

        return result;
    }

    private String formatConversations(List<CallRecordDto> callRecordDtos) {
        return callRecordDtos.stream()
                .map(callRecord -> {
                    if (callRecord.getSpeakerPhoneNumber().equals(Constants.PHONE)) {
                        return "상황실: " + callRecord.getTranscription();
                    } else {
                        return "신고자: " + callRecord.getTranscription();
                    }
                })
                .collect(Collectors.joining("\\n"));
    }

    private Map<String, Object> parseResponse(String responseBody) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // 각 passage를 순회하면서 "passage", "임상적 특징", "환자평가 필수항목" 추출
            for (int i = 0; i <= 5; i++) {
                String passageKey = "passage" + i;  // passage key
                String scriptKey = "script" + i;    // script key
                String simKey = "sim" + i;          // 유사도 key

                if (rootNode.has(passageKey) && rootNode.has(scriptKey) && rootNode.has(simKey)) {
                    Map<String, Object> formattedMap = new HashMap<>();
                    formattedMap.put("병명", rootNode.get(passageKey).asText());  // passage 저장
                    formattedMap.put("임상적 특징", rootNode.get(scriptKey).get("임상적 특징").asText());  // 임상적 특징 저장
                    formattedMap.put("환자평가 필수항목", rootNode.get(scriptKey).get("환자평가 필수항목").asText());  // 환자평가 필수항목 저장
                    formattedMap.put("유사도", rootNode.get(simKey).asDouble());  // 유사도 저장

                    responseMap.put("passage" + i, formattedMap);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("응답을 파싱하는 중 오류 발생", e);
        }
        return responseMap;
    }



    private void saveResponseToJsonFile(Map<String, Object> data, String filename) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(filename), data);
        } catch (Exception e) {
            throw new RuntimeException("Error saving response to JSON file", e);
        }
    }
}
