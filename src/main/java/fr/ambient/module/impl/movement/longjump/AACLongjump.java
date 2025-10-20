package fr.ambient.module.impl.movement.longjump;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import net.minecraft.network.play.client.C03PacketPlayer;

public class AACLongjump extends ModuleMode {

    private int jumpState;
    private boolean collideReset;
    private int speedReductionCounter;
    private boolean hasReset;


    public AACLongjump(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer == null) return;

        double playerYaw = Math.toRadians(mc.thePlayer.rotationYaw);

        if (mc.thePlayer.isRiding()) {
            jumpState = 1;
        } else {
            if (jumpState == 1) {
                jumpState = 0;
                collideReset = true;
                mc.thePlayer.motionX = 5.0 * -Math.sin(playerYaw);
                mc.thePlayer.motionZ = 5.0 * Math.cos(playerYaw);
                mc.thePlayer.motionY = 0.42;
            } else if (mc.thePlayer.onGround && jumpState == 0) {
                collideReset = false;
            }
        }

        if (jumpState == 1 && mc.thePlayer.onGround) {
            mc.thePlayer.motionX = (6.0 - 0.2 * speedReductionCounter) * -Math.sin(playerYaw);
            mc.thePlayer.motionZ = (6.0 - 0.2 * speedReductionCounter) * Math.cos(playerYaw);

            if (mc.thePlayer.isCollidedHorizontally || (6.0 - 0.2 * speedReductionCounter) < 0.4) {
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 51, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                jumpState = 0;
                speedReductionCounter = 0;
            } else {
                speedReductionCounter++;
            }
        }

        if (mc.thePlayer.isCollidedHorizontally && collideReset) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            collideReset = false;

            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 50, mc.thePlayer.posY, mc.thePlayer.posZ, false));
            hasReset = true;
        }
    }
}