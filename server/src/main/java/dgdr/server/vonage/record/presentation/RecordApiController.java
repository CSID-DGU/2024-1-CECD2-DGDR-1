package dgdr.server.vonage.record.presentation;

import dgdr.server.vonage.record.domain.dto.RecordDto;
import dgdr.server.vonage.record.domain.dto.RecordSave;
import dgdr.server.vonage.record.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class RecordApiController {

    private final RecordService recordService;

    @PostMapping("/api/v1/record")
    public ResponseEntity<RecordDto> saveRecord(@RequestBody RecordSave recordDto) {
        return ResponseEntity.ok(recordService.saveRecord(recordDto));
    }
}
