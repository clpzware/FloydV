package fr.ambient.module.impl.player.nofall;


import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.player.NoFall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;


public class BlinkNofall extends ModuleMode {

    private final NoFall noFall = (NoFall) this.getSuperModule();

    public BlinkNofall(String modeName, Module module) {
        super(modeName, module);
    }


    public boolean blinking = false;
    private boolean wasBlinking = false;
    private int ticks = 0;

    public void onDisable() {
        ticks = 0;
        blinking = false;
        wasBlinking = false;
        mc.timer.timerSpeed = 1f;
        BlinkComponent.onDisable();
    }




    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        if (blinking) {
            switch (noFall.blinkindicator.getValue()) {
                case "Legit":
                    mc.fontRendererObj.drawStringWithShadow("Blinking...", sr.getScaledWidth() / 1.9f, sr.getScaledHeight() / 1.9f, Color.WHITE.getRGB());
                    mc.fontRendererObj.drawStringWithShadow("Ticks : " + ticks, sr.getScaledWidth() / 1.9f, sr.getScaledHeight() / 1.9f + 12, Color.WHITE.getRGB());
                    break;
                case "Raven":
                    mc.fontRendererObj.drawStringWithShadow("§fblinking : §a" + ticks, sr.getScaledWidth() / 1.9f, sr.getScaledHeight() / 1.9f, Color.WHITE.getRGB());
                    break;
                case "Number":
                    mc.fontRendererObj.drawStringWithShadow("§a" + ticks, sr.getScaledWidth() / 1.9f, sr.getScaledHeight() / 1.9f, Color.WHITE.getRGB());
                    break;
                case "None":
                    break;
            }
        }
    }



    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            blinking = false;
            ticks = 0;
        } else {
            boolean canFall = !(noFall.getDistanceToGround() < 2);

            if (mc.thePlayer.airTicks < 2 && mc.thePlayer.motionY < 0 && canFall) {
                blinking = true;
            }
        }

        if (blinking && ticks < 115) {
            event.setOnGround(true);
            mc.thePlayer.fallDistance = 0;
            BlinkComponent.onEnable();
        } else if (wasBlinking && !blinking) {
            BlinkComponent.onDisable();
        }

        wasBlinking = blinking;
        ticks++;
    }
}
