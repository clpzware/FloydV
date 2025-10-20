package fr.ambient.module.impl.misc;

import fr.ambient.Ambient;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.NumberProperty;

public class SmoothReset extends Module {
    public SmoothReset() {
        super(96, ModuleCategory.MISC);
        registerProperties(smoothResetTicks);
    }

    public NumberProperty smoothResetTicks = NumberProperty.newInstance("Smooth Reset Ticks", 2f, 5f, 10f, 1f);

    public void onEnable(){
        Ambient.getInstance().getRotationComponent().smoothReset = true;
        Ambient.getInstance().getRotationComponent().smoothResetTicks = smoothResetTicks.getValue().intValue();
    }
    public void onDisable(){
        Ambient.getInstance().getRotationComponent().smoothReset = false;
    }
}
