package uz.drivesmart.config;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.drivesmart.constants.ApiConstants;
import uz.drivesmart.entity.User;
import uz.drivesmart.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createSuperAdminIfNotExists();
    }

    private void createSuperAdminIfNotExists() {
        if (userRepository.findByEmail("superadmin@drivesmart.uz").isEmpty()) {
            User superAdmin = new User();
            superAdmin.setEmail("superadmin@drivesmart.uz");
            superAdmin.setPasswordHash(passwordEncoder.encode("superadmin123")); // Parolni o'zgartiring
            superAdmin.setRole(ApiConstants.ROLE_SUPER_ADMIN);
            userRepository.save(superAdmin);
            System.out.println("SuperAdmin yaratildi: email=superadmin@drivesmart.uz, parol=superadmin123");
        } else {
            System.out.println("SuperAdmin allaqachon mavjud");
        }
    }
}