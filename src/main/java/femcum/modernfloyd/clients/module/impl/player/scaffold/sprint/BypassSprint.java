package femcum.modernfloyd.clients.module.impl.player.scaffold.sprint;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.value.Mode;

public class BypassSprint extends Mode<Scaffold> {

    public BypassSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.thePlayer.setSprinting(false);
    };
}
