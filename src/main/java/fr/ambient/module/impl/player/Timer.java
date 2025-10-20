package fr.ambient.module.impl.player;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;

public class Timer extends Module {

    private final NumberProperty timer = NumberProperty.newInstance("Timer", 0.1f, 1f, 10f, 0.01f);
    private final BooleanProperty mushmc = BooleanProperty.newInstance("MushMc Timer", false);
    private int tickCounter = 0;

    public Timer() {
        super(38, "Speeds up or slows down the game for you.", ModuleCategory.PLAYER);
        this.registerProperties(timer, mushmc);
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mushmc.getValue()) {
            tickCounter++;
            if (tickCounter <= 30 && mc.thePlayer.onGround) {
                mc.timer.timerSpeed = 2f;
            } else if (tickCounter <= 50) {
                mc.timer.timerSpeed = 0.89f;
            } else {
                tickCounter = 0;
            }
        } else {
            mc.timer.timerSpeed = timer.getValue();
        }
    }


    @Override
    public void onEnable() {
        tickCounter = 0;
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1f;
    }
}
