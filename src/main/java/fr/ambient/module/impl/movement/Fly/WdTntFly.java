package fr.ambient.module.impl.movement.Fly;


import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.render.styles.ModernStyle;


public class WdTntFly extends ModuleMode {

    private int tickcounter = 0;
    private boolean shouldBlink = false;

    public WdTntFly(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onEnable() {
        ChatUtil.display("Place a tnt Before Flying.");
    }


    @Override
    public void onDisable() {
        MoveUtil.strafe(0);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
        tickcounter = 0;
        shouldBlink = false;
        BlinkComponent.onDisable();
    }





    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        ModernStyle.drawProgress((float) tickcounter / 60);
    }



    @SubscribeEvent
    public void onTick(UpdateEvent event) {
        mc.thePlayer.motionY = mc.gameSettings.keyBindJump.pressed ? 2f : mc.gameSettings.keyBindSneak.pressed ? - 2f : 0;
        if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
            MoveUtil.strafe(2);
        } else {
            MoveUtil.strafe(0);
        }

        if (shouldBlink) {
            if (tickcounter < 60) {
                BlinkComponent.onEnable();
                tickcounter++;
            } else {
                BlinkComponent.onDisable();
                shouldBlink = false;
                this.getSuperModule().setEnabled(false);
            }
        } else {
            tickcounter = 0;
            shouldBlink = true;
        }
    }
}