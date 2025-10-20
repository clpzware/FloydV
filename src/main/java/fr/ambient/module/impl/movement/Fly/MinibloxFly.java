package fr.ambient.module.impl.movement.Fly;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0CPacketInput;

public class MinibloxFly extends ModuleMode {


    public MinibloxFly(String modeName, Module module) {
        super(modeName, module);
    }


    @Override
    public void onEnable() {
        for (int i = 0; i < 70; i++) {
            mc.getNetHandler().addToSendQueue(new C0CPacketInput(mc.thePlayer.moveStrafing, mc.thePlayer.moveForward, mc.gameSettings.keyBindJump.isPressed() && mc.thePlayer.onGround, mc.thePlayer.isSprinting()));
        }
    }

    @Override
    public void onDisable() {
        MoveUtil.strafe(0);

        mc.timer.timerSpeed = 1f;
        for (int i = 0; i < 70; i++) {
            mc.getNetHandler().addToSendQueue(new C0CPacketInput(mc.thePlayer.moveStrafing, mc.thePlayer.moveForward, mc.gameSettings.keyBindJump.isPressed() && mc.thePlayer.onGround, mc.thePlayer.isSprinting()));
        }
        MoveUtil.strafe(0);
    }


    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        mc.thePlayer.motionY = mc.gameSettings.keyBindJump.pressed ? 0.5f : mc.gameSettings.keyBindSneak.pressed ? -0.5 : 0;
        if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
            PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getPosition().add(0, 1, 0), 255, new ItemStack(Blocks.web), 0, 0, 0));
            if (MoveUtil.moving()) {
                MoveUtil.strafe(0.34f, 0.4f, 0.43f);
            } else {
                MoveUtil.strafe(0);
            }
        }
    }
}