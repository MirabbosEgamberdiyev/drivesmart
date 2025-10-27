package uz.drivesmart.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Validation utility class
 */
@Component
public class ValidationUtil {

    /**
     * Telefon raqami formatini tekshirish
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // UZ telefon raqami formati: 998XXXXXXXXX
        String pattern = "^998[0-9]{9}$";
        return phoneNumber.matches(pattern);
    }

    /**
     * Email formatini tekshirish
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(pattern);
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
     * Parol kuchliligini tekshirish
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        // Kamida bitta katta harf, kichik harf va raqam bo'lishi kerak
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasUppercase && hasLowercase && hasDigit;
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