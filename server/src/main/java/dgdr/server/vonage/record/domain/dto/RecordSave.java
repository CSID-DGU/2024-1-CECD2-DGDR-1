package dgdr.server.vonage.record.domain.dto;

import java.time.LocalDateTime;

public record RecordSave(
        Long callId,
        String userId,
        String speakerPhoneNumber,
        String transcription,
        LocalDateTime time
) {
}
