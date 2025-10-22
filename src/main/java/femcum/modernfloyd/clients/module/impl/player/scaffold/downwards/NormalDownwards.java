package femcum.modernfloyd.clients.module.impl.player.scaffold.downwards;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.value.Mode;
import org.lwjgl.input.Keyboard;

public class NormalDownwards extends Mode<Scaffold> {

    public NormalDownwards(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink(value = Priorities.HIGH)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (!Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) return;

        getParent().offset = getParent().offset.add(0,-1,0);
    };
}
