package femcum.modernfloyd.clients.module.impl.movement.flight;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.TeleportEvent;
import femcum.modernfloyd.clients.module.impl.movement.Flight;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

public class AstralMCFlight extends Mode<Flight> {

    private final NumberValue height = new NumberValue("Height", this, 1, 0.1, 10, 0.1);
    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 10, 0.1);

    public AstralMCFlight(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        ChatUtil.display("Place a block to fly");
    }

    @EventLink
    private final Listener<TeleportEvent> teleport = event -> {
        mc.thePlayer.motionY = height.getValue().doubleValue();
        MoveUtil.strafe(speed.getValue().doubleValue());
        event.setCancelled();
        mc.thePlayer.setPosition(event.getPosX(), event.getPosY(), event.getPosZ());
        PacketUtil.send(new C03PacketPlayer.C06PacketPlayerPosLook(event.getPosX(), event.getPosY(), event.getPosZ(), event.getYaw(), event.getPitch(), false));
    };

    @EventLink
    private final Listener<PreMotionEvent> preMotion = event -> {
    };
}
