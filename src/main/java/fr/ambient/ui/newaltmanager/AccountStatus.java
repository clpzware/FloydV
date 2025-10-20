package fr.ambient.ui.newaltmanager;

public enum AccountStatus {
    UNKNOWN("Unknown - Log in to check"),
    UNBANNED("§2Unbanned §8- May be inaccurate"),
    BANNED("§cBanned §8- %s remaining");

    private final String message;

    AccountStatus(String message) {
        this.message = message;
    }

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}
