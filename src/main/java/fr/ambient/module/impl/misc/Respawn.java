package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

public class Respawn extends Module {
    public ModeProperty respawn = ModeProperty.newInstance("Mode", new String[]{"PolarGround", "RinaorcHub"}, "RinaorcHub");
    public BooleanProperty auto = BooleanProperty.newInstance("Auto Respawn", false);

    public boolean hasSendCommand = false;

    public Respawn() {
        super(71,"Respawn", ModuleCategory.MISC);
        this.registerProperties(respawn, auto);
        this.setSuffix(respawn::getValue);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        hasSendCommand = false;
    }

    @SubscribeEvent
    private void onUpdate(UpdateEvent event) {
        if (mc.thePlayer != null) {
            switch (respawn.getValue()) {
                case "RinaorcHub":
                    if (!hasSendCommand) {
                        if (canAuto()) {
                            sendChatMessage();
                            hasSendCommand = true;
                        }
                    } else if (mc.thePlayer.inventory.getStackInSlot(6) != null
                            && mc.thePlayer.inventory.getStackInSlot(6).getItem() instanceof ItemSword
                            && mc.thePlayer.ticksExisted > 40
                            && mc.thePlayer.ticksExisted < 200) {

                        PacketUtil.sendPacket(new C09PacketHeldItemChange(6));
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(6)));

                        hasSendCommand = false;
                        ChatUtil.display("Adrien Iacono Respawn 1337");
                    }
                    break;
            }
        }
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.thePlayer != null) {
            if (respawn.getValue().equals("PolarGround")) {
                if (canAuto()) {
                    event.setPosY(event.getPosY() + 0.095);
                    event.setPosX(event.getPosX() + 0.095);
                    mc.thePlayer.onGround = true;
                    mc.thePlayer.jump();
                }
                hasSendCommand = false;
            }
        }
    }



    private void sendChatMessage() {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages("/hub");
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/hub");
    }

    public boolean canAuto() {
        if (auto.getValue()) {
            return mc.thePlayer.getHealth() < 4;
        }
        return true;
    }
}
