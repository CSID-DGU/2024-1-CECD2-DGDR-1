package dgdr.server.vonage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CallService {
    private final CallRepository callRepository;
    private final CallRecordRepository callRecordRepository;

    public List<CallDto> getCallList(String userId) {
        return callRepository.findAllByUserId(userId)
                .stream()
                .map(call -> CallDto.builder()
                        .id(call.getId())
                        .startTime(call.getStartTime())
                        .user(call.getUser())
                        .build())
                .collect(Collectors.toList());
    }
    public List<CallDto> getCallListByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return callRepository.findAllByStartTimeBetween(userId, startDateTime, endDateTime)
                .stream()
                .map(call -> CallDto.builder()
                        .id(call.getId())
                        .startTime(call.getStartTime())
                        .user(call.getUser())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CallRecordDto> getLatestCall(String userId) {
        Call latestCall = callRepository.findFirstByOrderByStartTimeDesc(userId);
        return callRecordRepository.findByCallId(latestCall.getId())
                .stream()
                .map(callRecord -> CallRecordDto.builder()
                        .id(callRecord.getId())
                        .call(callRecord.getCall())
                        .speakerPhoneNumber(callRecord.getSpeakerPhoneNumber())
                        .transcription(callRecord.getTranscription())
                        .time(callRecord.getTime())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CallRecordDto> getCallRecord(Long callId) {
        return callRecordRepository.findByCallId(callId)
                .stream()
                .map(callRecord -> CallRecordDto.builder()
                        .id(callRecord.getId())
                        .call(callRecord.getCall())
                        .speakerPhoneNumber(callRecord.getSpeakerPhoneNumber())
                        .transcription(callRecord.getTranscription())
                        .time(callRecord.getTime())
                        .build())
                .collect(Collectors.toList());
    }
}
