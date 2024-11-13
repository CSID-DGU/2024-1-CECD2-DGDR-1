package dgdr.server.vonage.call.service;

import dgdr.server.vonage.call.domain.Call;
import dgdr.server.vonage.call.domain.dto.CallDto;
import dgdr.server.vonage.call.domain.dto.CallRecordsDto;
import dgdr.server.vonage.call.domain.dto.CallReq;
import dgdr.server.vonage.call.infra.CallRepository;
import dgdr.server.vonage.record.domain.dto.RecordDto;
import dgdr.server.vonage.user.domain.User;
import dgdr.server.vonage.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CallService {

    private final CallRepository callRepository;
    private final UserRepository userRepository;

    public CallRecordsDto getLatestCall() {
        Call latestCall = callRepository.findFirstByOrderByStartTimeDesc()
                .orElseThrow(() -> new IllegalArgumentException("통화 내역이 존재하지 않습니다."));

        // CallRecordsDto 엔티티를 CallDTO로 변환하여 반환
        List<RecordDto> recordDtoList = latestCall.getCallRecords().stream().map(record ->
                new RecordDto(record.getId(),
                        record.getUserId(),
                        record.getSpeakerPhoneNumber(),
                        record.getTranscription(),
                        record.getCreatedAt())
        ).collect(Collectors.toList());

        return new CallRecordsDto(latestCall.getId(),
                latestCall.getStartTime(),
                recordDtoList);
    }

    public CallRecordsDto getCallById(Long callId) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("통화 내역이 존재하지 않습니다."));

        // CallRecordsDto 엔티티를 CallDTO로 변환하여 반환
        List<RecordDto> recordDtoList = call.getCallRecords().stream().map(record ->
                new RecordDto(record.getId(),
                        record.getUserId(),
                        record.getSpeakerPhoneNumber(),
                        record.getTranscription(),
                        record.getCreatedAt())
        ).collect(Collectors.toList());

        return new CallRecordsDto(call.getId(),
                call.getStartTime(),
                recordDtoList);
    }

    public List<CallDto> getCallList(String userId) {
        List<Call> callList = callRepository.findAllByUser_UserIdOrderByStartTimeDesc(userId);
        return callList.stream().map(call ->
                new CallDto(call.getId(),
                        call.getStartTime(),
                        call.getUser().getUserId())
        ).collect(Collectors.toList());
    }

    public List<CallDto> getGallListByDate(LocalDate startDate, LocalDate endDate, String userId){
        List<Call> callList = callRepository.findAllByStartTimeBetween(startDate.atStartOfDay(), endDate.atStartOfDay(), userId);
        return callList.stream().map(call ->
                new CallDto(call.getId(),
                        call.getStartTime(),
                        call.getUser().getUserId())
        ).collect(Collectors.toList());

    }

    @Transactional
    public Call saveCall(Call call) {
        return callRepository.save(call);
    }

    @Transactional
    public CallDto saveCall(CallReq callReq){
        User user = userRepository.findById(callReq.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));

        Call call = Call.builder()
                .startTime(callReq.startTime())
                .user(user)
                .build();

        Call savedCall = callRepository.save(call);

        return new CallDto(savedCall.getId(), savedCall.getStartTime(), savedCall.getUser().getUserId());
    }
}
