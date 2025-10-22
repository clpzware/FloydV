package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
@ModuleInfo(aliases = {"module.movement.sprint.name"}, description = "module.movement.sprint.description", category = Category.MOVEMENT)
public class Sprint extends Module {
    private final BooleanValue legit = new BooleanValue("Legit", this, true);

    @EventLink(value = Priorities.LOW)
    public final Listener<StrafeEvent> onStrafe = event -> {
        mc.gameSettings.keyBindSprint.setPressed(true);

        if (!legit.getValue()) {
            mc.thePlayer.omniSprint = MoveUtil.isMoving();

            MoveUtil.preventDiagonalSpeed();

            mc.thePlayer.setSprinting(!legit.getValue() && MoveUtil.isMoving() && !mc.thePlayer.isCollidedHorizontally &&
                    !mc.thePlayer.isSneaking() && !mc.thePlayer.isUsingItem());
        }
    };

    @Override
    public void onDisable() {
        mc.thePlayer.setSprinting(mc.gameSettings.keyBindSprint.isKeyDown());
        mc.thePlayer.omniSprint = false;
    }
}