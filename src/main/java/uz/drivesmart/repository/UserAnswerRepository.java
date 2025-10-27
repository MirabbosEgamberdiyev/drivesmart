package uz.drivesmart.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.UserAnswer;

import java.util.List;

// ✅ UserAnswerRepository'ga qo'shing
@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    List<UserAnswer> findByTestSessionId(Long testSessionId);

    // ✅ Duplicate check
    boolean existsByTestSessionIdAndQuestionId(Long testSessionId, Long questionId);

    // ✅ Count
    long countByTestSessionId(Long testSessionId);

    // ✅ User statistikasi
    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.user.id = :userId AND ua.isCorrect = true")
    long countCorrectAnswersByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.user.id = :userId")
    long countTotalAnswersByUserId(@Param("userId") Long userId);
}