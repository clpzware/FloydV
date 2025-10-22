package femcum.modernfloyd.clients.module.impl.combat;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.combat.velocity.*;
import femcum.modernfloyd.clients.module.impl.combat.velocity.*;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.combat.velocity.name"}, description = "module.combat.velocity.description" /* Sorry, Tecnio. */ /* Sorry Hazsi. */, category = Category.COMBAT)
public final class Velocity extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new StandardVelocity("Standard", this))
            .add(new BufferAbuseVelocity("Buffer Abuse", this))
            .add(new DelayVelocity("Delay", this))
            .add(new LegitVelocity("Legit/Jump Reset", this))
            .add(new LegitVelocity("Polar", this))
            .add(new GroundVelocity("Ground", this))
            .add(new IntaveVelocity("Intave", this))
            .add(new MatrixVelocity("Matrix", this))
            .add(new AACVelocity("AAC", this))
            .add(new VulcanVelocity("Vulcan", this))
            .add(new RedeskyVelocity("Redesky", this))
            .add(new TickVelocity("Tick", this))
            .add(new BounceVelocity("Bounce", this))
            .add(new KarhuVelocity("Karhu", this))
            .add(new MMCVelocity("MMC", this))
            .add(new UniversoCraftVelocity("Universocraft", this))
            .add(new WatchdogVelocity("Watchdog", this))
            .add(new GrimReduceVelocity("Grim Reduce", this))
            .add(new GrimVelocity("Grim", this))
            .setDefault("Standard");

    public final BooleanValue onSwing = new BooleanValue("On Swing", this, false);
}
