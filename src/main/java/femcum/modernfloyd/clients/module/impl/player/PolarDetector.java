package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.component.impl.render.NotificationComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

@ModuleInfo(aliases = {"module.player.polardetector.name"}, description = "module.player.polardetector.description", category = Category.PLAYER)
public class PolarDetector extends Module {
    boolean transaction = false;

    @Override
    public void onEnable() {
        NotificationComponent.post("Polar Detector","Join a game and this module will notify you of polars status");
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.ticksExisted == 30) {
            ChatUtil.display(transaction ? "Polar is enabled" : "Polar is disabled");
        }
    };

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
            transaction = true;
        }
    };

    @EventLink
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        transaction = false;
    };
}