package femcum.modernfloyd.clients.module.impl.ghost;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.ClickEvent;
import femcum.modernfloyd.clients.event.impl.render.Render3DEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.ghost.autoclicker.DragClickAutoClicker;
import femcum.modernfloyd.clients.module.impl.ghost.autoclicker.NormalAutoClicker;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import rip.vantage.commons.util.time.StopWatch;

@ModuleInfo(aliases = {"module.ghost.autoclicker.name"}, description = "module.ghost.autoclicker.description", category = Category.GHOST)
public class AutoClicker extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new NormalAutoClicker("Normal", this))
            .add(new DragClickAutoClicker("Drag Click Simulations", this))
            .setDefault("Normal");

    private final BooleanValue jitter = new BooleanValue("Jitter", this, false);

    private final StopWatch stopWatch = new StopWatch();
    private double directionX, directionY;

    @EventLink
    public final Listener<ClickEvent> onClick = event -> {
        stopWatch.reset();

        directionX = (Math.random() - 0.5) * 4;
        directionY = (Math.random() - 0.5) * 4;
    };

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        mc.leftClickCounter = -1;

        if (!stopWatch.finished(100) && this.jitter.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            EntityRenderer.mouseAddedX = (float) (((Math.random() - 0.5) * 400 / Minecraft.getDebugFPS()) * directionX);
            EntityRenderer.mouseAddedY = (float) (((Math.random() - 0.5) * 400 / Minecraft.getDebugFPS()) * directionY);
        }
    };

}