package femcum.modernfloyd.clients.component.impl.patches;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class GuiClosePatchComponent extends Component {

    private boolean inGUI;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.currentScreen == null && inGUI) {
            for (final KeyBinding bind : mc.gameSettings.keyBindings) {
                bind.setPressed(GameSettings.isKeyDown(bind));
            }
        }

        inGUI = mc.currentScreen != null;
    };
}
