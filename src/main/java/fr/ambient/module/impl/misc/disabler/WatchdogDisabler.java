package fr.ambient.module.impl.misc.disabler;

import fr.ambient.Ambient;
import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.event.impl.world.WorldChangeEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.module.impl.misc.Disabler;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.module.impl.player.Breaker;
import fr.ambient.ui.framework.Style;
import fr.ambient.ui.framework.UIComponent;
import fr.ambient.ui.framework.impl.UIProgressComponent;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import java.awt.*;

public class WatchdogDisabler extends ModuleMode {

    private boolean isOnHypixel = false;
    private boolean isInLimbo = false;
    private boolean isInReplay = false;
    private int lagback = 0;
    private static boolean disabled = false;

    private final Disabler disabler = (Disabler) this.getSuperModule();
    private final Animation animation = new Animation(Easing.EASE_IN_OUT_QUAD, 100);


    public WatchdogDisabler(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onDisable() {
        disabled = false;
    }

    @Override
    public void onEnable() {
        lagback = 0;
    }


    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S02PacketChat s02PacketChat) {
            String chatMessage = s02PacketChat.getChatComponent().getUnformattedText();
            isInLimbo = chatMessage.contains("/limbo for more information.");
            isInReplay = chatMessage.contains("Attempting to load replay...");
        }
        if (event.getPacket() instanceof S08PacketPlayerPosLook && !disabled) {
            lagback++;
            if (lagback == disabler.WDLagback.getValue()) {
                disabled = true;
                lagback = 0;
            }
        }
    }


    ScaledResolution sr = new ScaledResolution(mc);
    UIComponent progressBar =
            new UIProgressComponent()
                    .progress(animation.getFloatValue())
                    .background(new Color(0x65000000, true))
                    .position(sr.getScaledWidth() / 2f - 70, sr.getScaledHeight() / 2f + 50)
                    .size(140, 8)
                    .rounding(3.5f);


    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        if (disabler.progressbar.getValue() && !lobby() && !disabled && !isInLimbo && !isInReplay && isOnHypixel) {
            animation.setDuration(300);
            animation.run(lagback / disabler.WDLagback.getValue());

            ScaledResolution sr = new ScaledResolution(mc);

            UIProgressComponent progressComponent = (UIProgressComponent) progressBar;
            progressComponent.progress(animation.getFloatValue());
            progressBar.color(Style.HORIZONTAL, Ambient.getInstance().getHud().getColor1(), Ambient.getInstance().getHud().getColor2());
            progressBar.position(sr.getScaledWidth() / 2f - 70, sr.getScaledHeight() / 2f + 50);
            progressBar.render();
            Fonts.getNunito(13).drawCenteredString(String.format("%.0f", animation.getValue() * 100) + "%", (sr.getScaledWidth() / 2f - 70) + (140 * animation.getFloatValue()), sr.getScaledHeight() / 2f + 50 - 7, -1);

        } else {
            animation.setValue(0.0f);
        }
    }



    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMoveEvent(MovementEvent movementEvent) {
        if (mc.thePlayer.movementInput.moveForward < 0 && mc.thePlayer.onGround && Ambient.getInstance().getModuleManager().getModule(KillAura.class).isEnabled() && !Ambient.getInstance().getModuleManager().getModule(Speed.class).isEnabled()) {
            mc.thePlayer.motionX *= 0.85;
            mc.thePlayer.motionZ *= 0.85;
        }
        if (mc.thePlayer.movementInput.moveForward < 0 && mc.thePlayer.onGround && Ambient.getInstance().getModuleManager().getModule(Breaker.class).breakPos != null && !Ambient.getInstance().getModuleManager().getModule(Speed.class).isEnabled()) {
            mc.thePlayer.motionX *= 0.85;
            mc.thePlayer.motionZ *= 0.85;
        }
    }


    @SubscribeEvent
    private void onWorldChange(WorldChangeEvent event) {
        if (isOnHypixel) {
            disabled = false;
            lagback = 0;
        }
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {

        isOnHypixel = mc.getCurrentServerData() != null && !mc.isSingleplayer() && (mc.getCurrentServerData().serverIP.contains("hypixel.net") || mc.getCurrentServerData().serverIP.contains("buzz")
                || mc.getCurrentServerData().serverIP.contains("nyap") || mc.getCurrentServerData().serverIP.contains("ilovecatgirls.xyz") || mc.getCurrentServerData().serverIP.endsWith(".liquidproxy.net"));

        if (isInLimbo || isInReplay) return;

        if (disabler.watchdog.isSelected("Motion Y") && isOnHypixel && !lobby()) {
            if (disabled || mc.thePlayer.ticksExisted < 2) return;

            BlinkComponent.onDisable();
            Ambient.getInstance().getModuleManager().getModule(KillAura.class).setEnabled(false);

            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.42f;
            } else if (mc.thePlayer.airTicks >= 10) {
                if (mc.thePlayer.airTicks % 2 == 0) {
                    event.setPosZ(event.getPosZ() + 0.095 * Math.random());
                    event.setYaw(360);
                }
                mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0.0;
            }
        }
    }

    public boolean lobby() {
        for (String itemName : new String[]{"minecraft:compass"}) {
            Item item = Item.getByNameOrId(itemName);
            if (item != null) {
                for (int i = 0; i < 9; i++) {
                    ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (itemStack != null && itemStack.getItem() == item) return true;
                }
            }
        }
        return false;
    }
}
