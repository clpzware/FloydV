package fr.ambient.module.impl.misc.disabler;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.misc.Disabler;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.util.List;

public class RinaorcDisabler extends ModuleMode {

    public boolean hasSendCommand = false;

    public RinaorcDisabler(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (((Disabler) this.getSuperModule()).rinaorc.isSelected("Auto Unlink")) {
            List<String> chatMessages = Minecraft.getMinecraft().ingameGUI.getChatGUI().getSentMessages();
            for (String chatMessage : chatMessages) {
                if (chatMessage.contains("Si vous vous déconnecter vous serez banni du serveur !")) {
                    if (!hasSendCommand) {
                        PacketUtil.sendPacketNoEvent(new C01PacketChatMessage("/hub"));
                        PacketUtil.sendPacketNoEvent(new C01PacketChatMessage("/discord unlink"));
                        hasSendCommand = true;
                        ChatUtil.display("Unlinked");
                    }
                }
                hasSendCommand = false;
            }
        }
    }

    @Override
    public void onEnable() {
        hasSendCommand = false;
    }
}
