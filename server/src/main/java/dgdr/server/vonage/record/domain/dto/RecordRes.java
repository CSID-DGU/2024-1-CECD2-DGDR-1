package dgdr.server.vonage.record.domain.dto;

import java.time.LocalDateTime;

public record RecordRes(
        Long id,
        Long callId,
        String userId,
        String speakerPhoneNumber,
        String transcription,
        LocalDateTime time
) {
}
