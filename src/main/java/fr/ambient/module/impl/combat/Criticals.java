package fr.ambient.module.impl.combat;

import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.criticals.*;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;
import fr.ambient.util.math.TimeUtil;
import net.minecraft.entity.EntityLivingBase;

public class Criticals extends Module {

    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Watchdog",
            new WatchdogCritical2("Watchdog 2", this),
            new WatchdogCritical1("Watchdog", this),
            new MinibloxCritical("Miniblox", this),
            new MosPixelCriticial("MosPixel", this),
            new NoGroundCritical("Noground", this),
            new StillCritical("Still", this),
            new JumpCritical("Jump", this),
            new TimerCritical("Timer", this)
    );


    public final NumberProperty timer = NumberProperty.newInstance("Timer", 0.1f, 1f, 10f, 0.01f, () -> mode.getModeProperty().is("Timer"));
    public final NumberProperty delay = NumberProperty.newInstance("Delay", 0f, 500f, 1000f, 50f);

    public EntityLivingBase target;
    public boolean go = false;
    public boolean started = false;
    public int ticks = 0;
    public boolean slab = false;
    public  boolean docrit = false;

    public Criticals() {
        super(3, "Makes all your hits do critical damage.", ModuleCategory.COMBAT);
        this.registerProperties(mode.getModeProperty(), timer);
        this.setSuffix(mode.getModeProperty()::getValue);
        this.moduleModeProperties.add(mode);
    }


    public final TimeUtil stopwatch = new TimeUtil();

    public void onDisable() {
        mc.timer.timerSpeed = 1f;
        started = false;
    }
}