package dgdr.server.vonage;

import dgdr.server.vonage.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CallDto {
    private final Long id;
    private final LocalDateTime startTime;
    private final User user;

    @Builder
    public CallDto(Long id, LocalDateTime startTime, User user) {
        this.id = id;
        this.startTime = startTime;
        this.user = user;
    }
}
