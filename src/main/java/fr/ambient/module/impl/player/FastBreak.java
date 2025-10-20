package fr.ambient.module.impl.player;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.world.TickEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.NumberProperty;

public class FastBreak extends Module {

    private final NumberProperty multiplier = NumberProperty.newInstance("Multiplier", 1.0f, 1.15f, 3.0f, 0.05f);

    public FastBreak() {
        super(31,"Allows you to breaks blocks much faster than usual.", ModuleCategory.PLAYER);

        this.registerProperty(multiplier);
        this.setSuffix(() -> multiplier.getValue().toString());
    }

    @SubscribeEvent
    private void onTick(TickEvent event) {
        final double multiplier = this.multiplier.getValue();
        final float damage = mc.playerController.curBlockDamageMP;

        if (multiplier > 1.0) {
            final double endPoint = 1.0 / multiplier;

            if (damage < 1.0f && damage >= endPoint) {
                mc.playerController.curBlockDamageMP = 1;
            }
        }
    }
}
