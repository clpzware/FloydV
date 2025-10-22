package femcum.modernfloyd.clients.module.impl.player.scaffold.sprint;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.value.Mode;

public class DisabledSprint extends Mode<Scaffold> {

    public DisabledSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<StrafeEvent> onPreMotionEvent = event -> {
        mc.gameSettings.keyBindSprint.setPressed(false);
        mc.thePlayer.setSprinting(false);
    };
}
