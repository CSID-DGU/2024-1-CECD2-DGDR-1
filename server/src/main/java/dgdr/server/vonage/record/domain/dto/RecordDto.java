package dgdr.server.vonage.record.domain.dto;

import dgdr.server.vonage.record.domain.CallRecord;

import java.time.LocalDateTime;

public record RecordDto(
        Long id,
        String userId,
        String speakerPhoneNumber,
        String transcription,
        LocalDateTime time
) {
    public static RecordDto toDto(CallRecord callRecord) {
        return new RecordDto(
                callRecord.getId(),
                callRecord.getUserId(),
                callRecord.getSpeakerPhoneNumber(),
                callRecord.getTranscription(),
                callRecord.getCreatedAt()
        );
    }
}
