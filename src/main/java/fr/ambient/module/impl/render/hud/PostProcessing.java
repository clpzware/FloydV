package fr.ambient.module.impl.render.hud;

import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.render.GlowUtil;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PostProcessing extends Module {
    public PostProcessing() {
        super(74,"Applies visual effects like blur or glow to your game.", ModuleCategory.RENDER);
        this.registerProperties(blur, glow, radius);
    }

    public BooleanProperty blur = BooleanProperty.newInstance("Blur ", true);
    public BooleanProperty glow = BooleanProperty.newInstance("Glow", true);
    public NumberProperty radius = NumberProperty.newInstance("Radius", 10f, 10f, 30f, 1f, glow::getValue);

    public void onDisable() {
        if(Thread.currentThread().getName().equals("main")){
            GlowUtil.refreshFramebuffers();
        }
    }
}
