package uz.drivesmart.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddAdminRequest(
        @NotBlank(message = "Email kiritilishi shart")
        @Email(message = "To'g'ri email kiriting")
        String email,

        @NotBlank(message = "Parol kiritilishi shart")
        @Size(min = 8, message = "Parol kamida 8 belgidan iborat")
        String password
) {}