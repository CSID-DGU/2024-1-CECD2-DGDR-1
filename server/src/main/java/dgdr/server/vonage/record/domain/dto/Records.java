package dgdr.server.vonage.record.domain.dto;

import dgdr.server.vonage.record.domain.Record;

import java.util.List;

public record Records(
        List<Record> records
) {
}
