package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

public class VerusSpeed extends ModuleMode {

    public VerusSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        Speed speedModule = (Speed) this.getSuperModule();

        switch (speedModule.verusmode.getValue()) {
            case "Ground" -> {
                if (mc.gameSettings.keyBindJump.isKeyDown()) return;

                PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0, 0.5f, 0));

                if (MoveUtil.moving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.00001f;
                    }
                } else {
                    mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                }

                MoveUtil.strafe(MoveUtil.getVerusLimit(true));

                if (mc.thePlayer.fallDistance > 0.2) {
                    mc.thePlayer.motionY = -0.1f;
                }
            }
            case "Ground 2" -> {
                PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0, 0.5f, 0));
                if (mc.gameSettings.keyBindJump.isKeyDown()) return;
                var ticks = mc.thePlayer.ticksExisted % 9;
                if (ticks == 0) {
                    MoveUtil.strafe(0.37f, 0.7f, 0.75f);
                    event.setOnGround(false);

                } else {
                    MoveUtil.strafe(ticks * 0.05f, ticks * 0.07f, ticks * 0.07f);
                }
            }
            case "Low" -> {
                PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0, 0.5f, 0));
                if (mc.thePlayer.onGround && MoveUtil.moving()) {
                    mc.thePlayer.motionY = 0.42;
                    MoveUtil.strafe(0.48f, 0.52f, 0.6f);
                }
                if (mc.thePlayer.airTicks == 1) {
                    mc.thePlayer.motionY = -0.15233518685055714;
                }
            }
        }
    }


    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMovementInputEvent(MoveInputEvent event) {
        Speed speedModule = (Speed) this.getSuperModule();
        if (speedModule.verusmode.is("Low")) {
            event.setJumping(false);
        }
    }


    @Override
    public void onDisable() {
        MoveUtil.strafe(0.2);
    }
}