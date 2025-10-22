package femcum.modernfloyd.clients.util.chat;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.util.Accessor;
import femcum.modernfloyd.clients.util.localization.Localization;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 * This is a chat util which can be used to do various things related to chat
 */
@UtilityClass
public class ChatUtil implements Accessor {

    /**
     * Adds a message to the players chat without sending it to the server
     *
     * @param message message that is going to be added to chat
     */
    public void display(final Object message, final Object... objects) {
        if (mc.thePlayer != null) {
            final String format = String.format(Localization.get(message.toString()), objects);
            mc.thePlayer.addChatMessage(new ChatComponentText(getPrefix() + format));
            //System.out.println(message);
        }
    }

    public void debug(final Object message, final Object... objects) {
        if (Floyd.DEVELOPMENT_SWITCH) display(message, objects);
    }

    public void displayNoPrefix(final Object message, final Object... objects) {
        if (mc.thePlayer != null) {
            final String format = String.format(message.toString(), objects);
            mc.thePlayer.addChatMessage(new ChatComponentText(format));
        }
    }

    /**
     * Sends a message in the chat
     *
     * @param message message that is going to be sent in chat
     */
    public void send(final Object message) {
        if (mc.thePlayer != null) {
            PacketUtil.send(new C01PacketChatMessage(message.toString()));
        }
    }

    private String getPrefix() {
        final String color = Floyd.INSTANCE.getThemeManager().getTheme().getChatAccentColor().toString();
        return EnumChatFormatting.BOLD + color + Floyd.NAME
                + EnumChatFormatting.RESET + color + " » "
                + EnumChatFormatting.RESET;
    }
}
