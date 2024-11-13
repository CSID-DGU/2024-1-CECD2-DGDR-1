package dgdr.server.vonage.call.domain.dto;

import dgdr.server.vonage.record.domain.dto.RecordDto;

import java.time.LocalDateTime;
import java.util.List;

public record CallRecordsDto(
        Long callId,
        LocalDateTime callCreatedAt,
        List<RecordDto> records
) {
}
