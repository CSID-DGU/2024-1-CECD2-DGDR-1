package dgdr.server.vonage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CallRepository extends JpaRepository<Call, Long> {
    @Query("SELECT c FROM Call c WHERE c.user.userId = :userId ORDER BY c.startTime DESC")
    Call findFirstByOrderByStartTimeDesc(String userId);
    @Query("SELECT c FROM Call c WHERE c.user.userId = :userId AND c.startTime BETWEEN :startTime AND :startTime2")
    List<Call> findAllByStartTimeBetween(String userId, LocalDateTime startTime, LocalDateTime startTime2);

    @Query("SELECT c FROM Call c WHERE c.user.userId = :userId")
    Collection<Call> findAllByUserId(String userId);
}
