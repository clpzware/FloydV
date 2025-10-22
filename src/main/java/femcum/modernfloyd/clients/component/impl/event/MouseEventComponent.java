package femcum.modernfloyd.clients.component.impl.event;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.MouseInputEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import org.lwjgl.input.Mouse;

public class MouseEventComponent extends Component {
    int[] inputs = {0, 1, 2, 3, 4, 5};
    boolean[] downs = {false, false, false, false, false, false};

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        for (int input : inputs) {
            if (Mouse.isButtonDown(input)) {
                if (!downs[input]) Floyd.INSTANCE.getEventBus().handle(new MouseInputEvent(input));
                downs[input] = true;
            } else {
                downs[input] = false;
            }
        }
    };
}
