package fr.ambient.module.impl.movement;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;

public class Step extends Module {


    private final ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Vanilla"}, "Vanilla");
    private final NumberProperty height = NumberProperty.newInstance("Height", 1f, 1f, 10f, 1f, () -> mode.is("Vanilla"));


    public Step() {
        super(79, "Automatically steps up blocks without needing to jump.", ModuleCategory.MOVEMENT);
        this.registerProperties(mode, height);
    }

    public void onDisable() {
        mc.thePlayer.stepHeight = 0.5f;
    }



    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        switch (mode.getValue()) {
            case "Vanilla" -> mc.thePlayer.stepHeight = height.getValue();
            case "Watchdog" -> {
                if (mc.thePlayer.isCollidedHorizontally) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.41999998688698;
                    }
                    mc.thePlayer.motionY += switch (mc.thePlayer.airTicks) {
                        case 3 -> -0.135;
                        case 4 -> -0.2;
                        case 5 -> -0.19;
                        default -> 0;
                    };
                }
            }
        }
    }
}