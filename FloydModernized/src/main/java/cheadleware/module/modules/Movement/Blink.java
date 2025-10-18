package cheadleware.module.modules.Movement;

import cheadleware.Cheadleware;
import cheadleware.enums.BlinkModules;
import cheadleware.event.EventTarget;
import cheadleware.event.types.EventType;
import cheadleware.event.types.Priority;
import cheadleware.events.LoadWorldEvent;
import cheadleware.events.TickEvent;
import cheadleware.module.Module;
import cheadleware.property.properties.IntProperty;
import cheadleware.property.properties.ModeProperty;

public class Blink extends Module {
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"DEFAULT", "PULSE"});
    public final IntProperty ticks = new IntProperty("ticks", 20, 0, 1200);

    public Blink() {
        super("Blink", false);
    }

    @EventTarget(Priority.LOWEST)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST) {
            if (!Cheadleware.blinkManager.getBlinkingModule().equals(BlinkModules.BLINK)) {
                this.setEnabled(false);
            } else {
                if (this.ticks.getValue() > 0 && Cheadleware.blinkManager.countMovement() > (long) this.ticks.getValue()) {
                    switch (this.mode.getValue()) {
                        case 0:
                            this.setEnabled(false);
                            break;
                        case 1:
                            Cheadleware.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                            Cheadleware.blinkManager.setBlinkState(true, BlinkModules.BLINK);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onWorldLoad(LoadWorldEvent event) {
        this.setEnabled(false);
    }

    @Override
    public void onEnabled() {
        Cheadleware.blinkManager.setBlinkState(false, Cheadleware.blinkManager.getBlinkingModule());
        Cheadleware.blinkManager.setBlinkState(true, BlinkModules.BLINK);
    }

    @Override
    public void onDisabled() {
        Cheadleware.blinkManager.setBlinkState(false, BlinkModules.BLINK);
    }
}
