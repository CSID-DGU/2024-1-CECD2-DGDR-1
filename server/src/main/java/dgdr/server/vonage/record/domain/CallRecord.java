package dgdr.server.vonage.record.domain;

import dgdr.server.vonage.call.domain.Call;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "callRecords")
public class CallRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id")
    private Call call;
    private String userId;
    private String speakerPhoneNumber;
    private String transcription;
    private LocalDateTime createdAt;
}
