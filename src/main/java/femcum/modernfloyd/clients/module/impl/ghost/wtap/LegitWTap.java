package femcum.modernfloyd.clients.module.impl.ghost.wtap;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.module.impl.ghost.WTap;
import femcum.modernfloyd.clients.value.Mode;

public class LegitWTap extends Mode<WTap> {

    private boolean unsprint, wTap;

    public LegitWTap(String name, WTap parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        wTap = Math.random() * 100 < getParent().chance.getValue().doubleValue() && event.getTarget().hurtTime >= 6;

        if (!wTap || unsprint) return;

        if (mc.thePlayer.isSprinting() || mc.gameSettings.keyBindSprint.isKeyDown()) {
            mc.gameSettings.keyBindSprint.setPressed(true);
            unsprint = true;
        }
    };

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (!wTap) return;

        if (unsprint && Math.random() * 100 < getParent().chance.getValue().doubleValue()) {
            mc.gameSettings.keyBindSprint.setPressed(false);
            unsprint = false;
        }
    };
}