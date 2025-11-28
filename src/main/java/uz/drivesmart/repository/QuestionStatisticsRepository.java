package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.QuestionStatistics;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionStatisticsRepository extends JpaRepository<QuestionStatistics, Long> {

    Optional<QuestionStatistics> findByQuestionId(Long questionId);

    @Query("""
        SELECT qs FROM QuestionStatistics qs
        JOIN FETCH qs.question q
        WHERE q.topic = :topic
        AND qs.successRate < :maxSuccessRate
        ORDER BY qs.successRate ASC
        """)
    List<QuestionStatistics> findDifficultQuestions(
            @Param("topic") String topic,
            @Param("maxSuccessRate") Double maxSuccessRate
    );
}