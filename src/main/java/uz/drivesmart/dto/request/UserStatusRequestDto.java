package uz.drivesmart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Foydalanuvchi holati o'zgartirish DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusRequestDto {

    @NotNull(message = "Holat majburiy")
    private Boolean isActive;
}