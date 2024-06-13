package dgdr.server;

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
    private final ConversationRepository conversationRepository;

    public Map<String, Object> getManual() {
        List<Conversation> conversations = conversationRepository.findAll();
        String formattedTranscript = formatConversations(conversations);

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(Constants.AWS_ACCESS_KEY, Constants.AWS_SECRET_KEY);
        SageMakerRuntimeClient sagemakerRuntimeClient = SageMakerRuntimeClient.builder()
                .region(Constants.REGION)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        String jsonInput = String.format("{\"text\": \"%s\"}", formattedTranscript);

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

        // Save parsed response to a JSON file
        saveResponseToJsonFile(result, "response.json");

        return result;
    }

    private String formatConversations(List<Conversation> conversations) {
        return conversations.stream()
                .map(conversation -> {
                    if (conversation.getCallerId().equals(Constants.PHONE)) {
                        return "상황실: " + conversation.getTranscription();
                    } else {
                        return "신고자: " + conversation.getTranscription();
                    }
                })
                .collect(Collectors.joining("\\n"));
    }

    private Map<String, Object> parseResponse(String responseBody) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            for (int i = 0; i <= 5; i++) {
                String passageKey = "passage" + i;
                String simKey = "sim" + i;
                if (rootNode.has(passageKey) && rootNode.has(simKey)) {
                    Map<String, Object> passageSimMap = new HashMap<>();
                    passageSimMap.put("passage", rootNode.get(passageKey).asText());
                    passageSimMap.put("sim", rootNode.get(simKey).asDouble());
                    responseMap.put(String.valueOf(i), passageSimMap);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response", e);
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
