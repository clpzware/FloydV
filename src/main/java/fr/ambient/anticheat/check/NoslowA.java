package fr.ambient.anticheat.check;

import fr.ambient.anticheat.Check;
import fr.ambient.module.impl.misc.Anticheat;

public class NoslowA extends Check {
    public NoslowA(Anticheat anticheat) {
        super("NoSlow", anticheat);
    }

    @Override
    public void onUpdate() {
        if (!anticheat.isEnabled() || !anticheat.checks.isSelected("Noslow")) {
            return;
        }

        mc.theWorld.playerEntities.forEach(player -> {
            if (player.isBlocking() && player.hurtTime == 0 && player.groundTicks > 3) {
                if (player.blockTicks > 3) {
                    float bpt = (float) (Math.pow(player.posX - player.lastTickPosX, 2) + Math.pow(player.posZ - player.lastTickPosZ, 2));
                    float bps = (float) (Math.sqrt(bpt) * 20) * mc.timer.timerSpeed;
                    if (bps > 3.7) {
                        flagPlayer(player, 1);
                    }
                }
            }
        });
    }
}
