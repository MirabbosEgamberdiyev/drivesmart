package uz.drivesmart.dto.response;

import uz.drivesmart.enums.PaymentMethod;
import uz.drivesmart.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Long id,
        Long userId,
        Long packageId,
        String packageName,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String transactionId,
        LocalDateTime purchasedAt,
        LocalDateTime expiresAt,
        String paymentUrl // Click/Payme uchun
) {}
