package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.UserProgress;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserIdAndTopic(Long userId, String topic);

    List<UserProgress> findByUserId(Long userId);

    @Query("""
        SELECT up FROM UserProgress up
        JOIN FETCH up.user
        WHERE up.topic = :topic
        ORDER BY up.averageScore DESC, up.totalTests DESC
        """)
    List<UserProgress> findTopPerformersByTopic(@Param("topic") String topic);
}