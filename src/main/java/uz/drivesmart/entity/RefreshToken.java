package uz.drivesmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh token entity
 * Access token yangilash uchun ishlatiladi
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token"),
        @Index(name = "idx_refresh_user_id", columnList = "user_id"),
        @Index(name = "idx_refresh_expires_at", columnList = "expiresAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique refresh token string (UUID format)
     */
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Token egasi (user)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Token muddati tugash vaqti
     */
    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Token yaratilgan vaqt
     */
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Token bekor qilinganmi? (logout, password reset)
     */
    @Column(nullable = true, name = "is_revoked")
    private Boolean isRevoked = false;

    /**
     * Token bekor qilingan vaqt
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Device/Browser ma'lumotlari (optional)
     */
    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    /**
     * IP address (optional)
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * Entity yaratilishidan oldin avtomatik to'ldirish
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            // Default: 30 kun
            expiresAt = createdAt.plusDays(30);
        }
    }

    /**
     * Token muddati tugaganmi?
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Token bekor qilinganmi?
     */
    public boolean isRevoked() {
        return isRevoked != null && isRevoked;
    }

    /**
     * Token yaroqli (valid) mi?
     * Muddati tugamagan va bekor qilinmagan bo'lishi kerak
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    /**
     * Tokenni bekor qilish
     */
    public void revoke() {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * Token qolgan vaqti (minutlarda)
     */
    public long getRemainingMinutes() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
    }

    /**
     * Token faol bo'lgan vaqt (minutlarda)
     */
    public long getActiveMinutes() {
        LocalDateTime endTime = isRevoked() ? revokedAt : LocalDateTime.now();
        return java.time.Duration.between(createdAt, endTime).toMinutes();
    }
}