package femcum.modernfloyd.clients.module.impl.render.fullbright;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.TickEvent;
import femcum.modernfloyd.clients.module.impl.render.FullBright;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public final class EffectFullBright extends Mode<FullBright> {

    public EffectFullBright(String name, FullBright parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<TickEvent> onTick = event -> {
        mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, Integer.MAX_VALUE, 1));
    };

    @Override
    public void onDisable() {
        if (mc.thePlayer.isPotionActive(Potion.nightVision)) {
            mc.thePlayer.removePotionEffect(Potion.nightVision.id);
        }
    }
}