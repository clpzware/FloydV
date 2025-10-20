package fr.ambient.module.impl.movement.Fly;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.BlockUtil;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class HypixelPredMovementDisabler extends ModuleMode {
    public HypixelPredMovementDisabler(String modeName, Module module) {
        super(modeName, module);
    }

    private Vec3 startPos = null;
    private boolean hasBeenHit = false;


    @Override
    public void onEnable(){
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.07, mc.thePlayer.posZ, false));
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.07, mc.thePlayer.posZ, true));
        startPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        ChatUtil.display("Hypixel Pred Movement Disabler 2025-2025 </3");
        ChatUtil.display("Fly 10 block away from where u are currently and it's done!");
    }

    @Override
    public void onDisable() {
        MoveUtil.strafe(0);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    @SubscribeEvent
    public void onTick(UpdateEvent event) {
        mc.thePlayer.motionY = mc.gameSettings.keyBindJump.pressed ? 1.1f : mc.gameSettings.keyBindSneak.pressed ? 1.1f : 0;
        if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
            MoveUtil.strafe(1);
        } else {
            MoveUtil.strafe(0);
        }
    }

    @SubscribeEvent
    private void recvPacket(PacketReceiveEvent event){
        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.ticksExisted < 5) {
            return;
        }

        if (event.getPacket() instanceof S08PacketPlayerPosLook s08PacketPlayerPosLook) {
            BlockPos pos = BlockUtil.getNearestBlock(8);


            if(pos == null && hasBeenHit){
                ChatUtil.display("ICE got your ass on god");
                hasBeenHit = false;
            }
            if(pos == null){
                pos = new BlockPos(mc.thePlayer);
            }

            double y = hasBeenHit ? pos.getY() : s08PacketPlayerPosLook.getY();
            double x = hasBeenHit ? pos.getX() : s08PacketPlayerPosLook.getX();
            double z = hasBeenHit ? pos.getZ() : s08PacketPlayerPosLook.getZ();

            PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, s08PacketPlayerPosLook.getYaw(), s08PacketPlayerPosLook.getPitch(), true));
            hasBeenHit = false;
            event.setCancelled(true);
        }
    }
    @SubscribeEvent
    private void playerTickEvent(PreMotionEvent event){
        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.ticksExisted < 5) {
            return;
        }

        if (mc.thePlayer.ticksExisted % 5 == 0) {
            return;
        }
        if(startPos != null && startPos.distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) >= 10){
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.07, mc.thePlayer.posZ, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.07, mc.thePlayer.posZ, true));
            ChatUtil.display("Disabled The Anti-cheat");
            startPos = null;
        }

        mc.thePlayer.motionY = 0;
        event.setCancelled(true);

        PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(event.getPosX(), event.getPosY(), event.getPosZ(), event.getYaw(), event.getPitch(), false));
    }
}
