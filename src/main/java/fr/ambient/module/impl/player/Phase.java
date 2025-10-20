package fr.ambient.module.impl.player;

import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.player.phase.Watchdog120phase;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;

public class Phase extends Module {
    public Phase() {
        super(116, "Allow u to phase through block", ModuleCategory.PLAYER);
        this.registerProperties(mode.getModeProperty());
        this.setSuffix(mode.getModeProperty()::getValue);
        this.moduleModeProperties.add(mode);
    }


    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Watchdog 1.20",
            new Watchdog120phase("Watchdog 1.20", this)
    );
}

