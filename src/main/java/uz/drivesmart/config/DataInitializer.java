package uz.drivesmart.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.drivesmart.entity.User;
import uz.drivesmart.enums.Role;
import uz.drivesmart.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String superAdminPhone = "998889996499";
        String superAdminPassword = "Admin.123$";

        boolean exists = userRepository.existsByPhoneNumber(superAdminPhone);

        if (!exists) {
            User superAdmin = new User();
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setPhoneNumber(superAdminPhone);
            superAdmin.setPasswordHash(passwordEncoder.encode(superAdminPassword));
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setIsActive(true);

            userRepository.save(superAdmin);

            System.out.println("✅ SUPER_ADMIN yaratildi: " + superAdminPhone + " / " + superAdminPassword);
        } else {
            System.out.println("ℹ️ SUPER_ADMIN allaqachon mavjud.");
        }
    }
}
