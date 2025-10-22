package femcum.modernfloyd.clients.module.impl.combat.antibot;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.AntiBot;
import femcum.modernfloyd.clients.value.Mode;

public final class DuplicateIDCheck extends Mode<AntiBot> {

    public DuplicateIDCheck(String name, AntiBot parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.theWorld.playerEntities.forEach(player -> {
            if (mc.theWorld.playerEntities.stream().anyMatch(player2 -> player2.getEntityId() == player.getEntityId() && player2 != player)) {
                Floyd.INSTANCE.getBotManager().add(this, player);
            }
        });
    };

    @Override
    public void onDisable() {
        Floyd.INSTANCE.getBotManager().clear(this);
    }
}