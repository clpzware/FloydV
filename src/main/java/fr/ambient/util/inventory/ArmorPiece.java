package fr.ambient.util.inventory;

import lombok.Getter;

public enum ArmorPiece {
    HELMET(5, 0),
    CHESTPLATE(6, 1),
    LEGGINGS(7, 2),
    BOOTS(8, 3);

    @Getter
    private int slot, value;

    ArmorPiece(int slot, int value) {
        this.slot = slot;
        this.value = value;
    }

    public static ArmorPiece fromInt(int value) {
        for (ArmorPiece type : ArmorPiece.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid armor type value: " + value);
    }
}
