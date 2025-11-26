package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.drivesmart.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {


    /**
     * Token bo'yicha topish (faqat yaroqli va muddati o'tmagan token)
     */
    @Query("""
        SELECT r FROM RefreshToken r 
        WHERE r.token = :token 
          AND r.isRevoked = false 
          AND r.expiresAt > :now
        """)
    Optional<RefreshToken> findByToken(
            @Param("token") String token,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true, r.revokedAt = :now WHERE r.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR r.isRevoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

    /**
     * Token bo'yicha topish
     */
    @Query("""
        SELECT r FROM RefreshToken r 
        WHERE r.token = :token 
        AND r.isRevoked = false 
        AND r.expiresAt > :now
        """)
    Optional<RefreshToken> findByToken(
            @Param("token") String token
    );
}