package femcum.modernfloyd.clients.module.impl.movement.sneak;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.Sneak;
import femcum.modernfloyd.clients.value.Mode;

public class StandardSneak extends Mode<Sneak> {

    public StandardSneak(String name, Sneak parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.thePlayer.movementInput.sneak = mc.thePlayer.sendQueue.doneLoadingTerrain;
    };
}