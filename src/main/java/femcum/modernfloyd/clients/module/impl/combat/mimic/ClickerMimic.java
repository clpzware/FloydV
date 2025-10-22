package femcum.modernfloyd.clients.module.impl.combat.mimic;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.module.impl.combat.Mimic;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class ClickerMimic extends Mode<Mimic> {

    public ClickerMimic(String name, Mimic parent) {
        super(name, parent);
    }

    private long next;
    private HashMap<String, Tuple<ArrayList<Integer>, Integer>> clicks = new HashMap<>();

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PacketReceiveEvent> onReceive = event -> {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S0BPacketAnimation) {
            S0BPacketAnimation animation = ((S0BPacketAnimation) packet);

            int id = animation.getEntityID();
            if (id == mc.thePlayer.getEntityId()) return;

            String name = mc.theWorld.getEntityByID(id).getCommandSenderName();
            int tick = mc.thePlayer.ticksExisted;

            if (!clicks.containsKey(name)) {
                clicks.put(name, new Tuple<>(new ArrayList<>(), tick));
            }

            Tuple<ArrayList<Integer>, Integer> data = clicks.get(name);

            ChatUtil.display("Recorded " + (tick - data.getSecond()) + " von " + name);
            data.getFirst().add(tick - data.getSecond());
            data.setSecond(tick);
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) return;

        if (mc.gameSettings.keyBindSneak.isKeyDown()) clicks = new HashMap<>();

        if (System.currentTimeMillis() > next) {
            long next = 0;

            while (next == 0 || next >= 9 * 50) {
                if (next != 0) ChatUtil.display("Running again prev " + next);
                Optional<String> first = clicks.keySet().stream().findFirst();

                if (!first.isPresent()) {
                    ChatUtil.display("Empty");
                    return;
                }
                Tuple<ArrayList<Integer>, Integer> data = clicks.get(first.get());

                if (data.getFirst().isEmpty()) {
                    clicks.remove(first.get());
                    return;
                }

                next = data.getFirst().get(0) * 50;
                data.getFirst().remove(0);

                ChatUtil.display("Running " + first.get() + " " + next);
            }

            this.next = System.currentTimeMillis() + next / 2L;

            mc.clickMouse();
            mc.leftClickCounter = 1;
        }
    };
}
