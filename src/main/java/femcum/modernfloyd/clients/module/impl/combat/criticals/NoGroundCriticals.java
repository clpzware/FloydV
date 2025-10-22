package femcum.modernfloyd.clients.module.impl.combat.criticals;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.Criticals;
import femcum.modernfloyd.clients.value.Mode;

public final class NoGroundCriticals extends Mode<Criticals> {

    public NoGroundCriticals(String name, Criticals parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        event.setOnGround(false);
    };
}
