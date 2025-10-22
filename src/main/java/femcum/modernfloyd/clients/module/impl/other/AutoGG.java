package femcum.modernfloyd.clients.module.impl.other;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.value.impl.StringValue;

@ModuleInfo(aliases = {"module.other.autogg.name"}, description = "module.other.autogg.description", category = Category.PLAYER)
public final class AutoGG extends Module {

    private final StringValue message = new StringValue("Message", this, "Why waste another game without Rise?");
    private boolean active, worldChanged;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.ticksExisted % 18 != 0 || !mc.thePlayer.sendQueue.doneLoadingTerrain || mc.isIntegratedServerRunning()) {
            return;
        }

        if (mc.theWorld.playerEntities.stream().filter(entityPlayer -> !entityPlayer.isInvisible() || entityPlayer == mc.thePlayer).count() <= 1) {
            if (active) {
                mc.thePlayer.sendChatMessage(message.getValue());
                active = false;
            }
        } else if (worldChanged) {
            active = true;
            worldChanged = false;
        }
    };

    @EventLink
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        worldChanged = true;
    };

    @Override
    public void onEnable() {
        worldChanged = true;
    }
}