package fr.ambient.module.impl.combat.criticals;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Criticals;
import fr.ambient.util.player.MoveUtil;


public class StillCritical extends ModuleMode {

    private final Criticals crit = (Criticals) this.getSuperModule();

    public StillCritical(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onPlayerNetworkTick(PreMotionEvent event) {
        if (!MoveUtil.moving()) {
            if (crit.go) {
                event.setOnGround(false);
            } else {
                if (!mc.thePlayer.onGround) {
                    crit.go = true;
                }
            }
        } else {
            crit.go = false;
        }
    }
}