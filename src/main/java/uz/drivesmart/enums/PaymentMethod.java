package uz.drivesmart.enums;

public enum PaymentMethod {
    CLICK("Click to'lov"),
    PAYME("Payme to'lov"),
    UZUM("Uzum to'lov"),
    CARD_TRANSFER("Karta o'tkazmasi");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}