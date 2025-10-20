package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Regen extends Module {
    private final ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Vanilla", "1.17", "Command"}, "1.17");
    private final NumberProperty packet = NumberProperty.newInstance("Packet", 1f, 1f, 32f, 1f, () -> mode.is("Vanilla") || mode.is("1.17"));
    private final NumberProperty timer = NumberProperty.newInstance("Timer", 0.1f, 1f, 1f, 0.1f, () -> mode.is("Vanilla") || mode.is("1.17"));
    private final NumberProperty health = NumberProperty.newInstance("Health", 1f, 1f, 20f, 1f);

    public Regen() {
        super(95, "Allows you to regain hp", ModuleCategory.MISC);
        this.registerProperties(mode, packet, health, timer);
    }


    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1f;
    }

    @SubscribeEvent
    private void onPre(PreMotionEvent e) {
        boolean isRegenerating = mc.thePlayer.getHealth() < health.getValue();

        switch (mode.getValue()) {
            case "Command" -> {
                if (isRegenerating) {
                    PacketUtil.sendPacketNoEvent(new C01PacketChatMessage("/heal"));
                }
            }
            case "Vanilla" -> {
                if (isRegenerating) {
                    for (int i = 0; i < packet.getValue(); ++i) {
                        PacketUtil.sendPacket(new C03PacketPlayer(mc.thePlayer.onGround));
                    }
                    mc.timer.timerSpeed = timer.getValue();
                }
            }
            case "1.17" -> {
                if (isRegenerating) {
                    for (int i = 0; i < packet.getValue(); ++i) {
                        PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
                    }
                    mc.timer.timerSpeed = timer.getValue();
                }
            }
        }

        if (!isRegenerating) {
            mc.timer.timerSpeed = 1.0f;
        }
    }
}
