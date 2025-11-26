package uz.drivesmart.enums;


/**
 * Tasdiqlash turi enum
 */
public enum VerificationType {
    SMS("SMS orqali tasdiqlash"),
    EMAIL("Email orqali tasdiqlash");

    private final String displayName;

    VerificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
