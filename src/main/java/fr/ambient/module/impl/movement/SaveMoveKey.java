package fr.ambient.module.impl.movement;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import net.minecraft.client.settings.KeyBinding;



public class SaveMoveKey extends Module {
    private boolean wasInventoryOpen = false;

    public SaveMoveKey() {
        super(16,"Allows you to move immediately after coming out of an inventory", ModuleCategory.MOVEMENT);
    }

    @SubscribeEvent
    private void onPlayerTick(UpdateEvent event) {
        if (mc.currentScreen != null) {
            wasInventoryOpen = true;
        } else {
            if (wasInventoryOpen) {
                mc.addScheduledTask(() -> {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), org.lwjglx.input.Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), org.lwjglx.input.Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), org.lwjglx.input.Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), org.lwjglx.input.Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), org.lwjglx.input.Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), org.lwjglx.input.Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
                });
                wasInventoryOpen = false;
            }
        }
    }
}
