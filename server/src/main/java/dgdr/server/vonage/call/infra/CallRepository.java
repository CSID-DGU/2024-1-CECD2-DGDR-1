package dgdr.server.vonage.call.infra;

import dgdr.server.vonage.call.domain.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CallRepository extends JpaRepository<Call, Long> {

    Optional<Call> findFirstByOrderByStartTimeDesc();

    List<Call> findAllByUser_UserIdOrderByStartTimeDesc(String userId);

    List<Call> findAllByStartTimeBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);

    @Query("SELECT c FROM Call c WHERE c.startTime BETWEEN ?1 AND ?2 AND c.user.userId = ?3")
    List<Call> findAllByStartTimeBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1, String userId);
}
