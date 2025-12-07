package uz.drivesmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CardTransferRequest(
        @NotNull(message = "Package ID majburiy")
        Long packageId,

        @NotBlank(message = "Check rasmi majburiy")
        String receiptImageBase64
) {}