package fr.ambient.module.impl.combat.velocity;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class OldGrimVelocity extends ModuleMode {

    private boolean grimRealKB, balz, palle, isRealVelo = false; // year old shitcode

    public OldGrimVelocity(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent()
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity s12PacketEntityVelocity) {
            if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCancelled(true);
                palle = true;
                if (balz) {
                    balz = false;
                    return;
                }
                if (!isRealVelo) {
                    event.setCancelled(false);
                }
                grimRealKB = true;
                balz = false;
                palle = false;
            }
        }

        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            balz = true;
        }

        if (event.getPacket() instanceof S19PacketEntityStatus packet) {
            if (packet.getEntity(mc.theWorld) == mc.thePlayer && packet.getOpCode() == 2) {
                isRealVelo = true;
            }
        }
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
        PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, new BlockPos(mc.thePlayer), EnumFacing.DOWN));
        grimRealKB = false;
    }
}
