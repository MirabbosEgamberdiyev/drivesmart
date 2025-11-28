package uz.drivesmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.drivesmart.enums.VerificationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_codes", indexes = {
        @Index(name = "idx_verification_recipient", columnList = "recipient, type, is_used"),
        @Index(name = "idx_verification_expires", columnList = "expires_at"),
        @Index(name = "idx_verification_ip", columnList = "ip_address, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false, length = 100)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VerificationType type;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "is_used")
    @Builder.Default
    private Boolean isUsed = false;

    @Column(nullable = false, name = "attempt_count")
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // ✅ NEW: IP tracking for rate limiting
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // ✅ NEW: User agent tracking
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = createdAt.plusMinutes(5);
        }
        if (isUsed == null) {
            isUsed = false;
        }
        if (attemptCount == null) {
            attemptCount = 0;
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isMaxAttemptsReached() {
        return attemptCount >= 3;
    }

    public void incrementAttempts() {
        this.attemptCount++;
    }

    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
}