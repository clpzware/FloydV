package fr.ambient.module.impl.movement.noslow;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.NoSlowdown;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

import static fr.ambient.util.player.MoveUtil.getSwiftnessSpeed;


public class WatchdogNoslow extends ModuleMode {

    private final NoSlowdown noslow = (NoSlowdown) this.getSuperModule();

    public WatchdogNoslow(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onSendPacket(PacketSendEvent event) {
        if (noslow.wdoption.isSelected("Slowdown On Slab") && noslow.slab) {
            return;
        }

        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement c08 && !mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem() != null && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            if (c08.getPlacedBlockDirection() == 255 && noslow.isAllowed() && mc.thePlayer.airTicks < 2 && mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.41999998688698;
                noslow.doSend = true;
                event.setCancelled(true);
            }
        }
    }

    @SubscribeEvent
    private void onNetworkUpdate(PreMotionEvent event) {
        noslow.slab = Math.abs(event.getPosY() - Math.round(event.getPosY())) > 0.03 && mc.thePlayer.onGround;

        if (noslow.wdoption.isSelected("Slowdown On Slab") && noslow.slab) {
            return;
        }

        boolean isUsingItem = mc.thePlayer.isUsingItem();

        if (noslow.wdoption.isSelected("Faster On Ground") && isUsingItem && mc.thePlayer.onGround && noslow.isAllowed()) {
            MoveUtil.strafe(getSwiftnessSpeed(0.2f, 0.2));
        }

        if (mc.thePlayer.airTicks >= 2 && noslow.doSend && mc.thePlayer.getHeldItem() != null && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            noslow.doSend = false;
            PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.getHeldItem(), 0, 0, 0));
        } else if (isUsingItem && noslow.isAllowed()) {
            event.setPosY(event.getPosY() + 0.0001);
            event.setPosY(event.getPosY() + 1E-14);
        }
    }
}