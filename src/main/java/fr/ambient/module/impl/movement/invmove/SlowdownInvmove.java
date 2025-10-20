package fr.ambient.module.impl.movement.invmove;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.lwjglx.input.Keyboard;

public class SlowdownInvmove extends ModuleMode {


    public SlowdownInvmove(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        if (mc.currentScreen instanceof GuiChat) return;
        updateKeyBindings();

        if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest) {
            PotionEffect speedEffect = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed);
            float slowMultiplier = speedEffect != null ? 0.22f : 0.65f;

            mc.thePlayer.motionX *= slowMultiplier;
            mc.thePlayer.motionZ *= slowMultiplier;
        }
    }

    private void updateKeyBindings() {
        mc.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        mc.gameSettings.keyBindBack.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        mc.gameSettings.keyBindLeft.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        mc.gameSettings.keyBindRight.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());

        if (mc.currentScreen instanceof GuiChest) {
            mc.gameSettings.keyBindJump.pressed = false;
        }
    }
}

