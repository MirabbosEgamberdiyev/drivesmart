package uz.drivesmart.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.mapper.UserMapper;
import uz.drivesmart.dto.request.UserRequestDto;
import uz.drivesmart.dto.request.UserUpdateRequestDto;
import uz.drivesmart.dto.response.UserResponseDto;
import uz.drivesmart.entity.User;
import uz.drivesmart.enums.Role;
import uz.drivesmart.exception.BusinessException;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.UserRepository;
import uz.drivesmart.util.ValidationUtil;

import java.util.List;

/**
 * ✅ 100% Working User Service
 */
@Service
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * ✅ Barcha active foydalanuvchilarni olish
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllActiveUsers() {
        log.info("Fetching all active users");

        List<User> users = userRepository.findAllActive();
        return userMapper.toResponseDtoList(users);
    }

    /**
     * ✅ Yangi foydalanuvchi yaratish
     */
    public UserResponseDto createUser(UserRequestDto request) {
        log.info("Creating new user with phone: {} / email: {}",
                request.getPhoneNumber(), request.getEmail());

        // ✅ Validate phone number if provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (!ValidationUtil.isValidPhoneNumber(request.getPhoneNumber())) {
                throw new BusinessException("Telefon raqami noto'g'ri formatda");
            }
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BusinessException("Bu telefon raqami allaqachon ro'yxatdan o'tgan");
            }
        }

        // ✅ Validate email if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!ValidationUtil.isValidEmail(request.getEmail())) {
                throw new BusinessException("Email noto'g'ri formatda");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Bu email allaqachon ro'yxatdan o'tgan");
            }
        }

        // ✅ At least one contact method required
        if ((request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) &&
                (request.getEmail() == null || request.getEmail().isBlank())) {
            throw new BusinessException("Telefon raqami yoki email kiritilishi shart");
        }

        // ✅ Password validation
        if (!ValidationUtil.isStrongPassword(request.getPassword())) {
            throw new BusinessException(
                    "Parol kuchsiz. Kamida 8 ta belgi, katta/kichik harf, raqam va maxsus belgi bo'lishi kerak"
            );
        }

        // ✅ Create user
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);

        user = userRepository.save(user);

        log.info("User created with ID: {}", user.getId());
        return userMapper.toResponseDto(user);
    }

    /**
     * ✅ Foydalanuvchini yangilash
     */
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        // ✅ Validate and check phone uniqueness
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (!ValidationUtil.isValidPhoneNumber(request.getPhoneNumber())) {
                throw new BusinessException("Telefon raqami noto'g'ri formatda");
            }
            if (!request.getPhoneNumber().equals(user.getPhoneNumber()) &&
                    userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)) {
                throw new BusinessException("Bu telefon raqami allaqachon ro'yxatdan o'tgan");
            }
        }

        // ✅ Validate and check email uniqueness
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!ValidationUtil.isValidEmail(request.getEmail())) {
                throw new BusinessException("Email noto'g'ri formatda");
            }
            if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new BusinessException("Bu email allaqachon ro'yxatdan o'tgan");
            }
        }

        // ✅ At least one contact method required
        if ((request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) &&
                (request.getEmail() == null || request.getEmail().isBlank())) {
            throw new BusinessException("Telefon raqami yoki email kiritilishi shart");
        }

        // ✅ Update user
        userMapper.updateEntityFromDto(request, user);
        user = userRepository.save(user);

        log.info("User updated with ID: {}", id);
        return userMapper.toResponseDto(user);
    }

    /**
     * ✅ Foydalanuvchini o'chirish (soft delete)
     */
    public void deleteUser(Long id) {
        log.info("Soft deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        user.setIsDeleted(true);
        user.setIsActive(false);
        userRepository.save(user);

        log.info("User soft deleted with ID: {}", id);
    }

    /**
     * ✅ Foydalanuvchini ID bo'yicha olish
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        return userMapper.toResponseDto(user);
    }

    /**
     * ✅ Rol bo'yicha foydalanuvchilarni olish
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByRole(Role role) {
        log.info("Fetching users by role: {}", role);

        List<User> users = userRepository.findActiveByRole(role);
        return userMapper.toResponseDtoList(users);
    }

    /**
     * ✅ Foydalanuvchi holatini o'zgartirish (active/inactive)
     */
    public void toggleUserStatus(Long id, boolean isActive) {
        log.info("Toggling user status for ID: {} to {}", id, isActive);

        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        user.setIsActive(isActive);
        userRepository.save(user);

        log.info("User status toggled for ID: {}", id);
    }

    /**
     * ✅ Foydalanuvchi parolini reset qilish
     */
    public void resetUserPassword(Long id, String newPassword) {
        log.info("Resetting password for user ID: {}", id);

        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        // ✅ Validate password
        if (!ValidationUtil.isStrongPassword(newPassword)) {
            throw new BusinessException(
                    "Parol kuchsiz. Kamida 8 ta belgi, katta/kichik harf, raqam va maxsus belgi bo'lishi kerak"
            );
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset for user ID: {}", id);
    }
}