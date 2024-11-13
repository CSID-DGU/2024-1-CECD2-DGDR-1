package dgdr.server.vonage.call.presentation;

import dgdr.server.vonage.call.domain.dto.CallDto;
import dgdr.server.vonage.call.domain.dto.CallRecordsDto;
import dgdr.server.vonage.call.domain.dto.CallReq;
import dgdr.server.vonage.call.domain.dto.CallsRes;
import dgdr.server.vonage.call.service.CallService;
import dgdr.server.vonage.user.domain.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CallApiService {
    private final CallService callService;

    @GetMapping("/api/v1/call/latest")
    public ResponseEntity<CallRecordsDto> getLatestCall() {
        return ResponseEntity.ok(callService.getLatestCall());
    }

    @GetMapping("/api/v1/call/{callId}")
    public ResponseEntity<CallRecordsDto> getCallById(Long callId) {
        return ResponseEntity.ok(callService.getCallById(callId));
    }

    @GetMapping("/api/v1/calls")
    public ResponseEntity<CallsRes> getCallList(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(new CallsRes(callService.getCallList(principalDetails.getUsername())));
    }

    @PostMapping("/api/v1/call")
    public ResponseEntity<CallDto> saveCall(@RequestBody CallReq callReq) {
        return ResponseEntity.ok(callService.saveCall(callReq));
    }

}
