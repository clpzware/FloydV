package fr.ambient.anticheat.check;

import fr.ambient.anticheat.Check;
import fr.ambient.module.impl.misc.Anticheat;

public class NoslowB extends Check {
    public NoslowB(Anticheat anticheat) {
        super("NoSlow", anticheat);
    }

    @Override
    public void onUpdate() {
        if (!anticheat.isEnabled() || !anticheat.checks.isSelected("Noslow")) {
            return;
        }

        mc.theWorld.playerEntities.forEach(player -> {
            if (player.blockTicks > 3) {
                if (player.sprintTicks > 3) {
                    flagPlayer(player, 1);
                }
            }
        });
    }
}
