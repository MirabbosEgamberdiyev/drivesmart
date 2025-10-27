package uz.drivesmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDto {

    @NotBlank(message = "Ism majburiy")
    @Size(min = 2, max = 50, message = "Ism 2-50 belgi oralig'ida bo'lishi kerak")
    private String firstName;

    @Size(max = 50, message = "Familiya 50 ta belgidan oshmasligi kerak")
    private String lastName;

    @NotBlank(message = "Telefon raqami majburiy")
    @Pattern(regexp = "^998[0-9]{9}$", message = "Telefon raqami noto'g'ri formatda")
    private String phoneNumber;

    @NotBlank(message = "Parol majburiy")
    @Size(min = 6, max = 50, message = "Parol 6-50 belgi oralig'ida bo'lishi kerak")
    private String password;
}