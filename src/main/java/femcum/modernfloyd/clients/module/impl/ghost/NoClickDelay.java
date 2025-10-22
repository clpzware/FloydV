package femcum.modernfloyd.clients.module.impl.ghost;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;

@ModuleInfo(aliases = {"module.ghost.noclickdelay.name"}, description = "module.ghost.noclickdelay.description", category = Category.GHOST)
public class NoClickDelay extends Module {

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.leftClickCounter = 0;
        }
    };
}