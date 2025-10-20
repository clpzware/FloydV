package fr.ambient.module.impl.combat.velocity;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.ChatComponentText;

public class MushMcCancelVelocity extends ModuleMode {


    private boolean didVelo = false;
    private S00PacketKeepAlive clientboundPingPacket;

    public MushMcCancelVelocity(String modeName, Module module) {
        super(modeName, module);
    }


    @Override
    public void onEnable() {
        clientboundPingPacket = null;
        didVelo = false;
    }

    @SubscribeEvent
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (clientboundPingPacket == null) {
            if (event.getPacket() instanceof S00PacketKeepAlive packet) {
                event.setCancelled(true);
                if (!didVelo) clientboundPingPacket = packet;
            }
            didVelo = false;
        } else {
            if (event.getPacket() instanceof S12PacketEntityVelocity packet) {
                if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    event.setCancelled(true);
                    didVelo = true;
                }
            } else {
                mc.thePlayer.sendQueue.addToSendQueue(new C00PacketKeepAlive(clientboundPingPacket.func_149134_c()));
            }
            if (event.getPacket() instanceof S00PacketKeepAlive packet) {
                event.setCancelled(true);
                if (!didVelo) clientboundPingPacket = packet;
            }
        }
    }
}