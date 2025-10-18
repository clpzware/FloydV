package cheadleware.module.modules.Render;

import cheadleware.Cheadleware;
import cheadleware.module.Module;
import cheadleware.property.properties.BooleanProperty;
import cheadleware.property.properties.FloatProperty;
import cheadleware.property.properties.ModeProperty;

public class Animations extends Module {

    // Position
    public final FloatProperty x = new FloatProperty("x", 0.0F, -2.0F, 2.0F);
    public final FloatProperty y = new FloatProperty("y", 0.0F, -2.0F, 2.0F);
    public final FloatProperty z = new FloatProperty("z", 0.0F, -2.0F, 2.0F);
    public final FloatProperty scale = new FloatProperty("scale", 1.0F, 0.1F, 5.0F);

    // Animations
    public final BooleanProperty oldBlocking = new BooleanProperty("1.7-blocking", true);
    public final BooleanProperty smoothSwing = new BooleanProperty("smooth-swing", true);
    public final FloatProperty swingSpeed = new FloatProperty("swing-speed", 1.0F, 0.1F, 2.0F);

    // Swing Animation Mode
    public final ModeProperty mode = new ModeProperty("mode", 2, new String[]{
            "EXHIBITION", "SIGMA", "VANILLA", "PLAIN", "SPIN", "ETB", "DORTWARE",
            "AVATAR", "SWONG", "SWANG", "SWANK", "STYLES", "NUDGE", "PUNCH",
            "SLIDE", "JIGSAW", "WIZZARD", "LENNOX", "LUCKY"
    });

    public Animations() {
        super("Animations", false);
    }

    public static Animations getInstance() {
        return (Animations) Cheadleware.moduleManager.getModule(Animations.class);
    }
}