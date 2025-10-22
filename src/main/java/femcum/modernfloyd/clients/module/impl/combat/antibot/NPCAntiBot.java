package femcum.modernfloyd.clients.module.impl.combat.antibot;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.AntiBot;
import femcum.modernfloyd.clients.value.Mode;

public final class NPCAntiBot extends Mode<AntiBot> {

    public NPCAntiBot(String name, AntiBot parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.theWorld.playerEntities.forEach(player -> {
            if (player.moved) {
                Floyd.INSTANCE.getBotManager().remove(this, player);
            } else {
                Floyd.INSTANCE.getBotManager().add(this, player);
            }
        });
    };

    @Override
    public void onDisable() {
        Floyd.INSTANCE.getBotManager().clear(this);
    }
}