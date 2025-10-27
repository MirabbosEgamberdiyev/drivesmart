package uz.drivesmart.enums;

/**
 * Foydalanuvchi rollari enum'i
 */
public enum Role {
    SUPER_ADMIN("Super Admin"),
    ADMIN("Administrator"),
    USER("User");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}