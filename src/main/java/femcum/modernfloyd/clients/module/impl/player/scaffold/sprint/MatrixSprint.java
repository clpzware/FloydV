package femcum.modernfloyd.clients.module.impl.player.scaffold.sprint;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.MoveInputEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class MatrixSprint extends Mode<Scaffold> {
    private int time;
    private boolean ignore;

    public MatrixSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        time++;

        mc.gameSettings.keyBindSneak.setPressed(time >= 4);
    };

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> p = event.getPacket();

        if (p instanceof C08PacketPlayerBlockPlacement) {
            final C08PacketPlayerBlockPlacement wrapper = (C08PacketPlayerBlockPlacement) p;

            if (wrapper.getPlacedBlockDirection() != 255) {
                time = 0;
            }
        }
    };

    @EventLink(value = Priorities.MEDIUM)
    public final Listener<MoveInputEvent> onMove = event -> {
        event.setSneakSlowDownMultiplier(0.5);
        mc.gameSettings.keyBindSprint.setPressed(false);
        mc.thePlayer.setSprinting(false);
    };
}
