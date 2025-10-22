package femcum.modernfloyd.clients.module.impl.movement.sneak;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.Sneak;
import femcum.modernfloyd.clients.value.Mode;
import org.lwjgl.input.Keyboard;

public class HoldSneak extends Mode<Sneak> {

    public HoldSneak(String name, Sneak parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.gameSettings.keyBindSneak.setPressed(true);
    };

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindSneak.setPressed(Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
    }
}