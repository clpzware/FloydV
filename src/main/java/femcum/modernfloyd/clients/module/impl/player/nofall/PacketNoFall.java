package femcum.modernfloyd.clients.module.impl.player.nofall;

import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.NoFall;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;

public class PacketNoFall extends Mode<NoFall> {

    public PacketNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        float distance = FallDistanceComponent.distance;

        if (distance > 3) {
            PacketUtil.send(new C03PacketPlayer(true));
            distance = 0;
        }

        FallDistanceComponent.distance = distance;
    };
}