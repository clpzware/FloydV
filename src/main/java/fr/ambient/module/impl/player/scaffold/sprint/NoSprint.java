package fr.ambient.module.impl.player.scaffold.sprint;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import org.lwjglx.input.Keyboard;

public class NoSprint extends ModuleMode {
    public NoSprint(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdate(UpdateEvent event) {
        mc.thePlayer.setSprinting(false);
        mc.gameSettings.keyBindSprint.pressed = false;
        mc.gameSettings.keyBindSprint.unpressKey();
    }


    public void onDisable(){
        if(Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode())){
            mc.gameSettings.keyBindSprint.pressed = true;
        }
    }

}
