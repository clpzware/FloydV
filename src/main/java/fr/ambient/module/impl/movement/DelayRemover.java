package fr.ambient.module.impl.movement;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.MultiProperty;

public class DelayRemover extends Module {

    public MultiProperty delay = MultiProperty.newInstance("Delay", new String[]{"NoJumpDelay", "NoClickDelay", "RawMouse"});

    public DelayRemover() {
        super(107, "Remove Some Delay", ModuleCategory.PLAYER);
        this.registerProperties(delay);
    }

    @SubscribeEvent
    public void onTickEvent(MovementEvent event) {
       if (delay.isSelected("NoJumpDelay")) {
           mc.thePlayer.jumpTicks = 0;
       }
       if (delay.isSelected("NoClickDelay")) {
           mc.leftClickCounter = 0;
       }
    }
}