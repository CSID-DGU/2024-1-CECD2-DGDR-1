package dgdr.server.vonage;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CallRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "call_id")
    private Call call;

    @Column(name = "speaker_phone_number")
    private String speakerPhoneNumber;

    @Column(name = "transcription")
    private String transcription;

    @CreatedDate
    @Column(name = "time")
    private LocalDateTime time;

    @Builder
    public CallRecord(Call call, String speakerPhoneNumber, String transcription) {
        this.call = call;
        this.speakerPhoneNumber = speakerPhoneNumber;
        this.transcription = transcription;
    }
}
