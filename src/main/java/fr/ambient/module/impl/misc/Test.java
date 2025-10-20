package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;

public class Test extends Module {

    private final ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Test"}, "Test");
    private final NumberProperty timer = NumberProperty.newInstance("Timer", 0.1f, 0.8f, 10f, 0.1f);

    public Test() {
        super(11, "Test", ModuleCategory.MISC);
        this.registerProperties(mode, timer);
    }

    public void onEnable() {

    }

    @SubscribeEvent
    private void onPlayerNetworkTick(PreMotionEvent event) {
        switch (mode.getValue()) {
            case "Test" -> {

            }
        }
    }
}
