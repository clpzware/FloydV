package femcum.modernfloyd.clients.module.impl.combat.antibot;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.bots.BotManager;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.AntiBot;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class MiddleClickBot extends Mode<AntiBot> {

    private boolean down;

    public MiddleClickBot(String name, AntiBot parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (Mouse.isButtonDown(2) || (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && mc.gameSettings.keyBindAttack.isKeyDown())) {
            if (down) return;
            down = true;

            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                BotManager botManager = Floyd.INSTANCE.getBotManager();
                Entity entity = mc.objectMouseOver.entityHit;

                if (botManager.contains(this, entity)) {
                    Floyd.INSTANCE.getBotManager().remove(this, entity);
                } else {
                    Floyd.INSTANCE.getBotManager().add(this, entity);
                }
            }
        } else down = false;
    };

    @Override
    public void onDisable() {
        Floyd.INSTANCE.getBotManager().clear(this);
    }
}