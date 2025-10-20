package fr.ambient.module.impl.player.antivoid;

import fr.ambient.Ambient;
import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.module.impl.movement.LongJump;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.module.impl.player.AntiVoid;
import fr.ambient.module.impl.player.Scaffold;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;

import java.awt.*;

public class BlinkAntivoid extends ModuleMode {

    private boolean falling = false, air = false;
    private int ticks;
    private int jumpTicks = 0;
    public boolean blinking = false;

    public BlinkAntivoid(String modeName, Module module) {
        super(modeName, module);
    }

    public Vec3 predictedPoint(double mult) {
        return new Vec3(mc.thePlayer.posX + (mc.thePlayer.motionX * mult), mc.thePlayer.posY + (mc.thePlayer.motionY * mult), mc.thePlayer.posZ + (mc.thePlayer.motionZ * mult));
    }


    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        AntiVoid av = (AntiVoid) this.getSuperModule();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        if (blinking) {
            switch (av.blinkindicator.getValue()) {
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
    public void onUpdate(PreMotionEvent event) {
        if (Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() && mc.gameSettings.keyBindJump.isKeyDown() || (Ambient.getInstance().getModuleManager().getModule(LongJump.class).isEnabled())) {
            return;
        }
        AntiVoid av = (AntiVoid) this.getSuperModule();

        if (mc.thePlayer != null) {
            Vec3 vec3 = predictedPoint(0.5);

            if (jumpTicks-- <= 0 && !mc.thePlayer.capabilities.isFlying && av.isAboveVoid(vec3.xCoord, vec3.yCoord, vec3.zCoord) && !falling && !mc.thePlayer.onGround && KillAura.target == null) {
                falling = true;
                blinking = true;
                BlinkComponent.onEnable();
            } else if (falling && mc.thePlayer.fallDistance > 8 && av.isAboveVoid(vec3.xCoord, vec3.yCoord, vec3.zCoord) && !air) {
                Ambient.getInstance().getModuleManager().getModule(Speed.class).setEnabled(false);
                air = true;
                mc.getNetHandler().getNetworkManager().sendPacketNoEventAbsolute(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, -620, mc.thePlayer.posZ, false));
                blinking = false;
                BlinkComponent.onDisable();
            } else if (falling && (mc.thePlayer.onGround || !av.isAboveVoid(vec3.xCoord, vec3.yCoord, vec3.zCoord))) {
                falling = air = false;
                blinking = false;
                BlinkComponent.onDisable();
            }

            if (blinking) {
                ticks++;
            } else {
                ticks = 0;
            }
        }
    }
}