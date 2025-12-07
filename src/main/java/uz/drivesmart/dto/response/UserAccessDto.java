package uz.drivesmart.dto.response;

import java.time.LocalDateTime;

public record UserAccessDto(
        Long id,
        Long packageId,
        String packageName,
        Integer remainingAttempts,
        LocalDateTime expiresAt,
        Boolean hasAccess
) {}