package femcum.modernfloyd.clients.module.impl.combat.criticals;

import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.Criticals;
import femcum.modernfloyd.clients.value.Mode;

public final class VulcanCriticals extends Mode<Criticals> {

    public VulcanCriticals(String name, Criticals parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.ticksSinceVelocity <= 18 && FallDistanceComponent.distance < 1.8) {
            event.setOnGround(false);
        }
    };
}