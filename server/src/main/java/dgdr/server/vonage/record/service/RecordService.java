package dgdr.server.vonage.record.service;


import dgdr.server.vonage.call.domain.Call;
import dgdr.server.vonage.call.infra.CallRepository;
import dgdr.server.vonage.record.domain.CallRecord;
import dgdr.server.vonage.record.domain.dto.RecordDto;
import dgdr.server.vonage.record.domain.dto.RecordSave;
import dgdr.server.vonage.record.infra.RecordJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RecordService {
    private final RecordJpaRepository recordJpaRepository;
    private final CallRepository callRepository;

    @Transactional
    public RecordDto saveRecord(RecordSave recordSave) {
        Call call = callRepository.findById(recordSave.callId())
                .orElseThrow(() -> new IllegalArgumentException("통화 내역이 존재하지 않습니다."));

        CallRecord callRecord = CallRecord.builder()
                .call(call)
                .speakerPhoneNumber(recordSave.speakerPhoneNumber())
                .transcription(recordSave.transcription())
                .build();

        return RecordDto.toDto(recordJpaRepository.save(callRecord));
    }



}
