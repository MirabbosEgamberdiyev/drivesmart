package uz.drivesmart.dto.request;

import jakarta.validation.constraints.NotNull;
import uz.drivesmart.enums.PaymentMethod;

public record PurchaseInitRequest(
        @NotNull(message = "Package ID majburiy")
        Long packageId,

        @NotNull(message = "To'lov usuli majburiy")
        PaymentMethod paymentMethod
) {}