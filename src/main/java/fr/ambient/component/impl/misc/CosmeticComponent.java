package fr.ambient.component.impl.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.component.Component;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.util.CosmeticData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.HashMap;
import java.util.List;

import static net.minecraft.client.gui.GuiPlayerTabOverlay.field_175252_a;

public class CosmeticComponent extends Component {
    public static HashMap<String, String> hhvs = new HashMap<>();

    public static HashMap<String, String> uidToUuid = new HashMap<>();

    public static HashMap<String, CosmeticData> cosData = new HashMap<>();



    public CosmeticComponent(){
        if(Ambient.getInstance().getConfig().getValue("capeId").equals("none")){
            customCapeId = "None";
        }else{
            customCapeId = Ambient.getInstance().getConfig().getValue("capeId");
        }

        if(Ambient.getInstance().getConfig().getValue("haloId").equals("none")){
            customHaloId = "None";
        }else{
            customHaloId = Ambient.getInstance().getConfig().getValue("haloId");
        }
    }

    public static String customCapeId = "None";
    public static String customHaloId = "None";

    @SubscribeEvent
    private void onTickEvent(UpdateEvent event){

        if(mc.thePlayer.ticksExisted % 20 == 0){
            JsonObject object = new JsonObject();

            JsonArray array = new JsonArray();
            NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
            List<NetworkPlayerInfo> list = field_175252_a.sortedCopy(nethandlerplayclient.getPlayerInfoMap());
            for (NetworkPlayerInfo networkplayerinfo : list) {
                JsonObject object1 = new JsonObject();

                object1.addProperty("name", networkplayerinfo.getGameProfile().getName());
                object1.addProperty("uuid", networkplayerinfo.getGameProfile().getId().toString());


                array.add(object1);
            }

            object.add("querylist", array);
            object.addProperty("id", "tabirc");
            object.addProperty("mode", "query");
        }



        /*if(mc.thePlayer.ticksExisted % 20 == 0){
            Ambient.getInstance().getBackend().sendEncrypted("tabirc:" + Ambient.getInstance().getToken() + ":userinfo");
            NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
            List<NetworkPlayerInfo> list = field_175252_a.sortedCopy(nethandlerplayclient.getPlayerInfoMap());
            for (NetworkPlayerInfo networkplayerinfo : list) {
                if(hhvs.containsKey(networkplayerinfo.getGameProfile().getId().toString())){
                    Ambient.getInstance().getBackend().sendEncrypted("tabirc:" + Ambient.getInstance().getToken() + ":usercosmetic:" + hhvs.get(networkplayerinfo.getGameProfile().getId().toString()));
                }
            }
        }
        if(mc.thePlayer.ticksExisted == 5){
            Ambient.getInstance().getBackend().sendEncrypted("tabirc:" + Ambient.getInstance().getToken() + ":login:" + mc.getSession().getProfile().getId().toString());
        }
        Cosmetics cosmetics = Ambient.getInstance().getModuleManager().getModule(Cosmetics.class);

        if(mc.thePlayer.ticksExisted % 100 == 0){
            Ambient.getInstance().getBackend().sendEncrypted("tabirc:" + Ambient.getInstance().getToken() + ":equipcape:" + cosmetics.getCapeMode() + ";" + cosmetics.getCurrMode());
        }
        if(!Objects.equals(cosmetics.lastCapeMode, cosmetics.mode.getValue())){
            Ambient.getInstance().getBackend().sendEncrypted("tabirc:" + Ambient.getInstance().getToken() + ":equipcape:" + cosmetics.getCapeMode() + ";" + cosmetics.getCurrMode());
            cosmetics.lastCapeMode = cosmetics.mode.getValue();
        }*/



    }


}
