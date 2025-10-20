package fr.ambient.module.impl.player;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;

public class NoRotate extends Module {
    public NoRotate() {
        super(81, "Makes your not rotate on lagback", ModuleCategory.PLAYER);
    }
    private float yaw, pitch;
    private int ticks = 0;

    public void setNextTickYawPitch(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onPlayerNetworkTick(PreMotionEvent event) {
            if (ticks < 1) {
                event.setYaw(yaw);
                event.setPitch(pitch);
                ticks++;
            }
        }
    }

