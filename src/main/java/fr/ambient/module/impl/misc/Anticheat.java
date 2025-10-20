package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.MultiProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S02PacketChat;

public class Anticheat extends Module {

    private boolean banmsg = false;
    private final Minecraft mc = Minecraft.getMinecraft();
    public MultiProperty checks = MultiProperty.newInstance("Checks", new String[]{"Autoblock", "Noslow", "Scaffold"});
    public MultiProperty notif = MultiProperty.newInstance("Notifications Settings", new String[]{"Chat Message"});
    public final BooleanProperty team = BooleanProperty.newInstance("Whitelist Team", true);
    public final BooleanProperty autowdr = BooleanProperty.newInstance("Auto wdr cheater", true);
    private final BooleanProperty bandetector = BooleanProperty.newInstance("Ban Detector", false);
    private final BooleanProperty ban = BooleanProperty.newInstance("Auto Disconnect", false, bandetector::getValue);
    public Anticheat() {
        super(19, "A client-side anti cheat to detect other cheaters.", ModuleCategory.MISC);
        this.registerProperties(checks,notif,team,bandetector,ban,autowdr);
    }

    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S02PacketChat s02PacketChat) {
            String chatMessage = s02PacketChat.getChatComponent().getUnformattedText().toLowerCase().trim();
            banmsg = chatMessage.contains("a player has been removed from your game.");

            if (banmsg && ban.getValue()) {
                mc.thePlayer.sendChatMessage("/hub");
            }
        }
    }
}
