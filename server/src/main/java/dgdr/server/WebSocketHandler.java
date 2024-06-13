package dgdr.server;

import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
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
import java.util.List;
import java.util.concurrent.*;

@Component
public class WebSocketHandler extends BinaryWebSocketHandler {

    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, ByteArrayOutputStream> sessionAudioDataMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentMap<String, File> sessionAudioFileMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Boolean> isFirstRunMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalDateTime> sessionStartTimeMap = new ConcurrentHashMap<>();

    private final ConversationRepository conversationRepository;
    private final WebClient webClient;

    @Autowired
    public WebSocketHandler(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
        this.webClient = WebClient.builder().baseUrl("http://localhost:3000").build();
        scheduler.scheduleAtFixedRate(this::saveAndSendAudioToFile, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            ByteBuffer buffer = message.getPayload();
            byte[] receivedBytes = new byte[buffer.remaining()];
            buffer.get(receivedBytes);

            sessionAudioDataMap.computeIfAbsent(session.getId(), k -> new ByteArrayOutputStream());

            synchronized (sessionAudioDataMap.get(session.getId())) {
                sessionAudioDataMap.get(session.getId()).write(receivedBytes);
            }

            for (WebSocketSession s : sessions) {
                if (s.isOpen() && !s.getId().equals(session.getId())) {
                    try {
                        s.sendMessage(new BinaryMessage(receivedBytes));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        System.out.println("Received text message: " + payload);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.add(session);
        String callerId = getCallerIdFromSession(session);
        createNewAudioFile(session.getId(), callerId);
        isFirstRunMap.put(session.getId(), true);
        sessionStartTimeMap.put(session.getId(), LocalDateTime.now());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessions.remove(session);
        sessionAudioDataMap.remove(session.getId());
        sessionAudioFileMap.remove(session.getId());
        isFirstRunMap.remove(session.getId());
        sessionStartTimeMap.remove(session.getId());
    }

    private void saveAndSendAudioToFile() {
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }

            byte[] audioData;
            synchronized (sessionAudioDataMap.get(session.getId())) {
                audioData = sessionAudioDataMap.get(session.getId()).toByteArray();
                sessionAudioDataMap.get(session.getId()).reset();
            }

            if (audioData.length > 0) {
                try {
                    appendToWavFile(audioData, sessionAudioFileMap.get(session.getId()));
                    if (!isFirstRunMap.get(session.getId())) {
                        File audioFile = sessionAudioFileMap.get(session.getId());
                        if (audioFile.exists() && audioFile.length() > 0) {
                            convertSpeechToText(audioFile.getAbsolutePath(), session).subscribe(result -> {
                                System.out.println("Transcription result: \n" + result);

                                saveConversation(session, (String) result);

                                audioFile.delete();
                                String callerId = getCallerIdFromSession(session);
                                createNewAudioFile(session.getId(), callerId);
                            }, error -> {
                                System.err.println("Error during transcription: " + error.getMessage());
                            });
                        }
                    } else {
                        isFirstRunMap.put(session.getId(), false);
                        String callerId = getCallerIdFromSession(session);
                        createNewAudioFile(session.getId(), callerId);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
        try (FileOutputStream fos = new FileOutputStream(file, append);
             AudioInputStream audioInputStream = new AudioInputStream(
                     new ByteArrayInputStream(audioData), getAudioFormat(), audioData.length)) {

            if (append) {
                fos.getChannel().position(file.length());
            } else {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, fos);
                return;
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }

    private AudioFormat getAudioFormat() {
        return new AudioFormat(8000, 16, 1, true, false);
    }

    private Mono<String> sendToClovaSTT(File audioFile) throws IOException {
        WebClient clovaClient = WebClient.builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com")
                .build();

        byte[] audioBytes = Files.readAllBytes(audioFile.toPath());

        return clovaClient.post()
                .uri(uriBuilder -> uriBuilder.path("/recog/v1/stt")
                        .queryParam("lang", "Kor")
                        .build())
                .header("X-NCP-APIGW-API-KEY-ID", "u9sc8eoc0e")
                .header("X-NCP-APIGW-API-KEY", "wksYArTS8hgVP7f3TGmt9JSmlTXpzyk5Fi20BauX")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(audioBytes)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(body -> {
                            System.err.println("Error response body: " + body);
                            return Mono.error(new RuntimeException("Error response from API: " + body));
                        }))
                .bodyToMono(String.class)
                .doOnError(error -> {
                    error.printStackTrace();
                });
    }

    public Mono<Object> convertSpeechToText(String audioFilePath, WebSocketSession session) {
        String speechKey = "aebf7631e7e5435d86d5ae979e0a03c9";
        String speechRegion = "koreacentral";

        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
        speechConfig.setSpeechRecognitionLanguage("ko-KR");

        AudioConfig audioConfig;
        try {
            audioConfig = AudioConfig.fromWavFileInput(audioFilePath);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error creating AudioConfig from WAV file: " + audioFilePath, e));
        }

        ConversationTranscriber conversationTranscriber = new ConversationTranscriber(speechConfig, audioConfig);

        return Mono.create(sink -> {
            StringBuilder transcriptionBuilder = new StringBuilder();

            conversationTranscriber.transcribed.addEventListener((s, e) -> {
                if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                    transcriptionBuilder
                            .append(e.getResult().getText());
                } else if (e.getResult().getReason() == ResultReason.NoMatch) {
                    System.out.println("NOMATCH: Speech could not be transcribed.");
                }
            });

            conversationTranscriber.canceled.addEventListener((s, e) -> {
                System.out.println("CANCELED: Reason=" + e.getReason());

                if (e.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + e.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + e.getErrorDetails());
                    System.out.println("CANCELED: Did you update the subscription info?");
                    sink.error(new RuntimeException("Error during transcription: " + e.getErrorDetails()));
                }
            });

            conversationTranscriber.sessionStarted.addEventListener((s, e) -> {
                System.out.println("\n    Session started event.");
            });

            conversationTranscriber.sessionStopped.addEventListener((s, e) -> {
                System.out.println("\n    Session stopped event.");
                sink.success(transcriptionBuilder.toString());
            });

            try {
                conversationTranscriber.startTranscribingAsync().get();
            } catch (Exception e) {
                sink.error(new RuntimeException("Error starting transcription", e));
            }

        }).doFinally(signalType -> {
            conversationTranscriber.close();
            audioConfig.close();
            speechConfig.close();
        });
    }

    private String getCallerIdFromSession(WebSocketSession session) {
        String callerId = "unknown";

        try {
            URI uri = session.getUri();
            String query = uri.getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("caller-id")) {
                        callerId = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.toString());
                        break;
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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


    private void sendConversationToServer(Conversation conversation) {
        webClient.post()
                .uri("https://f238-210-94-220-228.ngrok-free.app/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(conversation)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(response -> {
                    System.out.println("Conversation successfully sent to server.");
                })
                .doOnError(error -> {
                    System.err.println("Error sending conversation to server: " + error.getMessage());
                })
                .subscribe();
    }
}
