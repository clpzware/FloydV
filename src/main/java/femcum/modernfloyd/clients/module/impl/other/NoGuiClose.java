package femcum.modernfloyd.clients.module.impl.other;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2EPacketCloseWindow;

@ModuleInfo(aliases = {"module.other.noguiclose.name"}, category = Category.PLAYER, description = "module.other.noguiclose.description")
public final class NoGuiClose extends Module {
    private final BooleanValue chatonly = new BooleanValue("Chat Only", this, false);

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();
        if (event.getPacket() instanceof S2EPacketCloseWindow && (mc.currentScreen instanceof GuiChat || !chatonly.getValue())) {
            event.setCancelled();
        }
    };
}

