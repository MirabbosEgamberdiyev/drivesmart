package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.drivesmart.entity.TestSession;
import java.util.List;
import java.util.Optional;

public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    List<TestSession> findByUserIdOrderByStartedAtDesc(Long userId);
    Optional<TestSession> findByIdAndUserId(Long id, Long userId);
}