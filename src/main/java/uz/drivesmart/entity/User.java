package uz.drivesmart.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.drivesmart.enums.Role;

/**
 * Tizim foydalanuvchilari entity'si
 * Authentication va role-based authorization uchun ishlatiladi
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_phone", columnList = "phone_number"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_deleted", columnList = "is_deleted")
})
public class User extends BaseEntity {

    @NotBlank(message = "Ism majburiy")
    @Size(max = 50, message = "Ism 50 ta belgidan oshmasligi kerak")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Size(max = 50, message = "Familiya 50 ta belgidan oshmasligi kerak")
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Pattern(regexp = "^998[0-9]{9}$", message = "Telefon raqami noto'g'ri formatda")
    @Column(name = "phone_number", unique = true, nullable = true, length = 15)
    private String phoneNumber;

    @Email(message = "Email noto'g'ri formatda")
    @Schema(example = "mirabbos@example.com", description = "Email manzili")
    @Column(name = "email", unique = true, nullable = true, length = 100)
    private String email;

    @NotBlank(message = "Parol majburiy")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * VALIDATSIYA: Kamida bittasi bo'lishi kerak
     */
    @PrePersist
    @PreUpdate
    private void validateUser() {
        if ((phoneNumber == null || phoneNumber.trim().isEmpty()) &&
                (email == null || email.trim().isEmpty())) {
            throw new IllegalStateException("Telefon raqami yoki emaildan kamida biri to'ldirilishi shart");
        }
    }
}