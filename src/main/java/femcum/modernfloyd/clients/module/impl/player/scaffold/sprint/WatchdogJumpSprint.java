package femcum.modernfloyd.clients.module.impl.player.scaffold.sprint;

import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.util.player.SlotUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.block.BlockAir;

public class WatchdogJumpSprint extends Mode<Scaffold> {

    public WatchdogJumpSprint(String name, Scaffold parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        if (!(PlayerUtil.block(mc.thePlayer.posX, mc.thePlayer.lastGroundY - 2, mc.thePlayer.posY) instanceof BlockAir)) {
            getParent().startY = mc.thePlayer.lastGroundY - 1;
        }
    }

    @EventLink(value = Priorities.LOW)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        boolean start = mc.thePlayer.lastGroundY == getParent().startY;

        if (getComponent(Slot.class).getItemStack() != null &&
                mc.thePlayer.posY > getParent().startY &&
                mc.thePlayer.posY + MoveUtil.predictedMotion(mc.thePlayer.motionY, 3) <
                        getParent().startY + 1) {

            // Getting ItemSlot
            getComponent(Slot.class).setSlot(SlotUtil.findBlock());

            if (getComponent(Slot.class).getItemStack().realStackSize > 0) {
                mc.rightClickMouse();
            }

//            getParent().offset = getParent().offset.add(0, 1, 0);

        }

        if (!start && mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }

        mc.thePlayer.omniSprint = MoveUtil.isMoving();
    };
}
