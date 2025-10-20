package fr.ambient.module.impl.combat;

import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.NumberProperty;

public class Reach extends Module {
    public Reach() {
        super(72, "Lets you hit enemies from farther away than normal.", ModuleCategory.COMBAT);
        this.registerProperty(reachDistance);
    }
    public NumberProperty reachDistance = NumberProperty.newInstance("Distance", 3f, 3f, 6f, 0.01f);
}
