package dgdr.server.vonage;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CallRecordDto {
    private final Long id;
    private final Call call;
    private final String speakerPhoneNumber;
    private final String transcription;
    private final LocalDateTime time;

    @Builder
    public CallRecordDto(Long id, Call call, String speakerPhoneNumber, String transcription, LocalDateTime time) {
        this.id = id;
        this.call = call;
        this.speakerPhoneNumber = speakerPhoneNumber;
        this.transcription = transcription;
        this.time = time;
    }
}
