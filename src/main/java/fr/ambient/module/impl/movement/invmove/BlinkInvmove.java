package fr.ambient.module.impl.movement.invmove;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import org.lwjglx.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class BlinkInvmove extends ModuleMode {

    private final List<Packet> packets = new ArrayList<>();

    public BlinkInvmove(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        if (mc.currentScreen instanceof GuiChat) return;
        updateKeyBindings();
    }

    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof C0DPacketCloseWindow) {
            for (Packet p : packets) {
                PacketUtil.sendPacketNoEvent(p);
            }
            packets.clear();
        }
    }


    private void updateKeyBindings() {
        mc.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        mc.gameSettings.keyBindBack.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        mc.gameSettings.keyBindLeft.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        mc.gameSettings.keyBindRight.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
        mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
    }

    @Override
    public void onEnable() {
        packets.clear();
    }

    @Override
    public void onDisable() {
        packets.clear();
    }
}
