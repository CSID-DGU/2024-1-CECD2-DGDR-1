package dgdr.server.vonage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CallRepository extends JpaRepository<Call, Long> {
    Call findFirstByOrderByStartTimeDesc();
    List<Call> findAllByStartTimeBetween(LocalDateTime startTime, LocalDateTime startTime2);
}
