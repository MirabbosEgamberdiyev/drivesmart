package uz.drivesmart.enums;

public enum PaymentStatus {
    PENDING("Kutilmoqda"),
    PROCESSING("Jarayonda"),
    COMPLETED("Muvaffaqiyatli"),
    FAILED("Muvaffaqiyatsiz"),
    CANCELLED("Bekor qilingan"),
    REFUNDED("Qaytarilgan"),
    AWAITING_CONFIRMATION("Admin tasdiqini kutmoqda"); // Karta to'lovi uchun

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}