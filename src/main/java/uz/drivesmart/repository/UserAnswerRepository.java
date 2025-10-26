package uz.drivesmart.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.drivesmart.entity.UserAnswer;
import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByTestSessionId(Long testSessionId);
}