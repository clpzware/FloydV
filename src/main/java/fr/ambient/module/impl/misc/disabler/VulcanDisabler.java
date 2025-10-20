package fr.ambient.module.impl.misc.disabler;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.misc.Disabler;
import fr.ambient.util.TimePacket;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.concurrent.ConcurrentLinkedQueue;

public class VulcanDisabler extends ModuleMode {

    private ConcurrentLinkedQueue<TimePacket> packets = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<TimePacket> packets2 = new ConcurrentLinkedQueue<>();
    private int attackAmount = 0;

    public VulcanDisabler(String modeName, Module module) {
        super(modeName, module);
    }
    private long lastMS = 0;
    private long lastHurtTime = 0;

    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        var setting = (((Disabler)this.getSuperModule()).vulcan);
        if (setting.isSelected("Modulo")) {
            if (event.getPacket() instanceof C03PacketPlayer pos) {
                pos.x += Math.random() / 10000;
                pos.z += Math.random() / 10000;
            }
        }
        if (setting.isSelected("Velocity")) {
            if (event.getPacket() instanceof C0FPacketConfirmTransaction || event.getPacket() instanceof C00PacketKeepAlive) {
                packets.offer(new TimePacket(System.currentTimeMillis(), event.getPacket()));
                event.setCancelled(true);
            }
            if (packets.stream().anyMatch(p -> (System.currentTimeMillis() - p.getTime() > 5000) || packets.size() > 50)) {
                packets.forEach(packet -> {
                    PacketUtil.sendPacketNoEvent(packet.getPacket());
                    packets.remove(packet);
                });
            }
        }

        if (setting.isSelected("AutoClicker")) {
            if (event.getPacket() instanceof C02PacketUseEntity c02PacketUseEntity && c02PacketUseEntity.getAction() == C02PacketUseEntity.Action.ATTACK) {
                attackAmount++;
            }
            if (attackAmount > 15) {
                PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockPos.ORIGIN, EnumFacing.DOWN));
                attackAmount = 0;
            }
        }

        if (setting.isSelected("Sprint")) {
            if (event.getPacket() instanceof C0BPacketEntityAction) {
                event.setCancelled(true);
            }
        }
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        var setting = (((Disabler)this.getSuperModule()).vulcan);
        if (setting.isSelected("Scaffold")) {

            if(mc.thePlayer.ticksExisted % 10 == 0){
                PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
            }
            if (mc.thePlayer.ticksExisted % 10 == 2) {
                PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }

        }
        if (setting.isSelected("Sprint")) {
            if (MoveUtil.moving()) {
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                } else {
                    mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                }
            } else {
                mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
            }
        }
    }

    @Override
    public void onDisable() {
        if (packets.isEmpty() || mc.theWorld == null || mc.thePlayer == null) return;

        packets.forEach(packet -> {
            PacketUtil.sendPacketNoEvent(packet.getPacket());
            packets.remove(packet);
        });
    }
}
