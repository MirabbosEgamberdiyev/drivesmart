package uz.drivesmart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.drivesmart.enums.Role;

/**
 * Foydalanuvchi yangilash uchun DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDto {

    @NotBlank(message = "Ism majburiy")
    @Size(max = 50, message = "Ism 50 ta belgidan oshmasligi kerak")
    private String firstName;

    @Size(max = 50, message = "Familiya 50 ta belgidan oshmasligi kerak")
    private String lastName;

    @Pattern(regexp = "^998[0-9]{9}$", message = "Telefon raqami noto'g'ri formatda")
    private String phoneNumber;

    @Email(message = "Email noto'g'ri formatda")
    @Schema(example = "mirabbos@example.com", description = "Email manzili")
    @Column(name = "email", unique = true, nullable = true)
    private String email;

    @NotNull(message = "Rol majburiy")
    private Role role;
}