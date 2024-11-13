package dgdr.server.vonage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CallController {
    private final CallService callService;

    @GetMapping("/api/v1/call")
    public ResponseEntity<List<CallDto>> getCallList() {
        List<CallDto> callList = callService.getCallList();
        return ResponseEntity.ok(callList);
    }

    @GetMapping("/api/v1/call/date")
    public ResponseEntity<List<CallDto>> getCallListByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        LocalDate startDateParsed = LocalDate.parse(startDate);
        LocalDate endDateParsed = LocalDate.parse(endDate);

        List<CallDto> callList = callService.getCallListByDateRange(startDateParsed, endDateParsed);
        return ResponseEntity.ok(callList);
    }

    @GetMapping("/api/v1/call/latest")
    public ResponseEntity<List<CallRecordDto>> getLatestCall() {
        List<CallRecordDto> latestCall = callService.getLatestCall();
        return ResponseEntity.ok(latestCall);
    }

    @GetMapping("/api/v1/{callId}/call-record")
    public ResponseEntity<List<CallRecordDto>> getCallRecord(@PathVariable Long callId) {
        List<CallRecordDto> callRecord = callService.getCallRecord(callId);
        return ResponseEntity.ok(callRecord);
    }
}
