package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.TestSession;
import java.util.List;
import java.util.Optional;
@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {

    @Query("""
        SELECT ts FROM TestSession ts 
        JOIN FETCH ts.user 
        WHERE ts.user.id = :userId AND ts.isDeleted = false
        ORDER BY ts.startedAt DESC
        """)
    List<TestSession> findByUserIdOrderByStartedAtDesc(@Param("userId") Long userId);

    @Query("""
        SELECT ts FROM TestSession ts 
        JOIN FETCH ts.user 
        WHERE ts.id = :id AND ts.user.id = :userId AND ts.isDeleted = false
        """)
    Optional<TestSession> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}