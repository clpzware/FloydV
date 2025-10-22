package femcum.modernfloyd.clients.module.impl.combat.antibot;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.AntiBot;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.client.network.NetworkPlayerInfo;

public final class PingCheck extends Mode<AntiBot> {

    public PingCheck(String name, AntiBot parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.theWorld.playerEntities.forEach(player -> {
            final NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(player.getUniqueID());

            if (info != null && info.getResponseTime() < 0) {
                Floyd.INSTANCE.getBotManager().add(this, player);
            }
        });
    };

    @Override
    public void onDisable() {
        Floyd.INSTANCE.getBotManager().clear(this);
    }
}