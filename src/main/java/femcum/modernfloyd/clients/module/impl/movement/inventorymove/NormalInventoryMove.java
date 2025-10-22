package femcum.modernfloyd.clients.module.impl.movement.inventorymove;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.module.impl.movement.InventoryMove;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public final class NormalInventoryMove extends Mode<InventoryMove> {
    public NormalInventoryMove(String name, InventoryMove parent) {
        super(name, parent);
    }

    private final KeyBinding[] AFFECTED_BINDINGS = new KeyBinding[]{
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindJump
    };

    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        if (mc.currentScreen == null || mc.currentScreen instanceof GuiChat || mc.currentScreen == this.getClickGUI())
            return;

        for (final KeyBinding bind : AFFECTED_BINDINGS) {
            bind.setPressed(GameSettings.isKeyDown(bind));
        }
    };
}
