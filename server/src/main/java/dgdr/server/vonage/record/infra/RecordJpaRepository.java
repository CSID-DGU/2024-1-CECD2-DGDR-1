package dgdr.server.vonage.record.infra;

import dgdr.server.vonage.record.domain.CallRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordJpaRepository extends JpaRepository<CallRecord, Long> {
}
