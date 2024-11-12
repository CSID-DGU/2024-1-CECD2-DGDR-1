package dgdr.server.vonage.record.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordJpaRepository extends JpaRepository<Record, Long> {
}
