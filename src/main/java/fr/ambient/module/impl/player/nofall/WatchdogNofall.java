package fr.ambient.module.impl.player.nofall;


import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.player.NoFall;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

import java.awt.*;

public class WatchdogNofall extends ModuleMode {

    private final NoFall noFall = (NoFall) this.getSuperModule();

    public WatchdogNofall(String modeName, Module module) {
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
        switch (noFall.wdmode.getValue()) {
            case "Watchdog Packet" -> {
                if (mc.thePlayer.fallDistance > 3.0f && noFall.getDistanceToGround() != 0) {
                    mc.timer.timerSpeed = 0.5f;
                    PacketUtil.sendPacket(new C03PacketPlayer(true));
                    mc.thePlayer.fallDistance = 0f;
                } else {
                    mc.timer.timerSpeed = 1f;
                }
            }
            case "C08 Water" -> {
                if (noFall.CanNofall()) {
                    mc.timer.timerSpeed = 0.5f;
                    PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 0, new ItemStack(Items.water_bucket, 1), 0.5F, 0.5F, 0.5F));
                    PacketUtil.sendPacket(new C03PacketPlayer(true));
                    mc.thePlayer.fallDistance = 1f;
                } else {
                    mc.timer.timerSpeed = 1f;
                }
            }

            case "Dynamic" -> {
                if (mc.thePlayer.onGround) {
                    blinking = false;
                    ticks = 0;
                    mc.timer.timerSpeed = 1.0f;
                } else {
                    boolean canFall = noFall.getDistanceToGround() >= 2;
                    if (mc.thePlayer.airTicks < 2 && mc.thePlayer.motionY < 0 && canFall) {
                        blinking = true;
                    } else {
                        if (mc.thePlayer.fallDistance > 3.0f && noFall.getDistanceToGround() != 0) {
                            mc.timer.timerSpeed = 0.5f;
                            PacketUtil.sendPacket(new C03PacketPlayer(true));
                            mc.thePlayer.fallDistance = 0.0f;
                        } else {
                            mc.timer.timerSpeed = 1.0f;
                        }
                    }
                }

                if (blinking && ticks < 115) {
                    event.setOnGround(true);
                    mc.thePlayer.fallDistance = 0.0f;
                    BlinkComponent.onEnable();
                } else if (wasBlinking && !blinking) {
                    BlinkComponent.onDisable();
                }

                wasBlinking = blinking;
                ticks++;
            }
        }
    }
}