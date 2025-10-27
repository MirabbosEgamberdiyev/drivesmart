package uz.drivesmart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.drivesmart.enums.Role;

import java.util.HashSet;
import java.util.Set;

/**
 * Tizim foydalanuvchilari entity'si
 * Authentication va role-based authorization uchun ishlatiladi
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @NotBlank(message = "Ism majburiy")
    @Size(max = 50, message = "Ism 50 ta belgidan oshmasligi kerak")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Size(max = 50, message = "Familiya 50 ta belgidan oshmasligi kerak")
    @Column(name = "last_name", length = 50)
    private String lastName;

    @NotBlank(message = "Telefon raqami majburiy")
    @Pattern(regexp = "^998[0-9]{9}$", message = "Telefon raqami noto'g'ri formatda")
    @Column(name = "phone_number", unique = true, nullable = false, length = 15)
    private String phoneNumber;

    @NotBlank(message = "Parol majburiy")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Column(name = "is_active")
    private Boolean isActive = true;
}