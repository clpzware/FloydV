package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.GuiKeyBoardEvent;
import femcum.modernfloyd.clients.event.impl.render.Render2DEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import org.lwjgl.input.Keyboard;
import rip.vantage.commons.util.time.StopWatch;

import static femcum.modernfloyd.clients.layer.Layers.BLOOM;
import static femcum.modernfloyd.clients.layer.Layers.REGULAR;

/**
 * Displays a GUI which can display and do various things
 */
@ModuleInfo(aliases = {"module.render.clickgui.name"}, description = "module.render.clickgui.description", category = Category.RENDER, keyBind = Keyboard.KEY_RSHIFT)
public final class ClickGUI extends Module {
    private final StopWatch stopWatch = new StopWatch();

    @Override
    public void onEnable() {
//        Floyd.INSTANCE.getEventBus().register(Floyd.INSTANCE.getClickGUI());
        mc.displayGuiScreen(Floyd.INSTANCE.getClickGUI());
        stopWatch.reset();
    }

    @Override
    public void onDisable() {
        mc.setIngameFocus();
        Keyboard.enableRepeatEvents(false);
        Floyd.INSTANCE.getEventBus().unregister(Floyd.INSTANCE.getClickGUI());
        threadPool.execute(() -> Floyd.INSTANCE.getConfigManager()
                .getLatestConfig().write());
    }

    @EventLink(value = Priorities.HIGH)
    public final Listener<Render2DEvent> onRender2D = event -> {
        getLayer(REGULAR, 2).add(() -> Floyd.INSTANCE.getClickGUI().render());
        getLayer(BLOOM, 3).add(() -> Floyd.INSTANCE.getClickGUI().bloom());
    };

    @EventLink
    public final Listener<GuiKeyBoardEvent> onKey = event -> {
        if (!stopWatch.finished(50)) return;

        if (event.getKeyCode() == this.getKey()) {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
        }
    };
}
