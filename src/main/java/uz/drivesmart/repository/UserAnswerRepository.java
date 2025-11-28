package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.UserAnswer;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    List<UserAnswer> findByTestSessionId(Long testSessionId);

    boolean existsByTestSessionIdAndQuestionId(Long testSessionId, Long questionId);

    long countByTestSessionId(Long testSessionId);

    boolean existsByTestSessionId(Long testSessionId);

    // ENG MUHIM METOD — PostgreSQL uchun maxsus optimallashtirilgan
    // Har bir savol uchun eng oxirgi xato javob bo‘yicha tartiblaydi
    @Query(value = """
        SELECT DISTINCT ON (ua.question_id) ua.question_id
        FROM user_answers ua
        JOIN questions q ON q.id = ua.question_id
        WHERE ua.user_id = :userId
          AND q.topic = :topic
          AND ua.is_correct = false
        ORDER BY ua.question_id, ua.answered_at DESC
        """, nativeQuery = true)
    List<Long> findLatestIncorrectQuestionIdsByTopic(
            @Param("userId") Long userId,
            @Param("topic") String topic
    );

    // Statistika uchun
    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.user.id = :userId AND ua.isCorrect = true")
    long countCorrectAnswersByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.user.id = :userId")
    long countTotalAnswersByUserId(@Param("userId") Long userId);
}