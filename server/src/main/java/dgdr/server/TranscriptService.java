package dgdr.server;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranscriptService {
    private final ConversationRepository conversationRepository;

    public List<Map<String, String>> getAllConversations() {
        List<Conversation> conversations = conversationRepository.findAll();
        return formatConversations(conversations);
    }

    private List<Map<String, String>> formatConversations(List<Conversation> conversations) {
        return conversations.stream()
                .map(conversation -> Map.of(
                        "callerId", conversation.getCallerId(),
                        "transcription", conversation.getTranscription(),
                        "startTime", conversation.getStartTime().toString()
                ))
                .collect(Collectors.toList());
    }
}
