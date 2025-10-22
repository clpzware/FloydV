package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.impl.NumberValue;

@ModuleInfo(aliases = {"module.movement.strafe.name"}, description = "module.movement.strafe.description", category = Category.MOVEMENT)
public class Strafe extends Module {
    private NumberValue strength = new NumberValue("Strength", this, 100, 1, 100, 1);
    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
    }

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        MoveUtil.partialStrafePercent(strength.getValue().floatValue());
    };
}