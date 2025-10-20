package fr.ambient.module.impl.misc.autoplay;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.misc.AutoPlay;
import fr.ambient.util.math.TimeUtil;
import net.minecraft.network.play.server.S02PacketChat;


public class Hypixel extends ModuleMode {

    private final TimeUtil timeUtil = new TimeUtil();
    private final TimeUtil timeUtilB = new TimeUtil();
    private String toSendAutoPlay = "";
    private String toSendAutoGG = "";

    private static final String WIN = "You won! Want to play again? Click here!";
    private static final String LOSE = "You died! Want to play again? Click here!";
    private static final String BW = "1st Killer";
    private static final String JOIN = "has joined";
    private static final String DUEL = "Accuracy";

    public Hypixel(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onEnable() {
        mc.thePlayer.sendChatMessage("/lang english");
    }

    @SubscribeEvent
    private void onMotion(UpdateEvent event) {
        if (mc.thePlayer.ticksExisted > 200) {
            AutoPlay autoPlayModule = (AutoPlay) this.getSuperModule();

            if (!toSendAutoGG.isEmpty() && timeUtil.finished(autoPlayModule.delayAGG.getValue().longValue())) {
                mc.thePlayer.sendChatMessage(toSendAutoGG);
                toSendAutoGG = "";
                timeUtil.reset();
            }

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
            String username = mc.thePlayer.getName();
            AutoPlay autoPlayModule = (AutoPlay) this.getSuperModule();

            if (autoPlayModule.autoplay.getValue()) {
                if (chatMessage.contains(WIN) || chatMessage.contains(LOSE) || chatMessage.contains(BW) || chatMessage.contains(DUEL)) {
                    String command = "/play ";
                    switch (autoPlayModule.hypixel.getValue()) {
                        case "Solo Insane" -> command += "solo_insane";
                        case "Solo Normal" -> command += "solo_normal";
                        case "BedWars Solo" -> command += "bedwars_eight_one";
                        case "BedWars Duo" -> command += "bedwars_eight_two";
                        case "BedWars Trio" -> command += "bedwars_four_three";
                        case "BedWars 4s" -> command += "bedwars_four_four";
                        case "Classic Duel" -> command += "duels_classic_duel";
                    }
                    toSendAutoPlay = command;
                    timeUtilB.reset();
                }
            }

            if (autoPlayModule.autogg.getValue()) {
                if (chatMessage.toLowerCase().contains("1st killer")) {
                    String txt = switch (autoPlayModule.gg.getValue()) {
                        case "GG" -> "GG";
                        case "How to play BW" -> "https://hypixel.net/threads/how-to-play-bedwars.3066561/";
                        case "gg ez" -> "gg éz";
                        default -> "gg";
                    };
                    toSendAutoGG = "/ac " + txt;
                    timeUtilB.reset();
                }
            }

            if (autoPlayModule.autowho.getValue() && chatMessage.contains(username + " " + JOIN)) {
                String command = "/who";
                mc.thePlayer.sendChatMessage(command);
            }
        }
    }
}
