package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class CustomSpeed extends ModuleMode {
    public CustomSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1f;
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        Speed speedModule = (Speed) this.getSuperModule();

        if (speedModule.teleport.getValue()) {
            if (mc.thePlayer.ticksExisted % 10 != 0) return;
            for (int i = 0; i <= speedModule.teleporttick.getValue(); i++) {
                var x = (-Math.sin(Math.toRadians(mc.thePlayer.rotationYaw)) * speedModule.tpdistance.getValue()) * i;
                var z = (Math.cos(Math.toRadians(mc.thePlayer.rotationYaw)) * speedModule.tpdistance.getValue()) * i;
                mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
            }
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.prevPosX, mc.thePlayer.prevPosY, mc.thePlayer.prevPosZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
        }

        if (speedModule.jump.getValue() && mc.thePlayer.onGround && MoveUtil.moving()) {
            mc.thePlayer.jump();
        }

        switch (speedModule.spoof.getValue()) {
            case "Off Ground" -> event.setOnGround(false);
            case "On Ground" -> event.setOnGround(true);
        }

        if (MoveUtil.moving()) {
            switch (speedModule.whentoapply.getValue()) {
                case "On Ground" -> {
                    if (mc.thePlayer.onGround) {
                        if (!speedModule.jump.getValue()) {
                            mc.thePlayer.motionY = speedModule.cmotiony.getValue();
                        }
                        MoveUtil.strafe(speedModule.cspeed.getValue());
                    }
                }
                case "On Fall" -> {
                    if (!mc.thePlayer.onGround && mc.thePlayer.motionY < 0) {
                        MoveUtil.strafe(speedModule.cspeed.getValue());
                    }
                }
                case "On Jump" -> {
                    if (mc.thePlayer.motionY > 0) {
                        MoveUtil.strafe(speedModule.cspeed.getValue());
                    }
                }
                case "Always" -> {
                    if (mc.thePlayer.onGround) {
                        if (!speedModule.jump.getValue()) {
                            mc.thePlayer.motionY = speedModule.cmotiony.getValue();
                        }
                    }
                    MoveUtil.strafe(speedModule.cspeed.getValue());
                }
            }
        }

        if (speedModule.applytimer.getValue()) {
            mc.timer.timerSpeed = speedModule.timerspeed.getValue();
        }

        if (speedModule.cfallmotion.getValue()) {
            if (mc.thePlayer.airTicks == speedModule.airtick.getValue()) {
                mc.thePlayer.motionY -= speedModule.fallmotion.getValue();
            }
        }
    }


    @SubscribeEvent
    private void onPlayerNetworkTickEvent(PreMotionEvent event) {
        Speed speedModule = (Speed) this.getSuperModule();
        if (speedModule.offset.getValue()) {
            event.setPosY(event.getPosY() + 1E-13f);
        }
    }
}
