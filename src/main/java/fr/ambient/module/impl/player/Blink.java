package fr.ambient.module.impl.player;

import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;


public class Blink extends Module {

    public Blink() {
        super(28, "Temporarily chokes your packets, then sends them all at once.", ModuleCategory.PLAYER);
    }


    public void onEnable() {
        BlinkComponent.onEnable();
    }

    public void onDisable() {
        BlinkComponent.onDisable();
    }
}