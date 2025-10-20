package fr.ambient.module.impl.combat.criticals;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Criticals;



public class JumpCritical extends ModuleMode {
    private final Criticals crit = (Criticals) this.getSuperModule();

    public JumpCritical(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onPlayerNetworkTick(PreMotionEvent event) {
        if (mc.thePlayer.motionY < 0 && !mc.thePlayer.onGround) {
            mc.timer.timerSpeed = crit.timer.getValue();
        } else {
            mc.timer.timerSpeed = 1f;
        }
    }
}