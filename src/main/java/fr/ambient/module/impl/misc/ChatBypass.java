package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import net.minecraft.network.play.client.C01PacketChatMessage;

public class ChatBypass extends Module {
    public ChatBypass() {
        super(109, ModuleCategory.MISC);
        registerProperties(mode);
    }


    private ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Spaces", "Numbers", "Underscores", "Accent"}, "Spaces");


    @SubscribeEvent
    public void onSendPacketEvent(PacketSendEvent event){
        if(event.getPacket() instanceof C01PacketChatMessage c01PacketChatMessage){

            if(c01PacketChatMessage.getMessage().contains("/")){
                return;
            }
            c01PacketChatMessage.setMessage(trunkWords(c01PacketChatMessage.getMessage()));

        }
    }

    public String trunkWords(String mode){
        switch (this.mode.getValue()){
            case "Spaces" -> {
                return addCharAfter(mode, " ");
            }
            case "Underscores" -> {
                return addCharAfter(mode, "_");
            }
            case "Numbers" -> {
                return mode.replace("e", "3").replace("E", "3").replace("o", "0").replace("O", "0").replace("i", "1").replace("I", "1");
            }
            case "Accent" -> {
                return mode.replace("a", "á")
                        .replace("A", "Á")
                        .replace("c", "ç")
                        .replace("C", "Ç")
                        .replace("e", "é")
                        .replace("E", "É")
                        .replace("i", "í")
                        .replace("I", "Í")
                        .replace("n", "ñ")
                        .replace("N", "Ñ")
                        .replace("o", "ó")
                        .replace("O", "Ó")
                        .replace("u", "ú")
                        .replace("U", "Ú");
            }
        }
        return mode;
    }

    public static String addCharAfter(String str, String ta) {
        StringBuilder spacedString = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            spacedString.append(str.charAt(i));
            if (i < str.length() - 1) {
                spacedString.append(ta);
            }
        }

        return spacedString.toString();
    }


}
