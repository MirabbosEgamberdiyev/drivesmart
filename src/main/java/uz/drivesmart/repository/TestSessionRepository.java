package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.dto.response.TopicStatsDto;
import uz.drivesmart.entity.TestSession;
import uz.drivesmart.enums.TestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {

    @Query("""
           SELECT ts FROM TestSession ts
           JOIN FETCH ts.user u
           WHERE u.id = :userId AND ts.isDeleted = false
           ORDER BY ts.startedAt DESC
           """)
    List<TestSession> findByUserIdOrderByStartedAtDesc(@Param("userId") Long userId);

    @Query("""
           SELECT ts FROM TestSession ts
           JOIN FETCH ts.user u
           WHERE ts.id = :id AND u.id = :userId AND ts.isDeleted = false
           """)
    Optional<TestSession> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // Dashboard statistikasi
    Long countByStatus(TestStatus status);

    @Query("""
           SELECT AVG(CAST(ts.correctCount AS double) * 100.0 / NULLIF(ts.totalQuestions, 0))
           FROM TestSession ts
           WHERE ts.status = 'COMPLETED'
           """)
    Double calculateAverageScore();

    Long countByStartedAtAfter(LocalDateTime date);

    @Query("""
           SELECT ts FROM TestSession ts
           JOIN FETCH ts.user
           WHERE ts.isDeleted = false
           ORDER BY ts.startedAt DESC
           LIMIT :limit
           """)
    List<TestSession> findRecentTests(@Param("limit") int limit);

    @Query("""
           SELECT new uz.drivesmart.dto.response.TopicStatsDto(
               ts.topic,
               COUNT(ts.id),
               AVG(CAST(ts.correctCount AS double) * 100.0 / NULLIF(ts.totalQuestions, 0) ),
               SUM(CASE WHEN (CAST(ts.correctCount AS double) * 100.0 / NULLIF(ts.totalQuestions, 0)) >= 70.0 THEN 1 ELSE 0 END)
           )
           FROM TestSession ts
           WHERE ts.status = 'COMPLETED'
           GROUP BY ts.topic
           ORDER BY COUNT(ts.id) DESC
           """)
    List<TopicStatsDto> calculateTopicStats();
}