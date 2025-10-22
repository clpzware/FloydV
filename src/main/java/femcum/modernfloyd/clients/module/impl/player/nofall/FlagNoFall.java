package femcum.modernfloyd.clients.module.impl.player.nofall;

import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.TeleportEvent;
import femcum.modernfloyd.clients.module.impl.player.NoFall;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;

public class FlagNoFall extends Mode<NoFall> {

    public FlagNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        float distance = FallDistanceComponent.distance;

        if (distance > 3) {
            distance = -999;
            event.setPosY(event.getPosY() + Math.random());
        }

        FallDistanceComponent.distance = distance;
    };

    @EventLink
    public final Listener<TeleportEvent> onTeleport = event -> {
        FallDistanceComponent.distance = 0;
        event.setResponse(new C03PacketPlayer.C06PacketPlayerPosLook(event.getPosX(), event.getPosY(), event.getPosZ(), event.getYaw(), event.getPitch(), true));
    };
}