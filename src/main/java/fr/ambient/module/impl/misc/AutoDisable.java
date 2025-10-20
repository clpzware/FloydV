package fr.ambient.module.impl.misc;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.world.WorldChangeEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.module.impl.player.Breaker;
import fr.ambient.property.impl.MultiProperty;

public class AutoDisable extends Module {

    public AutoDisable() {
        super(119, "Automatically Disable Some Module", ModuleCategory.MISC);
        registerProperties(when, module);
    }

    public MultiProperty when = MultiProperty.newInstance("Auto Disable Options", new String[]{"World Change", "On Death"});
    public MultiProperty module = MultiProperty.newInstance("Modules", new String[]{"Breaker", "Killaura"});

    @SubscribeEvent
    private void onWorldChange(WorldChangeEvent event) {
        if (when.isSelected("World Change")) {
            disableSelectedModules();
        }
    }

    @SubscribeEvent
    private void onUpdate(UpdateEvent event) {
        if (when.isSelected("On Death") && mc.thePlayer != null && mc.thePlayer.getHealth() <= 0) {
            disableSelectedModules();
        }
    }

    private void disableSelectedModules() {
        if (module.isSelected("Killaura")) {
            Module killAuraModule = Ambient.getInstance().getModuleManager().getModule(KillAura.class);
            if (killAuraModule != null) {
                killAuraModule.setEnabled(false);
            }
        }
        if (module.isSelected("Breaker")) {
            Module breakerModule = Ambient.getInstance().getModuleManager().getModule(Breaker.class);
            if (breakerModule != null) {
                breakerModule.setEnabled(false);
            }
        }
    }
}
