package femcum.modernfloyd.clients.module.impl.movement.phase;

import femcum.modernfloyd.clients.component.impl.player.PingSpoofComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.other.BlockAABBEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.module.impl.movement.Phase;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.block.BlockGlass;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import rip.vantage.commons.util.time.StopWatch;

public class WatchdogAutoPhase extends Mode<Phase> {
    private boolean phase;
    private final StopWatch stopWatch = new StopWatch();

    public WatchdogAutoPhase(String name, Phase parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (phase && !stopWatch.finished(4000)) PingSpoofComponent.blink();
    };

    @EventLink
    public final Listener<BlockAABBEvent> onBlockAABBEvent = event -> {
        if (phase && PingSpoofComponent.enabled && event.getBlock() instanceof BlockGlass) event.setCancelled();
    };

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            S02PacketChat s02PacketChat = ((S02PacketChat) packet);
            String chat = s02PacketChat.getChatComponent().getUnformattedText();

            switch (chat) {
                case "Cages opened! FIGHT!":
                case "§r§r§r                               §r§f§lSkyWars Duel§r":
                case "§r§eCages opened! §r§cFIGHT!§r":
                    phase = false;
                    break;

                case "The game starts in 3 seconds!":
                case "§r§e§r§eThe game starts in §r§a§r§c3§r§e seconds!§r§e§r":
                case "§r§eCages open in: §r§c3 §r§eseconds!§r":
                    phase = true;
                    stopWatch.reset();
                    break;
            }
        }
    };
}