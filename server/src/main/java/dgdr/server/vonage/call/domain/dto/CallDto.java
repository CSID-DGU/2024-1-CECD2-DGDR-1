package dgdr.server.vonage.call.domain.dto;

import java.time.LocalDateTime;

public record CallDto(
        Long callId,
        LocalDateTime callCreatedAt,
        String userId
) {
}
