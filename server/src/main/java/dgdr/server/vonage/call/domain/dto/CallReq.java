package dgdr.server.vonage.call.domain.dto;

import java.time.LocalDateTime;

public record CallReq(
        LocalDateTime startTime,
        String userId
) {
}
