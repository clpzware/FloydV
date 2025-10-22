package femcum.modernfloyd.clients.module.impl.player.nofall;

import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.NoFall;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;

public class MatrixNoFall extends Mode<NoFall> {

    public MatrixNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        float distance = FallDistanceComponent.distance;

        if (PlayerUtil.isBlockUnder()) {
            if (distance > 2) {
                MoveUtil.strafe(0.19);
            }

            if (distance > 3 && MoveUtil.speed() < 0.2) {
                event.setOnGround(true);
                distance = 0;
            }
        }

        FallDistanceComponent.distance = distance;
    };
}