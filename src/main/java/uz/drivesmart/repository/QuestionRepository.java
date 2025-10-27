package uz.drivesmart.repository;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM questions WHERE topic = :topic ORDER BY RANDOM() LIMIT :limit",
            nativeQuery = true)
    List<Question> findRandomByTopic(@Param("topic") String topic, @Param("limit") int limit);

    List<Question> findByTopic(String topic);

    @Query("SELECT DISTINCT q.topic FROM Question q ORDER BY q.topic")
    List<String> findAllTopics();

    @Query("SELECT DISTINCT q.topic FROM Question q")
    List<String> findDistinctTopics();

    boolean existsByTextAndTopic(String text, String topic);

}