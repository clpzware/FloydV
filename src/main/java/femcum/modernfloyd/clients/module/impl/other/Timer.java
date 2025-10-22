package femcum.modernfloyd.clients.module.impl.other;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.math.MathUtil;
import femcum.modernfloyd.clients.value.impl.BoundsNumberValue;

@ModuleInfo(aliases = {"module.other.timer.name"}, description = "module.other.timer.description", category = Category.MOVEMENT)
public final class Timer extends Module {

    private final BoundsNumberValue timer =
            new BoundsNumberValue("Timer", this, 1, 2, 0.1, 10, 0.05);

    @EventLink(value = Priorities.MEDIUM)
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.timer.timerSpeed = (float) MathUtil.getRandom(timer.getValue().floatValue(), timer.getSecondValue().floatValue());
    };

    @Override
    public void onDisable() {
        if (this.mc.timer.timerSpeed != 1) {
            this.mc.timer.timerSpeed = 1;
        }
    }

}
