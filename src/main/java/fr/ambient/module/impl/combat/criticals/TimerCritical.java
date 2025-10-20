package fr.ambient.module.impl.combat.criticals;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.KillAura;



public class TimerCritical extends ModuleMode {
    public TimerCritical(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onPlayerNetworkTick(PreMotionEvent event) {
        if (mc.thePlayer.onGround && KillAura.target != null) {
            mc.thePlayer.jump();
        }
    }
}