package uz.drivesmart.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.mapper.UserMapper;
import uz.drivesmart.dto.request.*;
import uz.drivesmart.dto.response.AuthResponseDto;
import uz.drivesmart.dto.response.UserResponseDto;
import uz.drivesmart.dto.response.VerificationSentResponseDto;
import uz.drivesmart.entity.RefreshToken;
import uz.drivesmart.entity.User;
import uz.drivesmart.enums.Role;
import uz.drivesmart.enums.VerificationType;
import uz.drivesmart.exception.BusinessException;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.RefreshTokenRepository;
import uz.drivesmart.repository.UserRepository;
import uz.drivesmart.security.UserPrincipal;
import uz.drivesmart.security.jwt.JwtTokenProvider;
import uz.drivesmart.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mukammal Authentication Service
 */
@Service
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final VerificationService verificationService;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       UserMapper userMapper,
                       VerificationService verificationService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userMapper = userMapper;
        this.verificationService = verificationService;
    }

    // ==================== REGISTRATION ====================

    /**
     * Ro'yxatdan o'tish boshlang'ich so'rov
     * Tasdiqlash kodi yuboradi
     */
    public VerificationSentResponseDto initiateRegistration(RegisterInitRequestDto request) {
        log.info("Registration initiated for: {}", getRecipient(request));

        // Telefon/Email unique ekanligini tekshirish
        if (request.getVerificationType() == VerificationType.SMS) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new BusinessException("Bu telefon raqami allaqachon ro'yxatdan o'tgan");
            }
        } else {
            // Email uchun ham qo'shing
            // if (userRepository.existsByEmail(request.getEmail())) { ... }
        }

        // Parol validation
        if (!ValidationUtil.isStrongPassword(request.getPassword())) {
            throw new BusinessException("Parol kuchsiz. Kamida 6 ta belgi, katta va kichik harf, raqam bo'lishi kerak");
        }

        // Tasdiqlash kodi yuborish
        String recipient = getRecipient(request);
        verificationService.sendVerificationCode(recipient, request.getVerificationType());

        return VerificationSentResponseDto.builder()
                .recipient(maskRecipient(recipient))
                .expiresInMinutes(5)
                .retryAfterSeconds(60)
                .message("Tasdiqlash kodi yuborildi")
                .build();
    }

    /**
     * Ro'yxatdan o'tishni tasdiqlash va yakunlash
     */
    public AuthResponseDto completeRegistration(RegisterInitRequestDto request, String code) {
        log.info("Completing registration for: {}", getRecipient(request));

        String recipient = getRecipient(request);

        // Tasdiqlash kodini tekshirish
        if (!verificationService.verifyCode(recipient, code, request.getVerificationType())) {
            throw new BusinessException("Tasdiqlash kodi noto'g'ri yoki muddati o'tgan");
        }

        // Foydalanuvchi yaratish
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(true);

        user = userRepository.save(user);

        // Tokenlar yaratish
        return generateAuthResponse(user);
    }

    // ==================== LOGIN ====================

    /**
     * Login qilish (telefon yoki email orqali)
     */
    public AuthResponseDto login(LoginRequestDto request) {
        log.info("Login attempt for: {}", request.getPhoneNumber() != null ? request.getPhoneNumber() : request.getEmail());

        User user;
        if (request.getPhoneNumber() != null) {
            user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new BusinessException("Telefon raqami yoki parol noto'g'ri"));
        } else {
            // Email login qo'shing
            throw new BusinessException("Email login hozircha qo'llab-quvvatlanmaydi");
        }

        if (!user.getIsActive()) {
            throw new BusinessException("Foydalanuvchi hisob qaydnomasi faol emas");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Telefon raqami yoki parol noto'g'ri");
        }

        log.info("User {} successfully logged in", user.getId());

        return generateAuthResponse(user);
    }

    // ==================== PASSWORD MANAGEMENT ====================

    /**
     * Parolni unutdim - tasdiqlash kodi yuborish
     */
    public VerificationSentResponseDto forgotPassword(ForgotPasswordRequestDto request) {
        String recipient = request.getVerificationType() == VerificationType.SMS
                ? request.getPhoneNumber()
                : request.getEmail();

        log.info("Forgot password request for: {}", recipient);

        // Foydalanuvchi mavjudligini tekshirish
        if (request.getVerificationType() == VerificationType.SMS) {
            userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));
        }

        // Tasdiqlash kodi yuborish
        verificationService.sendVerificationCode(recipient, request.getVerificationType());

        return VerificationSentResponseDto.builder()
                .recipient(maskRecipient(recipient))
                .expiresInMinutes(5)
                .retryAfterSeconds(60)
                .message("Tasdiqlash kodi yuborildi")
                .build();
    }

    /**
     * Parolni tiklash (kod tasdiqlashdan keyin)
     */
    public void resetPassword(ResetPasswordRequestDto request) {
        log.info("Password reset for: {}", request.getRecipient());

        // Tasdiqlash kodini tekshirish
        if (!verificationService.verifyCode(request.getRecipient(), request.getCode(), request.getVerificationType())) {
            throw new BusinessException("Tasdiqlash kodi noto'g'ri yoki muddati o'tgan");
        }

        // Parol validation
        if (!ValidationUtil.isStrongPassword(request.getNewPassword())) {
            throw new BusinessException("Parol kuchsiz. Kamida 6 ta belgi, katta va kichik harf, raqam bo'lishi kerak");
        }

        // Foydalanuvchi topish va parolni yangilash
        User user;
        if (request.getVerificationType() == VerificationType.SMS) {
            user = userRepository.findByPhoneNumber(request.getRecipient())
                    .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));
        } else {
            throw new BusinessException("Email reset hozircha qo'llab-quvvatlanmaydi");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Barcha tokenlarni bekor qilish
        refreshTokenRepository.revokeAllByUserId(user.getId(), LocalDateTime.now());

        log.info("Password reset successfully for user: {}", user.getId());
    }

    /**
     * Parolni o'zgartirish (autentifikatsiya qilingan)
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Password change request for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Joriy parol noto'g'ri");
        }

        if (!ValidationUtil.isStrongPassword(newPassword)) {
            throw new BusinessException("Parol kuchsiz. Kamida 6 ta belgi, katta va kichik harf, raqam bo'lishi kerak");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password successfully changed for user: {}", userId);
    }

    // ==================== TOKEN MANAGEMENT ====================

    /**
     * Refresh token orqali yangi access token olish
     */
    public AuthResponseDto refreshToken(String refreshTokenStr) {
        log.info("Refresh token request");

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenStr, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("Token yaroqsiz yoki muddati o'tgan"));

        User user = refreshToken.getUser();

        if (!user.getIsActive()) {
            throw new BusinessException("Foydalanuvchi faol emas");
        }

        String newAccessToken = tokenProvider.generateToken(user.getId(), user.getRole());

        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime())
                .user(userMapper.toResponseDto(user))
                .build();
    }

    /**
     * Logout - refresh tokenni bekor qilish
     */
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr, LocalDateTime.now())
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                    log.info("User logged out: {}", token.getUser().getId());
                });
    }


    // ==================== USER INFO ====================

    /**
     * Joriy foydalanuvchi ma'lumotlarini olish
     */
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new BusinessException("Authentication required");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        User user = userRepository.findById(userId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        return userMapper.toResponseDto(user);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Auth response yaratish (access + refresh token)
     */
    private AuthResponseDto generateAuthResponse(User user) {
        String accessToken = tokenProvider.generateToken(user.getId(), user.getRole());
        String refreshTokenStr = UUID.randomUUID().toString();

        // Refresh token saqlash
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime())
                .user(userMapper.toResponseDto(user))
                .build();
    }

    /**
     * Recipient olish (telefon yoki email)
     */
    private String getRecipient(RegisterInitRequestDto request) {
        return request.getVerificationType() == VerificationType.SMS
                ? request.getPhoneNumber()
                : request.getEmail();
    }

    /**
     * Recipient'ni mask qilish (xavfsizlik uchun)
     */
    private String maskRecipient(String recipient) {
        if (recipient.length() > 8) {
            return recipient.substring(0, 4) + "****" + recipient.substring(recipient.length() - 2);
        }
        return recipient;
    }
}