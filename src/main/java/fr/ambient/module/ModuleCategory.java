package fr.ambient.module;

import fr.ambient.structure.interfaces.Nameable;

public enum ModuleCategory implements Nameable {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    MISC("Misc");

    private final String name;

    ModuleCategory(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}