package dgdr.server.vonage;

import com.vonage.client.voice.ncco.Ncco;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VonageController {
    private final VonageService vonageService;
    private final ManualService manualService;
    private final TranscriptService transcriptService;

    @PostMapping("/answer")
    public Ncco answerWebhook(@RequestBody AnswerWebhookPayload payload) {
        String from = payload.getFrom();
        return vonageService.createConversationNcco(from);
    }

    @PostMapping("/event")
    public void eventWebhook(@RequestBody String payload) {
//        System.out.println("Event: " + payload);
    }

    @GetMapping("/manual")
    public ResponseEntity<Map<String, Object>> sendManual() {
        Map<String, Object> response = manualService.getManual();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transcript")
    public ResponseEntity<List<Map<String, String>>> getAllConversations() {
        List<Map<String, String>> allConversations = transcriptService.getAllConversations();
        return ResponseEntity.ok(allConversations);
    }

    @PostMapping("/transcript/clear")
    public ResponseEntity<Void> clearAllConversations() {
        transcriptService.clearAllConversation();
        return ResponseEntity.ok().build();
    }
}