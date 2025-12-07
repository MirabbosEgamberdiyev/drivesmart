package uz.drivesmart.dto.response;

import java.math.BigDecimal;

public record TestPackageDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer questionCount,
        Integer durationDays,
        Integer maxAttempts,
        String topic,
        Boolean isActive
) {}