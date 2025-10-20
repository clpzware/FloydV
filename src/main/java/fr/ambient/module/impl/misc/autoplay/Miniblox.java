package fr.ambient.module.impl.misc.autoplay;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.misc.AutoPlay;
import fr.ambient.util.math.TimeUtil;
import net.minecraft.network.play.server.S02PacketChat;


public class Miniblox extends ModuleMode {

    private final TimeUtil timeUtil = new TimeUtil();
    private final TimeUtil timeUtilB = new TimeUtil();
    private String toSendAutoPlay = "";

    private static final String WIN = "Queueing next game in 15 seconds (or press N to queue immediately)...";

    public Miniblox(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onMotion(UpdateEvent event) {
        if (mc.thePlayer.ticksExisted > 200) {
            AutoPlay autoPlayModule = (AutoPlay) this.getSuperModule();

            if (!toSendAutoPlay.isEmpty() && timeUtilB.finished(autoPlayModule.delayAP.getValue().longValue())) {
                mc.thePlayer.sendChatMessage(toSendAutoPlay);
                toSendAutoPlay = "";
                timeUtilB.reset();
            }
        }
    }

    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (!event.isCancelled() && event.getPacket() instanceof S02PacketChat s02PacketChat) {
            String chatMessage = s02PacketChat.getChatComponent().getUnformattedText();
            AutoPlay autoPlayModule = (AutoPlay) this.getSuperModule();
            if (autoPlayModule.autoplay.getValue()) {
                if (chatMessage.contains(WIN)) {
                    String command = "/play ";
                    switch (autoPlayModule.miniblox.getValue()) {
                        case "Skywars" -> command += "skywars";
                        case "Eggwars" -> command += "eggwars";
                    }
                    toSendAutoPlay = command;
                    timeUtilB.reset();
                }
            }
        }
    }
}