package dgdr.server.vonage.call.domain.dto;

import java.util.List;

public record CallsRes(
        List<CallDto> calls
) {
}
