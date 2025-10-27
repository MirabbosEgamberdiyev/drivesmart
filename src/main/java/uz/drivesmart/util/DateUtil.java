package uz.drivesmart.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Date utility class
 */
@Component
public class DateUtil {

    private static final ZoneId TASHKENT_ZONE = ZoneId.of("Asia/Tashkent");

    /**
     * Joriy Toshkent vaqti
     */
    public static LocalDateTime nowInTashkent() {
        return LocalDateTime.now(TASHKENT_ZONE);
    }

    /**
     * Joriy Toshkent sanasi
     */
    public static LocalDate todayInTashkent() {
        return LocalDate.now(TASHKENT_ZONE);
    }

    /**
     * Kun boshi (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Kun oxiri (23:59:59)
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }

    /**
     * Hafta boshi (Dushanba)
     */
    public static LocalDate startOfWeek(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    /**
     * Hafta oxiri (Yakshanba)
     */
    public static LocalDate endOfWeek(LocalDate date) {
        return date.with(DayOfWeek.SUNDAY);
    }

    /**
     * Oy boshi
     */
    public static LocalDate startOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /**
     * Oy oxiri
     */
    public static LocalDate endOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * Yil boshi
     */
    public static LocalDate startOfYear(LocalDate date) {
        return date.withDayOfYear(1);
    }

    /**
     * Yil oxiri
     */
    public static LocalDate endOfYear(LocalDate date) {
        return date.withDayOfYear(date.lengthOfYear());
    }

    /**
     * Ikki sana oralig'idagi kunlar soni
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Sana formatini o'zgartirish
     */
    public static String formatDate(LocalDate date, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * Vaqt formatini o'zgartirish
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}