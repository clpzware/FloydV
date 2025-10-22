package femcum.modernfloyd.clients.module.impl.ghost.wtap;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.module.impl.ghost.WTap;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.entity.EntityLivingBase;

public final class SilentWTap extends Mode<WTap> {
    private EntityLivingBase target;

    public SilentWTap(String name, WTap parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (target != null && target.hurtTime == 9) {
            event.setSprinting(false);
        }
    };

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        target = event.getTarget();
    };
}
