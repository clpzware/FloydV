package femcum.modernfloyd.clients.module.impl.player.nofall;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.NoFall;
import femcum.modernfloyd.clients.value.Mode;

public class NoGroundNoFall extends Mode<NoFall> {

    public NoGroundNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        event.setOnGround(false);
        event.setPosY(event.getPosY() + Math.random() / 100000000000000000000f);
    };
}