package fr.ambient.module.impl.player.scaffold.sprint;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

public class VanillaSprint extends ModuleMode {
    public VanillaSprint(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdate(UpdateEvent event) {
        mc.thePlayer.setSprinting(true);
    }
}
