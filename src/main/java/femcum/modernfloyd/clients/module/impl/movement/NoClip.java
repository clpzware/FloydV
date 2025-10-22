package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.component.impl.player.RotationComponent;
import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.component.impl.player.rotationcomponent.MovementFix;
import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.motion.PushOutOfBlockEvent;
import femcum.modernfloyd.clients.event.impl.other.BlockAABBEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.util.player.SlotUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import net.minecraft.block.BlockAir;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(aliases = {"module.movement.noclip.name"}, description = "module.movement.noclip.description", category = Category.MOVEMENT)
public class NoClip extends Module {

    private final BooleanValue block = new BooleanValue("Block", this, false);

    @Override
    public void onDisable() {
        mc.thePlayer.noClip = false;
    }

    @EventLink
    public final Listener<BlockAABBEvent> onBlockAABB = event -> {
        if (PlayerUtil.insideBlock()) {
            event.setBoundingBox(null);

            // Sets The Bounding Box To The Players Y Position.
            if (!(event.getBlock() instanceof BlockAir) && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

                if (y < mc.thePlayer.posY) {
                    event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
                }
            }
        }
    };

    @EventLink
    public final Listener<PushOutOfBlockEvent> onPushOutOfBlock = CancellableEvent::setCancelled;

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        mc.thePlayer.noClip = true;

        if (block.getValue()) {
            final int slot = SlotUtil.findBlock();

            if (slot == -1 || PlayerUtil.insideBlock()) {
                return;
            }

            getComponent(Slot.class).setSlot(slot);

            RotationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw, 90), 2 + Math.random(), MovementFix.NORMAL);

            if (RotationComponent.rotations.y >= 89 &&
                    mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                    mc.thePlayer.posY == mc.objectMouseOver.getBlockPos().up().getY()) {

                mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, getComponent(Slot.class).getItemStack(),
                        mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);

                mc.thePlayer.swingItem();
            }
        }
    };
}