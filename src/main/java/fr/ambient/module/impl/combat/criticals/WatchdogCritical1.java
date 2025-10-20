package fr.ambient.module.impl.combat.criticals;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Criticals;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.module.impl.player.Breaker;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;

public class WatchdogCritical1 extends ModuleMode {
    private int owo = 0;
    private final Criticals crit = (Criticals) this.getSuperModule();

    public WatchdogCritical1(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerNetworkTick(PreMotionEvent event) {
        if (critconditions()) {
            crit.docrit = true;

            if (crit.started) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ);
                crit.started = false;
            } else {
                event.setOnGround(crit.ticks == 0);

                if (crit.ticks == 1) {
                    double owo = 0.00001 + (Math.random() * (1e-35));
                    event.setPosY(event.getPosY() + owo);
                } else if (crit.ticks >= 2) {
                    crit.ticks = 0;
                    owo++;
                }

                crit.ticks++;
            }
        } else {
            crit.docrit = false;
            if (KillAura.target == null) {
                crit.started = true;
            }
        }
    }

    private boolean critconditions() {
        KillAura killAura = Ambient.getInstance().getModuleManager().getModule(KillAura.class);
        Speed speed = Ambient.getInstance().getModuleManager().getModule(Speed.class);

        return !PlayerUtil.isNearSlabAndStairs() && mc.thePlayer != null && mc.thePlayer.onGround && PlayerUtil.isBlockUnder(1) && !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down(1)).getBlock() instanceof BlockAir) &&
                KillAura.target != null && killAura.isEnabled() && mc.thePlayer.swingProgressInt != 0 && !speed.isEnabled() && mc.thePlayer.fallDistance <= 1 && Ambient.getInstance().getModuleManager().getModule(Breaker.class).breakPos == null &&
                !mc.gameSettings.keyBindJump.isKeyDown();
    }
}
