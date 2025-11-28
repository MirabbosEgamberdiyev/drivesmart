package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.User;
import uz.drivesmart.enums.Role;

import java.util.List;
import java.util.Optional;

/**
 * User entity uchun repository interface
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // ==================== PHONE NUMBER QUERIES ====================

    /**
     * Telefon raqami bo'yicha foydalanuvchi izlash
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

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

    // ==================== EMAIL QUERIES (✅ FIXED) ====================

    /**
     * Email orqali foydalanuvchi izlash
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Email mavjudligini tekshirish
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.isDeleted = false")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Email mavjudligini tekshirish (o'zgartirishda)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :id AND u.isDeleted = false")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    // ==================== USER LISTS ====================

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

    // ==================== USER LOOKUP ====================

    /**
     * ID bo'yicha active foydalanuvchi
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findActiveById(@Param("id") Long id);
}