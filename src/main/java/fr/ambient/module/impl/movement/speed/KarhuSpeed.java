package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

public class KarhuSpeed extends ModuleMode {

    private int karhujumps = 0;

    public KarhuSpeed(String modeName, Module module) {
        super(modeName, module);
    }


    @Override
    public void onDisable() {
        karhujumps = 0;
    }

    @SubscribeEvent
    private void onPlayerTick(UpdateEvent event) {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42;
            karhujumps++;
        }
        if (mc.thePlayer.airTicks == 1) {
            switch (karhujumps) {
                case 2:
//                    mc.thePlayer.motionX *= 1.125;
//                    mc.thePlayer.motionZ *= 1.125;
                    mc.thePlayer.motionY = 0.21;
                    break;
                case 3:
           //         mc.thePlayer.motionX *= 1.125;
        //            mc.thePlayer.motionZ *= 1.125;
                    mc.thePlayer.motionY = 0.25;
                    break;
                case 5:
         //           mc.thePlayer.motionX *= 1.125;
        //            mc.thePlayer.motionZ *= 1.125;
                    mc.thePlayer.motionY = 0.17;
                    karhujumps = 0;
                    break;
            }
        }
    }
}