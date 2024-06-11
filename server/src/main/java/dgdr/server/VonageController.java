package dgdr.server;

import com.vonage.client.voice.ncco.Ncco;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class VonageController {
    private final VonageService vonageService;

    @PostMapping("/answer")
    public Ncco answerWebhook(@RequestBody AnswerWebhookPayload payload) {
        String from = payload.getFrom();
        return vonageService.createConversationNcco(from);
    }

    @PostMapping("/event")
    public void eventWebhook(@RequestBody String payload) {
        System.out.println("Event: " + payload);
    }
}