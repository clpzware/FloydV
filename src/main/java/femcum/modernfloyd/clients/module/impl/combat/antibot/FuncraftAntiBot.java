package femcum.modernfloyd.clients.module.impl.combat.antibot;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.module.impl.combat.AntiBot;
import femcum.modernfloyd.clients.value.Mode;

public final class FuncraftAntiBot extends Mode<AntiBot> {
    public FuncraftAntiBot(String name, AntiBot parent) {
        super(name, parent);
    }

    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        mc.theWorld.playerEntities.forEach(player -> {
            if (player.getDisplayName().getUnformattedText().contains("§")) {
                Floyd.INSTANCE.getBotManager().remove(this, player);
                return;
            }

            Floyd.INSTANCE.getBotManager().add(this, player);
        });
    };

    @Override
    public void onDisable() {
        Floyd.INSTANCE.getBotManager().clear(this);
    }
}