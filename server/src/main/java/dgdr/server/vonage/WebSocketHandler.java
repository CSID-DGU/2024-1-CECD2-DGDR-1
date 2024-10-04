package dgdr.server.vonage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import reactor.core.publisher.Mono;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

@Component
public class WebSocketHandler extends BinaryWebSocketHandler {

    private final ConcurrentMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ByteArrayOutputStream> sessionAudioDataMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentMap<String, File> sessionAudioFileMap = new ConcurrentHashMap<>();
    private final ConversationRepository conversationRepository;

    @Autowired
    public WebSocketHandler(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
        scheduler.scheduleAtFixedRate(this::saveAndSendAudioToFile, 3, 5, TimeUnit.SECONDS);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            ByteBuffer buffer = message.getPayload();
            sessionAudioDataMap.computeIfAbsent(session.getId(), k -> new ByteArrayOutputStream());
            sessionAudioDataMap.get(session.getId()).write(buffer.array(), buffer.position(), buffer.remaining());

            for (WebSocketSession s : sessionMap.values()) {
                synchronized (s) {
                    if (s.isOpen() && !s.getId().equals(session.getId()))
                        s.sendMessage(message);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        System.out.println("참여자 : " + session.getId());
        String callerId = getCallerIdFromSession(session);
        createNewAudioFile(session.getId(), callerId);
        sessionMap.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessionAudioDataMap.remove(session.getId());
        sessionAudioFileMap.remove(session.getId());
        sessionMap.remove(session.getId());
        session.close();
    }

    private void saveAndSendAudioToFile() {
        for (WebSocketSession session : sessionMap.values()) {
            if (!session.isOpen()) {
                continue;
            }

            byte[] audioData;
            audioData = sessionAudioDataMap.get(session.getId()).toByteArray();
            sessionAudioDataMap.get(session.getId()).reset();


            if (audioData.length > 0) {
                System.out.println("Saving audio data to file...");
                try {
                    appendToWavFile(audioData, sessionAudioFileMap.get(session.getId()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                processAudioFile(session);
            }
        }
    }

    private void processAudioFile(WebSocketSession session) {
        try {
            File audioFile = sessionAudioFileMap.get(session.getId());
            if (audioFile.exists() && audioFile.length() > 0) {
                byte[] audioData = Files.readAllBytes(audioFile.toPath());
                createNewAudioFile(session.getId(), getCallerIdFromSession(session));

//                sendToClovaSTT(audioData).subscribe(result -> {
//                    if (result != null && !result.isEmpty()) {
//                        saveConversation(session, result);
//                        System.out.println("Transcription result: \n" + result);
//                    }
//
//                    String callerId = getCallerIdFromSession(session);
//                    createNewAudioFile(session.getId(), callerId);
//                }, error -> {
//                    System.err.println("Error during transcription: " + error.getMessage());
//                    error.printStackTrace();
//                });
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
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    System.out.println("Full Response: " + response);
                    JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                    return jsonObject.get("text").getAsString();
                })
                .doOnError(e -> {
                    System.err.println("STT API error: " + e.getMessage());
                    e.printStackTrace();
                });
    }

    private String getCallerIdFromSession(WebSocketSession session) {
        String callerId = "unknown";

        URI uri = session.getUri();
        String query = uri.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals("caller-id")) {
                    callerId = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    break;
                }
            }
        }

        return callerId;
    }

    private void saveConversation(WebSocketSession session, String transcription) {
        Conversation conversation = new Conversation();
        conversation.setStartTime(LocalDateTime.now());
        conversation.setCallerId(getCallerIdFromSession(session));
        conversation.setTranscription(transcription);
        conversationRepository.save(conversation);
    }
}