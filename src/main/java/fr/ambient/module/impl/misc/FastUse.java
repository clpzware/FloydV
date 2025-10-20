package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C03PacketPlayer;

public class FastUse extends Module {
    private final ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Vanilla", "Matrix 1.17", "1.17"}, "1.17");
    private final NumberProperty packet = NumberProperty.newInstance("Packet", 1f, 1f, 32f, 1f);
    private final NumberProperty timer = NumberProperty.newInstance("Timer", 0.1f, 1f, 1f, 0.1f);

    public FastUse() {
        super(8, "Allows you to use items at an accelerated speed.", ModuleCategory.MISC);
        this.registerProperties(mode, packet,timer);
    }

    public void onEnable() {
        if (mode.is("Matrix 1.17")) {
            ChatUtil.display("Will only work if not moving");
        }
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1f;
    }

    @SubscribeEvent
    private void onPre(PreMotionEvent event) {
        int packetCount = packet.getValue().intValue();
        boolean isUsingItem = mc.thePlayer.isUsingItem() && !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword);

        switch (mode.getValue()) {
            case "Vanilla" -> {
                if (isUsingItem) {
                    for (int i = 0; i < packetCount; i++) {
                        PacketUtil.sendPacket(new C03PacketPlayer(mc.thePlayer.onGround));
                    }
                    mc.timer.timerSpeed = timer.getValue();
                }
            }
            case "Matrix 1.17" -> {
                if (isUsingItem && !MoveUtil.moving()) {
                    sendPlayerPositionPackets(packetCount);
                    mc.timer.timerSpeed = timer.getValue();
                }
            }
            case "1.17" -> {
                if (isUsingItem) {
                    sendPlayerPositionPackets(packetCount);
                    mc.timer.timerSpeed = timer.getValue();
                }
            }
        }

        if (!isUsingItem) {
            mc.timer.timerSpeed = 1.0f;
        }
    }

    private void sendPlayerPositionPackets(int packetCount) {
        for (int i = 0; i < packetCount; i++) {
            PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
        }
    }
}
