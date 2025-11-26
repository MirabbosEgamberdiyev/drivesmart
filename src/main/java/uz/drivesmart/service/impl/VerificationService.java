package uz.drivesmart.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.entity.VerificationCode;
import uz.drivesmart.enums.VerificationType;
import uz.drivesmart.exception.BusinessException;
import uz.drivesmart.repository.VerificationCodeRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Tasdiqlash kodlari boshqaruvi
 */
@Service
@Slf4j
@Transactional
public class VerificationService {

    private static final int CODE_LENGTH = 6;
    private static final int MAX_CODES_PER_HOUR = 5;
    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int RETRY_DELAY_SECONDS = 60;

    private final VerificationCodeRepository verificationRepository;
    private final EskizSmsService smsService;
    private final EmailService emailService;
    private final SecureRandom random;

    public VerificationService(VerificationCodeRepository verificationRepository,
                               EskizSmsService smsService,
                               EmailService emailService) {
        this.verificationRepository = verificationRepository;
        this.smsService = smsService;
        this.emailService = emailService;
        this.random = new SecureRandom();
    }

    /**
     * Tasdiqlash kodini yaratish va yuborish
     */
    public void sendVerificationCode(String recipient, VerificationType type) {
        // Rate limiting - 1 soatda maksimal 5 ta kod
        checkRateLimit(recipient, type);

        // Kod generatsiya qilish
        String code = generateCode();

        // Bazaga saqlash
        VerificationCode verificationCode = VerificationCode.builder()
                .code(code)
                .recipient(recipient)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .build();

        verificationRepository.save(verificationCode);

        // Yuborish
        if (type == VerificationType.SMS) {
            String message = String.format("DriveSmart tasdiqlash kodi: %s\nKod %d daqiqa amal qiladi.", code, CODE_EXPIRY_MINUTES);
            smsService.sendSms(recipient, message);
        } else {
            emailService.sendVerificationCode(recipient, code);
        }

        log.info("Tasdiqlash kodi yuborildi: {} ({})", recipient, type);
    }

    /**
     * Tasdiqlash kodini tekshirish
     */
    public boolean verifyCode(String recipient, String code, VerificationType type) {
        VerificationCode verificationCode = verificationRepository
                .findByRecipientAndCodeAndType(recipient, code, type, LocalDateTime.now())
                .orElse(null);

        if (verificationCode == null) {
            log.warn("Tasdiqlash kodi topilmadi yoki muddati o'tgan: {}", recipient);
            return false;
        }

        if (verificationCode.isMaxAttemptsReached()) {
            throw new BusinessException("Maksimal urinishlar soni oshdi. Yangi kod so'rang.");
        }

        verificationCode.incrementAttempts();
        verificationRepository.save(verificationCode);

        if (!verificationCode.getCode().equals(code)) {
            log.warn("Noto'g'ri tasdiqlash kodi: {}", recipient);
            return false;
        }

        // Kod to'g'ri - ishlatilgan deb belgilaymiz
        verificationCode.markAsUsed();
        verificationRepository.save(verificationCode);

        log.info("Tasdiqlash kodi to'g'ri tasdiqlandi: {}", recipient);
        return true;
    }

    /**
     * Rate limiting tekshiruvi
     */
    private void checkRateLimit(String recipient, VerificationType type) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentCount = verificationRepository.countRecentCodes(recipient, type, oneHourAgo);

        if (recentCount >= MAX_CODES_PER_HOUR) {
            throw new BusinessException(
                    String.format("Soatiga maksimal %d ta kod yuborish mumkin. Keyinroq urinib ko'ring.", MAX_CODES_PER_HOUR)
            );
        }
    }

    /**
     * 6 xonali tasdiqlash kodini generatsiya qilish
     */
    private String generateCode() {
        int code = random.nextInt(900000) + 100000; // 100000 - 999999
        return String.valueOf(code);
    }

    /**
     * Har kuni soat 3:00 da muddati o'tgan kodlarni o'chirish
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredCodes() {
        log.info("Muddati o'tgan tasdiqlash kodlarini tozalash...");
        verificationRepository.deleteExpiredCodes(LocalDateTime.now());
        log.info("Tozalash tugadi");
    }
}