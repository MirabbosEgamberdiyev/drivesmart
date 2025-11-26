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
 * Foydalanuvchi boshqaruvi uchun service
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
     * Barcha active foydalanuvchilarni olish
     *
     * @return Active foydalanuvchilar ro'yxati
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllActiveUsers() {
        log.info("Fetching all active users");

        List<User> users = userRepository.findAllActive();
        return userMapper.toResponseDtoList(users);
    }

    /**
     * Yangi foydalanuvchi yaratish
     *
     * @param request Foydalanuvchi ma'lumotlari
     * @return Yaratilgan foydalanuvchi ma'lumotlari
     * @throws BusinessException Agar telefon raqami allaqachon mavjud bo'lsa
     */
    public UserResponseDto createUser(UserRequestDto request) {
        log.info("Creating new user with phone: {}", request.getPhoneNumber());

        // Telefon raqami unique ekanligini tekshirish
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException("Bu telefon raqami allaqachon ro'yxatdan o'tgan");
        }

        // Parol validation
        if (!ValidationUtil.isStrongPassword(request.getPassword())) {
            throw new BusinessException("Parol kuchsiz. Kamida 6 ta belgi, katta va kichik harf, raqam bo'lishi kerak");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);

        user = userRepository.save(user);

        log.info("User created with ID: {}", user.getId());
        return userMapper.toResponseDto(user);
    }

    /**
     * Foydalanuvchini yangilash
     *
     * @param id Foydalanuvchi ID
     * @param request Yangilash ma'lumotlari
     * @return Yangilangan foydalanuvchi ma'lumotlari
     */
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto request) {
        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        // Telefon raqami unique ekanligini tekshirish (yangilanish paytida)
        if (userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)) {
            throw new BusinessException("Bu telefon raqami allaqachon ro'yxatdan o'tgan");
        }
        // Telefon raqami unique ekanligini tekshirish (yangilanish paytida)
        if (userRepository.findByEmail(request.getEmail())) {
            throw new BusinessException("Bu email allaqachon ro'yxatdan o'tgan");
        }

        userMapper.updateEntityFromDto(request, user);
        user = userRepository.save(user);

        log.info("User updated with ID: {}", id);
        return userMapper.toResponseDto(user);
    }

    /**
     * Foydalanuvchini o'chirish (soft delete)
     *
     * @param id Foydalanuvchi ID
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
     * Foydalanuvchini ID bo'yicha olish
     *
     * @param id Foydalanuvchi ID
     * @return Foydalanuvchi ma'lumotlari
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
     * Rol bo'yicha foydalanuvchilarni olish
     *
     * @param role Foydalanuvchi roli
     * @return Belgilangan roldagi foydalanuvchilar
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByRole(Role role) {
        log.info("Fetching users by role: {}", role);

        List<User> users = userRepository.findActiveByRole(role);
        return userMapper.toResponseDtoList(users);
    }

    /**
     * Foydalanuvchi holatini o'zgartirish (active/inactive)
     *
     * @param id Foydalanuvchi ID
     * @param isActive Yangi holat
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
     * Foydalanuvchi parolini reset qilish
     *
     * @param id Foydalanuvchi ID
     * @param newPassword Yangi parol
     */
    public void resetUserPassword(Long id, String newPassword) {
        log.info("Resetting password for user ID: {}", id);

        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        if (!ValidationUtil.isStrongPassword(newPassword)) {
            throw new BusinessException("Parol kuchsiz. Kamida 6 ta belgi, katta va kichik harf, raqam bo'lishi kerak");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset for user ID: {}", id);
    }
}