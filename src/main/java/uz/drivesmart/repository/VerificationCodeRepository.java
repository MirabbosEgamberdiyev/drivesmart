package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.VerificationCode;
import uz.drivesmart.enums.VerificationType;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * Recipient va type bo'yicha eng so'nggi kodini topish
     */
    @Query("""
        SELECT v FROM VerificationCode v 
        WHERE v.recipient = :recipient 
        AND v.type = :type 
        AND v.isUsed = false 
        AND v.expiresAt > :now
        ORDER BY v.createdAt DESC
        LIMIT 1
        """)
    Optional<VerificationCode> findLatestValidCode(
            @Param("recipient") String recipient,
            @Param("type") VerificationType type,
            @Param("now") LocalDateTime now
    );

    /**
     * Recipient va type bo'yicha kod va uning amal qilish muddatini tekshirish
     */
    @Query("""
        SELECT v FROM VerificationCode v 
        WHERE v.recipient = :recipient 
        AND v.code = :code 
        AND v.type = :type 
        AND v.isUsed = false 
        AND v.expiresAt > :now
        """)
    Optional<VerificationCode> findByRecipientAndCodeAndType(
            @Param("recipient") String recipient,
            @Param("code") String code,
            @Param("type") VerificationType type,
            @Param("now") LocalDateTime now
    );

    /**
     * Muddati o'tgan kodlarni o'chirish
     */
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);

    /**
     * Oxirgi 1 soat ichida yuborilgan kodlar sonini hisoblash (rate limiting)
     */
    @Query("""
        SELECT COUNT(v) FROM VerificationCode v 
        WHERE v.recipient = :recipient 
        AND v.type = :type 
        AND v.createdAt > :since
        """)
    long countRecentCodes(
            @Param("recipient") String recipient,
            @Param("type") VerificationType type,
            @Param("since") LocalDateTime since
    );
}