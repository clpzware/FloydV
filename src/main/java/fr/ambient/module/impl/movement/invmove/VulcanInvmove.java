package fr.ambient.module.impl.movement.invmove;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Keyboard;

public class VulcanInvmove extends ModuleMode {
    private double maxSpeed = 0;

    public VulcanInvmove(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {

        updateKeyBindings();

        if (mc.currentScreen instanceof GuiInventory) {
            if (maxSpeed == 0) maxSpeed = MathHelper.clamp_double(MoveUtil.speed(), 0, 0.155);
            MoveUtil.strafe(maxSpeed * 0.999);
            maxSpeed *= 0.999;
        } else {
            if (maxSpeed != 0) {
                maxSpeed = 0;
            }
        }
    }

    private void updateKeyBindings() {
        mc.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        mc.gameSettings.keyBindBack.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        mc.gameSettings.keyBindLeft.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        mc.gameSettings.keyBindRight.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
        mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
    }
}
