package uz.drivesmart.service.impl;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.drivesmart.dto.mapper.UserMapper;
import uz.drivesmart.dto.request.LoginRequestDto;
import uz.drivesmart.dto.request.RegisterRequestDto;
import uz.drivesmart.dto.response.AuthResponseDto;
import uz.drivesmart.dto.response.UserResponseDto;
import uz.drivesmart.entity.User;
import uz.drivesmart.enums.Role;
import uz.drivesmart.exception.BusinessException;
import uz.drivesmart.exception.CustomException;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.UserRepository;
import uz.drivesmart.security.UserPrincipal;
import uz.drivesmart.security.jwt.JwtTokenProvider;
import uz.drivesmart.util.ValidationUtil;


/**
 * Authentication va authorization service
 */
@Service
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userMapper = userMapper;
    }

    /**
     * Foydalanuvchi tizimga kirishi
     *
     * @param request Login ma'lumotlari
     * @return JWT token va foydalanuvchi ma'lumotlari
     * @throws BusinessException Agar login muvaffaqiyatsiz bo'lsa
     */
    public AuthResponseDto login(LoginRequestDto request) {
        log.info("Login attempt for phone number: {}", request.getPhoneNumber());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new BusinessException("Telefon raqami yoki parol noto'g'ri"));

        if (!user.getIsActive()) {
            throw new BusinessException("Foydalanuvchi hisob qaydnomasi faol emas");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Telefon raqami yoki parol noto'g'ri");
        }

        String accessToken = tokenProvider.generateToken(user.getId(), user.getRole());

        log.info("User {} successfully logged in", user.getId());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .expiresIn(tokenProvider.getExpirationTime())
                .user(userMapper.toResponseDto(user))
                .build();
    }

    /**
     * Foydalanuvchi parolini o'zgartirish
     *
     * @param userId Foydalanuvchi ID
     * @param currentPassword Joriy parol
     * @param newPassword Yangi parol
     * @throws BusinessException Agar joriy parol noto'g'ri bo'lsa
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Password change request for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Joriy parol noto'g'ri");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password successfully changed for user: {}", userId);
    }

    /**
     * JWT token orqali foydalanuvchi ma'lumotlarini olish
     *
     * @return Foydalanuvchi ma'lumotlari
     */
    public UserResponseDto getCurrentUser(Authentication authentication) throws CustomException {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new CustomException("Authentication required");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Token Authentication obyektidan oling
        String token = null;
        if (authentication.getCredentials() instanceof String) {
            token = (String) authentication.getCredentials();
        }

        // Yoki UserPrincipal'dan user ID'ni to'g'ridan-to'g'ri oling
        Long userId = userPrincipal.getId();

        User user = userRepository.findById(userId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        return userMapper.toResponseDto(user);
    }

    /**
     * Yangi foydalanuvchi ro'yxatdan o'tishi
     * Role avtomatik USER qilib o'rnatiladi
     */
    public AuthResponseDto register(RegisterRequestDto request) {
        log.info("Registration attempt for phone: {}", request.getPhoneNumber());

        // Telefon raqami unique ekanligini tekshirish
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException("Bu telefon raqami allaqachon ro'yxatdan o'tgan");
        }

        // Parol validation
        if (!ValidationUtil.isStrongPassword(request.getPassword())) {
            throw new BusinessException("Parol kuchsiz. Kamida 6 ta belgi, katta va kichik harf, raqam bo'lishi kerak");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); // ✅ Har doim USER
        user.setIsActive(true);

        user = userRepository.save(user);

        // JWT token yaratish
        String accessToken = tokenProvider.generateToken(user.getId(), user.getRole());

        log.info("User registered successfully with ID: {}", user.getId());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .expiresIn(tokenProvider.getExpirationTime())
                .user(userMapper.toResponseDto(user))
                .build();
    }
}