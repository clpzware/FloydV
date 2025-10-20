package fr.ambient.component.impl.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fr.ambient.Ambient;
import fr.ambient.component.Component;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.impl.movement.speed.WatchdogSpeed;
import fr.ambient.util.packet.RequestUtil;
import lombok.SneakyThrows;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.EnumChatFormatting;
import org.lwjglx.Sys;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import static net.minecraft.client.gui.GuiPlayerTabOverlay.field_175252_a;

public class HypixelComponent extends Component {



    public static final ArrayList<UUID> known_cheaters_or_alts = new ArrayList<>();
    public static final ArrayList<UUID> beamed_accounts = new ArrayList<>();
    public static String HYPIXEL_APIKEY = "";
    public static HashMap<UUID, String> namecache = new HashMap<>();


    public static HashMap<UUID, Integer> joinTimes = new HashMap<>();


    @SneakyThrows
    public static void loadCheaters(){
        beamed_accounts.clear();
        known_cheaters_or_alts.clear();
        joinTimes.clear();
    }


    @SubscribeEvent
    private void onReceivePacket(PacketReceiveEvent event){
        if(event.getPacket() instanceof S02PacketChat s02PacketChat){
            String unformattedStr = EnumChatFormatting.getTextWithoutFormattingCodes(s02PacketChat.getChatComponent().getUnformattedText());

            if(unformattedStr.contains("has joined (")){
                String username = "";

                if(unformattedStr.contains("[")){
                    username = unformattedStr.split(" ")[1];
                }else{
                    username = unformattedStr.split(" ")[0];
                }

                NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
                List<NetworkPlayerInfo> list = field_175252_a.sortedCopy(nethandlerplayclient.getPlayerInfoMap());
                for (NetworkPlayerInfo networkplayerinfo : list) {
                    UUID uuid = networkplayerinfo.getGameProfile().getId();
                    String uname = networkplayerinfo.getGameProfile().getName();
                    if(uname.equals(username)){
                        if(!joinTimes.containsKey(uuid)){
                            joinTimes.put(uuid, 0);
                        }else{
                            joinTimes.put(uuid, joinTimes.get(uuid) + 1);
                        }
                    }
                }
            }
        }
    }


//    @SneakyThrows
//    public static void loadCheaterAPI(){
//        synchronized (known_cheaters_or_alts){
//            known_cheaters_or_alts.clear();
//
//
////            String requestResult = RequestUtil.requestResultAll.apply("");
//
//            JsonArray array = JsonParser.parseString(requestResult).getAsJsonArray();
//
//
//            for(JsonElement a : array.asList()){
//                try {
//                    String bg = a.getAsString().replaceFirst( "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5" );
//                    known_cheaters_or_alts.add(UUID.fromString(bg));
//                }catch (Exception ignored){
//                }
//            }
//
//
//
//        }
//
//    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        WatchdogSpeed speed = (WatchdogSpeed) Ambient.getInstance().getModuleManager().getModule("Speed").moduleModeProperties.get(0).getModuleModes().get("Watchdog");

        if(!speed.getSuperModule().isEnabled() && !speed.hasFinished && mc.thePlayer.airTicks >= 0 && mc.thePlayer.airTicks < 10) {
            switch (mc.thePlayer.airTicks) {
                case 0 -> speed.hasFinished = true;
                case 1 -> mc.thePlayer.motionY += 0.05;
                case 2 -> mc.thePlayer.motionY += 0.012;
                case 3 -> mc.thePlayer.motionY -= 0.135;
                case 4 -> mc.thePlayer.motionY -= 0.2;
            }
        }
    }





}
