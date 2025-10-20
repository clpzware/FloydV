package fr.ambient.module.impl.combat.velocity;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Velocity;


public class MushmcReverseVelocity extends ModuleMode {


    private final Velocity velocity = (Velocity) this.getSuperModule();

    public MushmcReverseVelocity(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent()
    private void onPlayerNetworkTickEvent(PreMotionEvent event) {
        if (mc.thePlayer.hurtTime != 0 && !mc.thePlayer.onGround) {
            mc.thePlayer.motionY -= 0.80;
        }
    }
}




