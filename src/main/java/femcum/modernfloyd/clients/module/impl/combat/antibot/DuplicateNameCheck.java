package femcum.modernfloyd.clients.module.impl.combat.antibot;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.AntiBot;
import femcum.modernfloyd.clients.value.Mode;

public final class DuplicateNameCheck extends Mode<AntiBot> {

    public DuplicateNameCheck(String name, AntiBot parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.theWorld.playerEntities.forEach(player -> {
            String name = player.getDisplayName().getUnformattedText();

            if (mc.theWorld.playerEntities.stream().anyMatch(player2 -> name.equals(player2.getDisplayName().getUnformattedText()))) {
                Floyd.INSTANCE.getBotManager().add(this, player);
            }
        });
    };

    @Override
    public void onDisable() {
        Floyd.INSTANCE.getBotManager().clear(this);
    }
}