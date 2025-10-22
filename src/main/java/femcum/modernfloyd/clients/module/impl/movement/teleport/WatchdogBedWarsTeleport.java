package femcum.modernfloyd.clients.module.impl.movement.teleport;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.Teleport;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.util.Vec3;

public final class WatchdogBedWarsTeleport extends Mode<Teleport> {

    public WatchdogBedWarsTeleport(String name, Teleport parent) {
        super(name, parent);
    }

    public Vec3 position = new Vec3(0, 0, 0);

    @Override
    public void onEnable() {
        position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        ChatUtil.display("Die -> Fly to where you want to teleport -> Toggle");
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        event.setPosY(position.yCoord + 3);
        event.setPosX(position.xCoord);
        event.setPosZ(position.zCoord);
        event.setOnGround(false);
    };
}
