package uz.drivesmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.drivesmart.enums.Role;

/**
 * Foydalanuvchi yaratish/yangilash uchun request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {

    @NotBlank(message = "Ism majburiy")
    @Size(max = 50, message = "Ism 50 ta belgidan oshmasligi kerak")
    private String firstName;

    @Size(max = 50, message = "Familiya 50 ta belgidan oshmasligi kerak")
    private String lastName;

    @NotBlank(message = "Telefon raqami majburiy")
    @Pattern(regexp = "^998[0-9]{9}$", message = "Telefon raqami noto'g'ri formatda")
    private String phoneNumber;

    @NotBlank(message = "Parol majburiy")
    @Size(min = 6, max = 50, message = "Parol 6-50 belgi oralig'ida bo'lishi kerak")
    private String password;

    @NotNull(message = "Rol majburiy")
    private Role role;
}