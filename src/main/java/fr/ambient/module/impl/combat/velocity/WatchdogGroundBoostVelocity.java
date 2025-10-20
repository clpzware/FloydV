package fr.ambient.module.impl.combat.velocity;

import fr.ambient.Ambient;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Velocity;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.module.impl.player.Breaker;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;

public class WatchdogGroundBoostVelocity extends ModuleMode {

    private final ArrayList<Packet> delayed = new ArrayList<>();
    private final Velocity velocity = (Velocity) this.getSuperModule();
    private int stage = 0;
    public int ticks = 0;


    public WatchdogGroundBoostVelocity(String modeName, Module module) {
        super(modeName, module);
    }


    public void onDisable() {
        if (!delayed.isEmpty()) {
            delayed.forEach(p -> PacketUtil.sendPacketNoEvent(p));
            delayed.clear();
        }
    }

    @SubscribeEvent
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        boolean speedenabled = Ambient.getInstance().getModuleManager().getModule(Speed.class).isEnabled();
        if (event.getPacket() instanceof S12PacketEntityVelocity velocityPacket) {
            event.setCancelled(true);
            if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                if (velocity.breaker.getValue() && Ambient.getInstance().getModuleManager().getModule(Breaker.class).breakPos != null) {
                    return;
                }
                if (velocity.getDistanceToGround() < 3 && !mc.thePlayer.onGround && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava()) {
                    stage = 1;
                } else {
                    if (velocity.boosts.getValue() && Conditions()) {
                        if (MoveUtil.moving() && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed) {
                            mc.thePlayer.motionX = velocityPacket.getMotionX() / 8000D;
                            mc.thePlayer.motionZ = velocityPacket.getMotionZ() / 8000D;
                        } else {
                            mc.thePlayer.motionY = velocityPacket.getMotionY() / 8000D;
                        }
                    } else {
                        if (speedenabled) {
                            mc.thePlayer.motionX = -(velocityPacket.getMotionX() / 8000D);
                            mc.thePlayer.motionZ = -(velocityPacket.getMotionZ() / 8000D);
                        } else {
                            mc.thePlayer.motionY = velocityPacket.getMotionY() / 8000D;
                        }
                    }
                }
            }
        }


        if (event.getPacket() instanceof S27PacketExplosion && velocity.noExplosion.getValue()) {
            event.setCancelled(true);
        }

        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            this.stage = 2;
        }
    }

    @SubscribeEvent(EventPriority.LOW)
    private void onSendPacket(PacketSendEvent event) {
        if ((event.getPacket() instanceof C0FPacketConfirmTransaction || event.getPacket() instanceof C00PacketKeepAlive)) {
            if (stage >= 1 && !event.isCancelled()) {
                event.setCancelled(true);
                delayed.add(event.getPacket());
                if (stage == 2) {
                    stage = 0;
                    delayed.forEach(PacketUtil::sendPacketNoEvent);
                    delayed.clear();
                }
            }
        }
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if ((mc.thePlayer.airTicks <= 2 && !mc.thePlayer.onGround) || (mc.thePlayer.groundTicks > 3)) {
            if (stage == 1) {
                stage = 2;
            }
        }
    }

    public boolean Conditions() {
        return !mc.thePlayer.isCollidedHorizontally || !mc.thePlayer.isCollidedVertically && !mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.onGround
                && !Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() && mc.thePlayer.fallDistance <= 1 && !PlayerUtil.isNearSlabAndStairs() &&
                !PlayerUtil.isBlockUnder(1) || (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down(1)).getBlock() instanceof BlockAir);
    }
}
