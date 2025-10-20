package fr.ambient.module.impl.player.antivoid;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.player.AntiVoid;
import fr.ambient.util.player.PlayerUtil;

public class PositionAntivoid extends ModuleMode {
    public PositionAntivoid(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        AntiVoid av = (AntiVoid) this.getSuperModule();
        if (mc.thePlayer.fallDistance > av.distance.getValue() && !PlayerUtil.isBlockUnder(50)) {
            event.setPosY(event.getPosY() + mc.thePlayer.fallDistance);
        }
    }
}