package fr.ambient.module.impl.misc.disabler;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.world.WorldChangeEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.PacketTick;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

import java.util.ArrayList;

public class NegativityDisabler extends ModuleMode {

    private final ArrayList<PacketTick> pkts = new ArrayList<>();

    public NegativityDisabler(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof C0FPacketConfirmTransaction || event.getPacket() instanceof C00PacketKeepAlive) {
            event.setCancelled(true);
            pkts.add(new PacketTick(mc.thePlayer.ticksExisted, event.getPacket()));
        }
    }

    @SubscribeEvent
    private void onWorldChange(WorldChangeEvent event) {
        pkts.clear();
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        synchronized (pkts) {
            ArrayList<PacketTick> toRemove = new ArrayList<>();

            for (PacketTick t : pkts) {
                if (t.getTick() + 20 < mc.thePlayer.ticksExisted) {
                    PacketUtil.sendPacketNoEvent(t.getPacket());
                    toRemove.add(t);
                }
            }

            pkts.removeAll(toRemove);
        }
    }
}
