package fr.ambient.ui.newaltmanager;

public enum AltType {

    MICROSOFT("microsoft"),
    COOKIE("cookie"),
    CRACKED("cracked"),
    SESSION("session"),
    DOG("microsoft");

    private final String icon;

    AltType(final String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

}
