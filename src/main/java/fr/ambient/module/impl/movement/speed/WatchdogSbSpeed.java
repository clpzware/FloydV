package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WatchdogSbSpeed extends ModuleMode {

    public WatchdogSbSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerTick(MovementEvent event) {
        if(mc.thePlayer.airTicks > 4 && mc.thePlayer.fallDistance > 0){
            double speed = mc.thePlayer.capabilities.getWalkSpeed() * 2;
            double rad = mc.thePlayer.rotationYaw * Math.PI / 180;
            double x = -Math.sin(rad) * speed;
            double z = Math.cos(rad) * speed;
            mc.thePlayer.setVelocity(0, mc.thePlayer.motionY, 0);

            ScheduledExecutorService scheduler0 = Executors.newScheduledThreadPool(1);
            Runnable task2 = () -> {
                mc.thePlayer.setVelocity(x, mc.thePlayer.motionY, z);
            };

            scheduler0.schedule(task2, 0, TimeUnit.MILLISECONDS);
        }

    }
}
