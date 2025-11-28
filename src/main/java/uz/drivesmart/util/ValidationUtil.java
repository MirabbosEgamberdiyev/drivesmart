package uz.drivesmart.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Validation utility class
 */
@Component
public class ValidationUtil {
    // ✅ OWASP-compliant password regex
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("^998[0-9]{9}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Password strength checker
     * Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character (@$!%*?&#)
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }

        // Length check
        if (password.length() < 8) {
            return false;
        }

        // Complexity check
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.matches(".*[@$!%*?&#].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Phone number validation
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * Email validation
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Get password strength level (for UI feedback)
     */
    public static PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.WEAK;
        }

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[@$!%*?&#].*")) score++;

        if (score <= 2) return PasswordStrength.WEAK;
        if (score <= 4) return PasswordStrength.MEDIUM;
        return PasswordStrength.STRONG;
    }

    /**
     * Mask sensitive data
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "**@" + domain;
        }

        return localPart.substring(0, 2) + "***@" + domain;
    }

    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }

    /**
     * Barcode formatini tekshirish (EAN-13)
     */
    public static boolean isValidBarcode(String barcode) {
        if (barcode == null || barcode.length() != 13) {
            return false;
        }

        if (!barcode.matches("\\d{13}")) {
            return false;
        }

        return isValidEAN13CheckDigit(barcode);
    }

    /**
     * EAN-13 check digit ni tekshirish
     */
    private static boolean isValidEAN13CheckDigit(String barcode) {
        String first12 = barcode.substring(0, 12);
        int expectedCheckDigit = Integer.parseInt(barcode.substring(12));

        int sum = 0;
        for (int i = 0; i < first12.length(); i++) {
            int digit = Character.getNumericValue(first12.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }

        int remainder = sum % 10;
        int calculatedCheckDigit = (remainder == 0) ? 0 : 10 - remainder;

        return calculatedCheckDigit == expectedCheckDigit;
    }

    /**
     * Decimal qiymatni tekshirish
     */
    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Musbat decimal qiymatni tekshirish
     */
    public static boolean isPositiveAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * String bo'sh emasligini tekshirish
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Collection bo'sh emasligini tekshirish
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}