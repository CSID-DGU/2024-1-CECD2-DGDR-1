package dgdr.server.vonage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {
    List<CallRecord> findByCallId(Long callId);
}
