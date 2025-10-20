package fr.ambient.util.player;

import fr.ambient.Ambient;
import fr.ambient.util.InstanceAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

@UtilityClass
public class ChatUtil implements InstanceAccess {
    public void display(final Object message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§9Ambient§7]§f » §7" + message));
        }else{
            System.out.println(message);
        }
    }
}