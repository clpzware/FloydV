package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.util.player.ChatUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

public class ClientSpoofer extends Module {

    public final ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Lunar", "Forge", "Geyser"}, "Lunar");

    public ClientSpoofer() {
        super(118, "ClientSpoofer", ModuleCategory.MISC);
        this.registerProperties(mode);
    }


    @Override
    public void  onEnable() {
        ChatUtil.display("Relog For It to apply");
    }


    private C17PacketCustomPayload getC17() {
        String spoofedBrand;
        switch (mode.getValue()) {
            case "Lunar" -> spoofedBrand = "lunarclient:v2.18.11-2510";
            case "Forge" -> spoofedBrand = "fml,forge";
            case "Geyser" -> spoofedBrand = "Geyser";
            default -> spoofedBrand = ClientBrandRetriever.getClientModName();
        }
        return new C17PacketCustomPayload("MC|Brand", new PacketBuffer(Unpooled.buffer()).writeString(spoofedBrand));
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer != null && mc.thePlayer.sendQueue != null) {
            mc.thePlayer.sendQueue.addToSendQueue(getC17());
        }
    }
}
