package femcum.modernfloyd.clients.module.impl.player.scaffold.tower;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

public class MMCTower extends Mode<Scaffold> {

    public MMCTower(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (mc.gameSettings.keyBindJump.isKeyDown() && packet instanceof C08PacketPlayerBlockPlacement) {
            final C08PacketPlayerBlockPlacement c08PacketPlayerBlockPlacement = ((C08PacketPlayerBlockPlacement) packet);

            if (c08PacketPlayerBlockPlacement.getPosition().equals(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.4, mc.thePlayer.posZ))) {
                mc.gameSettings.keyBindSprint.setPressed(false);
                mc.thePlayer.setSprinting(false);
                mc.thePlayer.motionY = 0.42F;
            }
        }
    };
}
