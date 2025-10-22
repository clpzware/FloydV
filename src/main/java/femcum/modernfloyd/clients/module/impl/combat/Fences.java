package femcum.modernfloyd.clients.module.impl.combat;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.SafeWalkEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;

@ModuleInfo(aliases = {"module.combat.fences.name"}, description = "module.combat.fences.description", category = Category.COMBAT)
public final class Fences extends Module {
    @Override
    public void onDisable() {
        mc.thePlayer.safeWalk = false;
    }

    @EventLink
    public final Listener<SafeWalkEvent> onSafeWalk = event -> {
        mc.thePlayer.safeWalk = true;
        event.setHeight(-5);
    };
}
