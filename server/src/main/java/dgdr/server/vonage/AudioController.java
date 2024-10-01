package dgdr.server.vonage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import reactor.core.publisher.Mono;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

@Controller
public class AudioController {

    private final ConcurrentMap<String, ByteArrayOutputStream> sessionAudioDataMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentMap<String, File> sessionAudioFileMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> callerIdMap = new ConcurrentHashMap<>();
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public AudioController(ConversationRepository conversationRepository, SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.messagingTemplate = messagingTemplate;
        scheduler.scheduleAtFixedRate(this::saveAndSendAudioToFile, 3, 5, TimeUnit.SECONDS);
    }

    @MessageMapping("/register")
    public void registerCallerId(@Header("caller-id") String callerId, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        callerIdMap.put(sessionId, callerId);
        createNewAudioFile(sessionId, callerId);
    }

    @MessageMapping("/audio")
    public void handleAudioMessage(@Payload byte[] message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        sessionAudioDataMap.computeIfAbsent(sessionId, k -> new ByteArrayOutputStream());
        try {
            sessionAudioDataMap.get(sessionId).write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 다른 클라이언트에게 브로드캐스트가 필요한 경우
        messagingTemplate.convertAndSend("/topic/audio", message);
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        String sessionId = SimpMessageHeaderAccessor.getSessionId(event.getMessage().getHeaders());
        System.out.println("세션 연결됨: " + sessionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("세션 연결 해제됨: " + sessionId);
        sessionAudioDataMap.remove(sessionId);
        sessionAudioFileMap.remove(sessionId);
        callerIdMap.remove(sessionId);
    }

    private void saveAndSendAudioToFile() {
        for (String sessionId : sessionAudioDataMap.keySet()) {
            byte[] audioData = sessionAudioDataMap.get(sessionId).toByteArray();
            sessionAudioDataMap.get(sessionId).reset();

            if (audioData.length > 0) {
                System.out.println("오디오 데이터를 파일에 저장 중...");
                try {
                    appendToWavFile(audioData, sessionAudioFileMap.get(sessionId));
                    processAudioFile(sessionId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void processAudioFile(String sessionId) {
        try {
            File audioFile = sessionAudioFileMap.get(sessionId);
            if (audioFile.exists() && audioFile.length() > 0) {
                byte[] audioData = Files.readAllBytes(audioFile.toPath());
                // 오디오 파일을 재설정
                String callerId = getCallerIdFromSessionId(sessionId);
                createNewAudioFile(sessionId, callerId);

                // 오디오 데이터를 STT 서비스로 보내고 응답 처리
                sendToClovaSTT(audioData).subscribe(result -> {
                    if (result != null && !result.isEmpty()) {
                        saveConversation(sessionId, result);
                        System.out.println("음성인식 결과: \n" + result);
                    }
                }, error -> {
                    System.err.println("음성인식 중 오류 발생: " + error.getMessage());
                    error.printStackTrace();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNewAudioFile(String sessionId, String callerId) {
        File audioDir = new File("audio");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File audioFile = new File(audioDir, "audio_" + callerId + "_" + timeStamp + ".wav");
        sessionAudioFileMap.put(sessionId, audioFile);
    }

    private void appendToWavFile(byte[] audioData, File file) throws IOException {
        boolean append = file.exists();
        int bufferSize = 16384;
        try (FileOutputStream fos = new FileOutputStream(file, append);
             AudioInputStream audioInputStream = new AudioInputStream(
                     new ByteArrayInputStream(audioData), getAudioFormat(), audioData.length)) {

            if (append) {
                fos.getChannel().position(file.length());
            } else {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, fos);
                return;
            }

            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }

    private AudioFormat getAudioFormat() {
        return new AudioFormat(16000, 16, 1, true, false);
    }

    private String getCallerIdFromSessionId(String sessionId) {
        return callerIdMap.getOrDefault(sessionId, "unknown");
    }

    private void saveConversation(String sessionId, String transcription) {
        Conversation conversation = new Conversation();
        conversation.setStartTime(LocalDateTime.now());
        conversation.setCallerId(getCallerIdFromSessionId(sessionId));
        conversation.setTranscription(transcription);
        conversationRepository.save(conversation);
    }

    private Mono<String> sendToClovaSTT(byte[] wavData) {
        WebClient clovaClient = WebClient.builder()
                .baseUrl(Constants.CLOVA_SPEECH_INVOKE_URL)
                .defaultHeader("X-CLOVASPEECH-API-KEY", Constants.CLOVA_SPEECH_SECRET_KEY)
                .build();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("media", new ByteArrayResource(wavData))
                .header("Content-Disposition", "form-data; name=media; filename=audio.wav")
                .contentType(MediaType.MULTIPART_FORM_DATA);

        String paramsJson = """
            {
                "language": "ko-KR",
                "completion": "sync",
                "noiseFiltering": true,
                "fullText": true,
                "diarization": {
                    "enable": false
                }
            }
            """;
        builder.part("params", paramsJson)
                .contentType(MediaType.APPLICATION_JSON);

        return clovaClient.post()
                .uri("/recognizer/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    System.out.println("전체 응답: " + response);
                    JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                    return jsonObject.get("text").getAsString();
                })
                .doOnError(e -> {
                    System.err.println("STT API 오류: " + e.getMessage());
                    e.printStackTrace();
                });
    }
}
