package femcum.modernfloyd.clients.component.impl.hypixel;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.ServerJoinEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.util.player.ServerUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class APIKeyComponent extends Component {

    private final Pattern pattern = Pattern.compile("Your new API key is (.*)");

    public static boolean receivedKey;
    public static String apiKey;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (!receivedKey && mc.thePlayer.ticksExisted == 2 && ServerUtil.isOnServer("hypixel")) {
//            mc.thePlayer.sendChatMessage("/api new");
        }
    };

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        final Packet<?> p = event.getPacket();

        if (p instanceof S02PacketChat) {
            if (!ServerUtil.isOnServer("hypixel")) {
                return;
            }

            final S02PacketChat wrapper = (S02PacketChat) p;
            final String message = wrapper.getChatComponent().getUnformattedText();
            final Matcher matcher = pattern.matcher(message);

            if (!wrapper.isChat() && matcher.find()) {
                apiKey = matcher.group(1);

                if (!receivedKey) {
                    event.setCancelled();
                }

                receivedKey = true;
            }
        }
    };

    @EventLink
    public final Listener<ServerJoinEvent> onServerJoin = event -> {
        APIKeyComponent.receivedKey = false;
        APIKeyComponent.apiKey = null;
    };
}
