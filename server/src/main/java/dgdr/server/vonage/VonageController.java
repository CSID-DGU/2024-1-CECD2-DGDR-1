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

    @PostMapping("/answer")
    public Ncco answerWebhook(@RequestBody AnswerWebhookPayload payload) {
        String from = payload.getFrom();
        return vonageService.createOrJoinConversationWithWebSocket(from);
    }

    @PostMapping("/event")
    public void eventWebhook(@RequestBody String payload) {
        System.out.println("Event: " + payload);
    }

    @GetMapping("/manual/{callId}")
    public ResponseEntity<Map<String, Object>> sendManual(@PathVariable Long callId) {
        Map<String, Object> response = manualService.getManual(callId);
        return ResponseEntity.ok(response);
    }
}