package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.render.ViewBobbingEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import femcum.modernfloyd.clients.value.impl.SubMode;

@ModuleInfo(aliases = {"module.render.viewbobbing.name"}, description = "module.render.viewbobbing.description", category = Category.RENDER)
public final class ViewBobbing extends Module {

    public final ModeValue viewBobbingMode = new ModeValue("Mode", this)
            .add(new SubMode("Smooth"))
            .add(new SubMode("Meme"))
            .add(new SubMode("None"))
            .setDefault("None");

    @EventLink
    public final Listener<ViewBobbingEvent> onViewBobbing = event -> {
        if (viewBobbingMode.getValue().getName().equals("Smooth") && (event.getTime() == 0 || event.getTime() == 2)) {
            event.setCancelled();
        }
    };

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.gameSettings.viewBobbing = true;

        switch (viewBobbingMode.getValue().getName()) {
            case "Meme": {
                mc.thePlayer.cameraYaw = 0.5F;
                break;
            }

            case "None": {
                mc.thePlayer.distanceWalkedModified = 0.0F;
                break;
            }
        }
    };
}