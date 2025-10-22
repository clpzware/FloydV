package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;

@ModuleInfo(aliases = {"module.render.particles.name"}, description = "module.render.particles.description", category = Category.RENDER)
public final class Particles extends Module {

    private final NumberValue multiplier = new NumberValue("Multiplier", this, 1, 1, 10, 1);
    private final BooleanValue alwaysCrit = new BooleanValue("Always Crit", this, true);

    private final BooleanValue alwaysSharpness = new BooleanValue("Always Sharpness", this, true);

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        Entity target = event.getTarget();

        if (mc.thePlayer.fallDistance > 0 || alwaysCrit.getValue() || alwaysSharpness.getValue()) {
            for (int i = 0; i <= multiplier.getValue().intValue(); i++) {
                if (this.alwaysCrit.getValue()) {
                    mc.thePlayer.onCriticalHit(target);
                }

                if (this.alwaysSharpness.getValue()) {
                    this.mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.CRIT_MAGIC);
                }
            }
        }
    };
}