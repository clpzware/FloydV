package femcum.modernfloyd.clients.module.impl.movement.inventorymove;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.impl.movement.InventoryMove;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;

public class WatchdogInventoryMove extends Mode<InventoryMove> {
    public WatchdogInventoryMove(String name, InventoryMove parent) {
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
    private final Listener<PreMotionEvent> preUpdateEventListener = event -> {
        if (mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiChest || mc.currentScreen == this.getClickGUI() || mc.currentScreen instanceof GuiInventory) {
            MoveUtil.stop();
            
        }
    };

    @EventLink
    private final Listener<PacketSendEvent> packetSendEventListener = event -> {
    };
}
