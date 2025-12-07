package uz.drivesmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_test_access",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "package_id"}),
        indexes = @Index(name = "idx_access_user_active", columnList = "user_id, is_active, expires_at")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTestAccess extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private TestPackage testPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private UserPurchase purchase;

    @Column(nullable = false)
    private Integer remainingAttempts;

    @Column(nullable = false)
    private LocalDateTime accessedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    public boolean hasAccess() {
        return isActive
                && remainingAttempts > 0
                && LocalDateTime.now().isBefore(expiresAt);
    }
}
