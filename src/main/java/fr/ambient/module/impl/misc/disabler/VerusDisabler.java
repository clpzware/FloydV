package fr.ambient.module.impl.misc.disabler;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.PostMotionEvent;
import fr.ambient.event.impl.world.WorldChangeEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.misc.Disabler;
import fr.ambient.module.impl.movement.Flight;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;


//Some part of this code was given to me by wykt.

public class VerusDisabler extends ModuleMode {

    private final Disabler disabler = (Disabler) this.getSuperModule();
    private final Deque<Packet<?>> packets = new ConcurrentLinkedDeque<>();
    private boolean sentSprint, cancelNextC06, gotc02;

    public VerusDisabler(String modeName, Module module) {
        super(modeName, module);
    }

    private boolean shouldDisable() {
        return Ambient.getInstance().getModuleManager().getModule(Speed.class).isEnabled() ||
                Ambient.getInstance().getModuleManager().getModule(Flight.class).isEnabled();
    }

    @SubscribeEvent
    private void onWorldChange(WorldChangeEvent event) {
        if (shouldDisable()) return;
        packets.clear();
        cancelNextC06 = false;
    }

    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (shouldDisable()) return;
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 20) {
            packets.clear();
            cancelNextC06 = false;
            return;
        }

        if (event.getPacket() instanceof S32PacketConfirmTransaction) {
            packets.add(event.getPacket());
            event.setCancelled(true);

            int maxPool = disabler.verus.isSelected("Transaction Spam") ? 250 : 35;

            while (packets.size() > maxPool) {
                Packet<?> polledPacket = packets.pollFirst();
                mc.addScheduledTask(() -> ((Packet<INetHandlerPlayClient>) polledPacket).processPacket(mc.getNetHandler()));
            }
        }

        if (event.getPacket() instanceof S08PacketPlayerPosLook packet) {
            double x = mc.thePlayer.posX - packet.getX();
            double y = mc.thePlayer.posY - packet.getY();
            double z = mc.thePlayer.posZ - packet.getZ();
            double delta = Math.sqrt(x * x + y * y + z * z);

            if (delta < 8 && disabler.verus.isSelected("Teleport Spam")) {
                PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.getX(), packet.getY(), packet.getZ(), false));
                event.setCancelled(true);
                return;
            } else {
                PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.getX(), packet.getY(), packet.getZ(), false));
                mc.thePlayer.setPosition(packet.getX(), packet.getY(), packet.getZ());
            }

            cancelNextC06 = true;
        }
    }


    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        if (shouldDisable()) return;
        if (event.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook && cancelNextC06) {
            event.setCancelled(true);
            cancelNextC06 = false;
        }

        if (disabler.verus.isSelected("Sprint Spam")) {
            if (event.getPacket() instanceof C0BPacketEntityAction) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getPacket() instanceof C03PacketPlayer packet) {
            if (mc.thePlayer.ticksExisted % 50 == 0 && disabler.verus.isSelected("Teleport Spam") && !gotc02) {
                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(mc.thePlayer).down(), EnumFacing.UP.getIndex(), new ItemStack(Blocks.stone, 1), 0.5F, 0.5F, 0.5F));
                PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.getPositionX(), packet.getPositionY() - 15, packet.getPositionZ(), packet.isOnGround()));
            }
        }

        if (event.getPacket() instanceof C02PacketUseEntity) {
            gotc02 = true;
        }

        if (disabler.verus.isSelected("Sprint Spam") && !sentSprint) {
            if (mc.thePlayer.ticksExisted % 5 == 0) {
                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }

            if (mc.thePlayer.ticksExisted % 6 == 0) {
                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
            }
        }
        sentSprint = false;
    }


    @SubscribeEvent
    private void onUpdate(PostMotionEvent event) {
        if (shouldDisable()) return;
        gotc02 = false;
    }
}
