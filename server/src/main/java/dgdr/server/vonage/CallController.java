package dgdr.server.vonage;

import dgdr.server.vonage.user.domain.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<List<CallDto>> getCallList(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<CallDto> callList = callService.getCallList(principalDetails.getUsername());
        return ResponseEntity.ok(callList);
    }

    @GetMapping("/api/v1/call/date")
    public ResponseEntity<List<CallDto>> getCallListByDateRange(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        LocalDate startDateParsed = LocalDate.parse(startDate);
        LocalDate endDateParsed = LocalDate.parse(endDate);

        List<CallDto> callList = callService.getCallListByDateRange(principalDetails.getUsername(), startDateParsed, endDateParsed);
        return ResponseEntity.ok(callList);
    }

    @GetMapping("/api/v1/call/latest")
    public ResponseEntity<List<CallRecordDto>> getLatestCall(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<CallRecordDto> latestCall = callService.getLatestCall(principalDetails.getUsername());
        return ResponseEntity.ok(latestCall);
    }

    @GetMapping("/api/v1/{callId}/call-record")
    public ResponseEntity<List<CallRecordDto>> getCallRecord(@PathVariable Long callId) {
        List<CallRecordDto> callRecord = callService.getCallRecord(callId);
        return ResponseEntity.ok(callRecord);
    }
}
