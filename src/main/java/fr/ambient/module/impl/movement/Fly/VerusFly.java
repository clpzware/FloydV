package fr.ambient.module.impl.movement.Fly;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.world.BoundingBoxEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Flight;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.AxisAlignedBB;

public class VerusFly extends ModuleMode {
    private final Flight fly = (Flight) this.getSuperModule();
    private final TimeUtil verusTime = new TimeUtil();
    private boolean shouldStop;
    private boolean damaged = false;

    public VerusFly(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onEnable() {
        if (fly.verusmode.is("Damage")) {
            verusTime.reset();
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }
            mc.timer.timerSpeed = 0.5f;
            PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0, 0.5f, 0));
            PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.5, mc.thePlayer.posZ), 1, new ItemStack(Blocks.stone.getItem(mc.theWorld, new BlockPos(-1, -1, -1))), 0, 0.94f, 0));
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.001, mc.thePlayer.posZ, false));
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
            damaged = false;
        }
    }

    @Override
    public void onDisable() {
        verusTime.reset();
        MoveUtil.strafe(0);
        mc.timer.timerSpeed = 1f;
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent ignoredEvent) {
        switch (fly.verusmode.getValue()) {
            case "AirWalk" -> {
                PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0, 0.5f, 0));

                mc.thePlayer.motionY = mc.gameSettings.keyBindJump.isKeyDown() ? 0.5 : mc.gameSettings.keyBindSneak.isKeyDown() ? -0.5 : mc.thePlayer.onGround ? 0.00001f : mc.thePlayer.motionY;

                if (!mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    if (mc.thePlayer.motionY < 0.1) {
                        MoveUtil.strafe(MoveUtil.getVerusLimit(false));
                    } else {
                        verusTime.reset();
                        shouldStop = true;
                    }
                } else {
                    MoveUtil.strafe(MoveUtil.getBaseMoveSpeed());
                }

                if (shouldStop && !verusTime.finished(400)) {
                    mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                }
            }
            case "Damage" -> {
                if (mc.thePlayer.hurtTime > 1) {
                    damaged = true;
                }
                if (damaged) {
                    mc.timer.timerSpeed = 1.0f;
                    if (!verusTime.finished(1100)) {
                        MoveUtil.strafe(3);
                    } else {
                        MoveUtil.strafe(mc.gameSettings.keyBindSneak.isKeyDown() ? 0.25f : 0.36f);
                    }

                    PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0, 0.5f, 0));

                    mc.thePlayer.motionY = mc.thePlayer.movementInput.jump ? 0.5 : mc.thePlayer.movementInput.sneak ? -0.5 : 0;

                } else {
                    verusTime.reset();
                }
            }
            case "Vanilla" -> {
                PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0, 0, 0));

                mc.thePlayer.motionY = mc.gameSettings.keyBindJump.pressed ? 0.2 : mc.gameSettings.keyBindSneak.pressed ? -0.2 : 0;

                if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
                    MoveUtil.strafe(0.32f, 0.4f, 0.43f);
                } else {
                    MoveUtil.strafe(0);
                }
            }
        }
    }

    @SubscribeEvent
    public void onBound(BoundingBoxEvent event) {
        if (fly.verusmode.is("Airwalk")) {
            AxisAlignedBB axisAlignedBB = AxisAlignedBB.fromBounds(-5, -1, -5, 5, 1, 5).offset(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ());
            if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                event.setBoundingBox(axisAlignedBB);
            }
        }
    }
}
