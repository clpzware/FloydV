package fr.ambient.module.impl.movement.longjump;

import fr.ambient.Ambient;
import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.module.impl.combat.Velocity;
import fr.ambient.module.impl.movement.LongJump;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.render.styles.ModernStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class WatchdogFbLongjump extends ModuleMode {

    private boolean wasenabled;
    private int ticksSinceEnabled = 0;
    private int oldSlot = -1;
    boolean start;
    int ticks;
    private BlockPos startPos;
    boolean done, stop;
    int cancelTicks;

    public WatchdogFbLongjump(String modeName, Module module) {
        super(modeName, module);
    }

    public void onEnable() {
        startPos = mc.thePlayer.getPosition();

        oldSlot = mc.thePlayer.inventory.currentItem;
        ticksSinceEnabled = 0;
        ticks = 0;
        start = false;
        done = false;
        wasenabled = Ambient.getInstance().getModuleManager().getModule(Velocity.class).isEnabled();
        Ambient.getInstance().getModuleManager().getModule(Velocity.class).setEnabled(false);
        BlinkComponent.onDisable();
    }

    public void onDisable() {
        if (getFireballSlot() != 1 && mc.thePlayer.inventory.currentItem == getFireballSlot()) {
            mc.thePlayer.inventory.currentItem = oldSlot;
        }
        start = false;
        if (KillAura.target == null) {
            BlinkComponent.onDisable();
        }
        if (wasenabled) {
            Ambient.getInstance().getModuleManager().getModule(Velocity.class).setEnabled(true);
        }
        start = false;
        done = false;
        stop = false;
        ticks = 0;
        cancelTicks = 0;
    }

    @SubscribeEvent
    private void onPre(PreMotionEvent e) {
        ticksSinceEnabled++;

        if (((LongJump) this.getSuperModule()).auto.getValue()) {
            if (ticksSinceEnabled < 4) {
                int temp = getFireballSlot();
                if (temp != -1) {
                    mc.thePlayer.inventory.currentItem = temp;
                } else {
                    Ambient.getInstance().getModuleManager().getModule(LongJump.class).setEnabled(false);
                    return;
                }

                e.setPitch(89);
                e.setYaw(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - 180));

                mc.thePlayer.rotationYawHead = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - 180);
                mc.thePlayer.renderPitchHead = 89;

                if (ticksSinceEnabled == 3) {
                    mc.rightClickMouse();
                }
            }
        }

        if (mc.thePlayer.hurtTime >= 3 && !done) {
            start = true;
        }

        if (start) {
            ticks++;
        }

        if (start) {
            if (mc.thePlayer.hurtTime == 9 && ticks < 10) {
                MoveUtil.strafe(1.9425);
            } else if (ticks <= 10) {
                MoveUtil.strafe(MoveUtil.speed() * (ticks % 3 == 0 ? 1.0163 : 1.013));
            }

            switch (ticks) {
                case 1:
                    mc.thePlayer.motionY += 0.000183f;
                    break;
                case 25:
                    if (MoveUtil.speed() > 0.4) {
                        MoveUtil.strafe(0.4625);
                        mc.thePlayer.motionY += 0.4f;
                    }
                    break;
                case 26:
                    if (MoveUtil.speed() > 0.4) {
                        mc.thePlayer.motionY += 0.2f;
                    }
                    break;
                default:
                    if (ticks < 26) {
                        mc.thePlayer.motionY += (ticks % 2 == 0) ? 0.028f : 0.019f;
                    } else {
                        mc.thePlayer.motionY += 0.028f;
                    }
                    break;
            }
        }

        if (done || ticks >= 50 || (mc.thePlayer.onGround && ticks > 5)) {
            Ambient.getInstance().getModuleManager().getModule(LongJump.class).setEnabled(false);
        }
    }



    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        ModernStyle.drawProgress((float) ticks / 50);
    }

    private int getFireballSlot() {
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i) == null) {
                continue;
            }
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() == Items.fire_charge) {
                return i;
            }
        }
        return -1;
    }
}
