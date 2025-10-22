package femcum.modernfloyd.clients.module.impl.player.scaffold.sprint;

import femcum.modernfloyd.clients.component.impl.player.RotationComponent;
import femcum.modernfloyd.clients.component.impl.player.rotationcomponent.MovementFix;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.util.vector.Vector3d;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.EnumFacing;

public class WatchdogLimitSprint extends Mode<Scaffold> {

    private int ticks;

    public WatchdogLimitSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink(value = Priorities.HIGH)
    private final Listener<PreUpdateEvent> preMotionEventListener = event -> {
        RotationComponent.setSmoothed(false);

        if (ticks > 1 && !mc.gameSettings.keyBindJump.isKeyDown()) {
            getParent().offset = getParent().offset.add(0, -1, 0);
        }

        RotationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw - 180 - 45, 88), 10, MovementFix.NORMAL);

        mc.gameSettings.keyBindSprint.setPressed(false);
    };

    @EventLink
    public final Listener<PacketSendEvent> eventListener = event -> {
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) event.getPacket();
            if (!packet.getPosition().equalsVector(new Vector3d(-1, -1, -1)) && EnumFacing.UP.getIndex() != packet.getPlacedBlockDirection()) {
                if (packet.getPosition().getY() < mc.thePlayer.posY - 1) {
                    ticks = 0;
                } else {
                    ticks++;
                }
            }
        }
    };

    @Override
    public void onEnable() {
        ticks++;
    }
}