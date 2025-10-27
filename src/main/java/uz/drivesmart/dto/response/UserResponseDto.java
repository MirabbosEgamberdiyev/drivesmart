package uz.drivesmart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.drivesmart.enums.Role;

import java.time.LocalDateTime;
/**
 * Foydalanuvchi ma'lumotlari uchun response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private String roleDisplay;
    private Boolean isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}