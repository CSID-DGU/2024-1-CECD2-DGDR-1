package dgdr.server.vonage.record.service;


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



}
