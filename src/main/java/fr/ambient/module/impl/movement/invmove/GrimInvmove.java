package fr.ambient.module.impl.movement.invmove;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import org.lwjglx.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class GrimInvmove extends ModuleMode {

    private boolean magic = false;
    private int magic2 = 0;
    private final List<Packet> packets = new ArrayList<>();

    public GrimInvmove(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onEnable() {
        packets.clear();
    }

    @Override
    public void onDisable() {
        packets.clear();
        magic = false;
        magic2 = 0;
    }


@SubscribeEvent
    public void onMoveEvent(MovementEvent event) {
        if (!(mc.currentScreen instanceof GuiInventory)) {
            if (magic) {
                mc.thePlayer.movementInput.moveForward = 0;
                mc.thePlayer.movementInput.moveStrafe = 0;
                ChatUtil.display(magic2);
            }
        }
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        if (mc.currentScreen instanceof GuiChat) return;

        updateKeyBindings();

        if (mc.currentScreen instanceof GuiInventory) {
            magic = true;
        } else {
            if (magic) {
                magic2++;
                resetMovementKeys();
                if (magic2 > 3) {
                    for (Packet p : packets) {
                        PacketUtil.sendPacketNoEvent(p);
                    }
                    packets.clear();
                    magic2 = 0;
                    magic = false;
                } else {
                    PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                }
            }
        }
    }

    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof C16PacketClientStatus || event.getPacket() instanceof C0EPacketClickWindow) {
            packets.add(event.getPacket());
            event.setCancelled(true);
        }
    }

    private void resetMovementKeys() {
        mc.gameSettings.keyBindForward.pressed = false;
        mc.gameSettings.keyBindLeft.pressed = false;
        mc.gameSettings.keyBindRight.pressed = false;
        mc.gameSettings.keyBindBack.pressed = false;
    }


    private void updateKeyBindings() {
        mc.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        mc.gameSettings.keyBindBack.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        mc.gameSettings.keyBindLeft.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        mc.gameSettings.keyBindRight.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
        mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
    }
}