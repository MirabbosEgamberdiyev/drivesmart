package uz.drivesmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.drivesmart.enums.PaymentMethod;
import uz.drivesmart.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_purchases", indexes = {
        @Index(name = "idx_purchase_user", columnList = "user_id, status"),
        @Index(name = "idx_purchase_transaction", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPurchase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private TestPackage testPackage;

    @Column(unique = true, length = 255)
    private String transactionId; // Click/Payme transaction ID

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime purchasedAt;

    private LocalDateTime expiresAt; // Access muddati

    private LocalDateTime confirmedAt; // Admin tasdiqlagan vaqt (karta to'lovi uchun)

    @Column(length = 500)
    private String receiptImagePath; // Check rasmi (karta to'lovi uchun)

    @Column(columnDefinition = "TEXT")
    private String adminNotes; // Admin izohi

    @Column(nullable = false)
    private Boolean isRefunded = false;

    private LocalDateTime refundedAt;

    @Column(columnDefinition = "TEXT")
    private String refundReason;
}