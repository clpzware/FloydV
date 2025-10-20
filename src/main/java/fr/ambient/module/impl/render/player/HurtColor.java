package fr.ambient.module.impl.render.player;

import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ColorProperty;
import fr.ambient.property.impl.NumberProperty;

import java.awt.*;

public class HurtColor extends Module {
    public HurtColor() {
        super(100, ModuleCategory.RENDER);
        registerProperties(hurtColor, transparency);
    }

    public ColorProperty hurtColor = ColorProperty.newInstance("Hurt Color", Color.RED);
    public NumberProperty transparency = NumberProperty.newInstance("Transparency", 0f, 1f, 1f, 0.1f);

}
