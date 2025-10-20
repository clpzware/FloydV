package fr.ambient.module.impl.movement;


import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.movement.invmove.*;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;




public class InvMove extends Module {

    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Watchdog",
            new BlinkInvmove("Blink", this),
            new Watchdoginvmove("Watchdog", this),
            new SlowdownInvmove("Slowdown", this),
            new GrimInvmove("Grim", this),
            new VulcanInvmove("Vulcan", this)
    );


    public InvMove() {
        super(12, "Lets you move while your inventory is open.", ModuleCategory.MOVEMENT);
        registerProperties(mode.getModeProperty());
        this.moduleModeProperties.add(mode);
        this.setSuffix(mode.getModeProperty()::getValue);
    }
}