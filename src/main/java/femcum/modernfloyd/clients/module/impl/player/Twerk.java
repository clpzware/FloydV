package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;

@ModuleInfo(aliases = {"module.player.twerk.name"}, description = "module.player.twerk.description", category = Category.PLAYER)
public class Twerk extends Module {

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.gameSettings.keyBindSneak.setPressed(Math.random() < 0.5);
    };

}