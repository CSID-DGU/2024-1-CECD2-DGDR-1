package dgdr.server.vonage.record.domain.dto;

import dgdr.server.vonage.record.domain.CallRecord;

import java.util.List;

public record Records(
        List<CallRecord> callRecords
) {
}
