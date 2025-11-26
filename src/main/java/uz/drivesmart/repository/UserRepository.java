package uz.drivesmart.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.Question;
import uz.drivesmart.entity.User;
import uz.drivesmart.enums.Role;


import java.util.List;
import java.util.Optional;

/**
 * User entity uchun repository interface
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query(value = """
        SELECT * FROM questions 
        WHERE topic = :topic AND is_deleted = false
        ORDER BY RANDOM() 
        LIMIT :limit
        """, nativeQuery = true)
    List<Question> findRandomByTopic(@Param("topic") String topic, @Param("limit") int limit);

    @Query("SELECT q FROM Question q WHERE q.isDeleted = false AND q.topic = :topic")
    List<Question> findByTopic(@Param("topic") String topic);

    @Query("SELECT DISTINCT q.topic FROM Question q WHERE q.isDeleted = false ORDER BY q.topic")
    List<String> findDistinctTopics();

    // ✅ Count by topic
    @Query("SELECT COUNT(q) FROM Question q WHERE q.isDeleted = false AND q.topic = :topic")
    long countByTopic(@Param("topic") String topic);
    /**
     * Telefon raqami bo'yicha foydalanuvchi izlash
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Active foydalanuvchilar
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.isActive = true ORDER BY u.firstName, u.lastName")
    List<User> findAllActive();

    /**
     * Rol bo'yicha active foydalanuvchilar
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.isActive = true AND u.role = :role ORDER BY u.firstName, u.lastName")
    List<User> findActiveByRole(@Param("role") Role role);

    /**
     * Telefon raqami mavjudligini tekshirish
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.isDeleted = false AND u.phoneNumber = :phoneNumber")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Telefon raqami mavjudligini tekshirish (o'zgartirishda)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.isDeleted = false AND u.phoneNumber = :phoneNumber AND u.id != :id")
    boolean existsByPhoneNumberAndIdNot(@Param("phoneNumber") String phoneNumber, @Param("id") Long id);
    // ✅ Email orqali qidirish
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    boolean findByEmail(String email);

    // ✅ Email mavjudligini tekshirish
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.isDeleted = false")
    boolean existsByEmail(String email);
}