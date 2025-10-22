package femcum.modernfloyd.clients.module.impl.movement.flight;

import femcum.modernfloyd.clients.component.impl.player.BlinkComponent;
import femcum.modernfloyd.clients.component.impl.player.RotationComponent;
import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.component.impl.player.rotationcomponent.MovementFix;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.event.impl.other.BlockAABBEvent;
import femcum.modernfloyd.clients.module.impl.movement.Flight;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.player.SlotUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;

public class SlimeNCPFlight extends Mode<Flight> {

    private boolean started;

    public SlimeNCPFlight(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        started = false;
        BlinkComponent.blinking = true;
    }

    @Override
    public void onDisable() {
        BlinkComponent.blinking = false;
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (mc.thePlayer.ticksExisted % 10 == 0) {
            BlinkComponent.dispatch();
        }

        if (!started) {
            final int slot = SlotUtil.findBlock(Blocks.slime_block);

            if (slot == -1) {
                return;
            }

            MoveUtil.stop();

            getComponent(Slot.class).setSlot(slot);

            RotationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw, 90), 3, MovementFix.OFF);

            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }

            if (RotationComponent.rotations.y >= 89 &&
                    mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                    mc.thePlayer.posY > mc.objectMouseOver.getBlockPos().add(0, 2, 0).getY()) {

                mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, getComponent(Slot.class).getItemStack(),
                        mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);

                mc.thePlayer.swingItem();

                started = true;
            }
        } else {
            if (mc.thePlayer.motionY > 0) {
                mc.thePlayer.motionY = MoveUtil.predictedMotion(0);
            }

            mc.timer.timerSpeed = 1.4f;
        }
    };

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (!started) {
            event.setForward(0);
            event.setStrafe(0);
        }
    };

    @EventLink
    public final Listener<BlockAABBEvent> onBlockAABB = event -> {
        if (started) {
            // Sets The Bounding Box To The Players Y Position.
            if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

                if (y < mc.thePlayer.posY) {
                    event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
                }
            }
        }
    };
}