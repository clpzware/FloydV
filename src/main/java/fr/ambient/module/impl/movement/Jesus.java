package fr.ambient.module.impl.movement;


import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.movement.jesus.*;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;

public class Jesus extends Module {


    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Dolphin",
            new JesusDolphin("Watchdog Dolphin", this),
            new JesusWalk("Watchdog Walk", this),
            new JesusVerusFloat("Verus Float", this),
            new JesusVanilla("Vanilla", this),
            new JesusVulcan("Vulcan", this),
            new JesusBullet("Bullet", this)
    );

    public Jesus() {
        super(85, "Allows you to walk on water", ModuleCategory.MOVEMENT);
        registerProperties(mode.getModeProperty());
        this.moduleModeProperties.add(mode);
    }
}
