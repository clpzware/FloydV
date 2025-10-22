package femcum.modernfloyd.clients.component.impl.player;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;

public final class FallDistanceComponent extends Component {

    public static float distance;

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        final double fallDistance = mc.thePlayer.lastTickPosY - mc.thePlayer.posY;

        if (fallDistance > 0) {
            distance += fallDistance;
        }

        if (event.isOnGround()) {
            distance = 0;
        }
    };
}
