package fr.ambient.module.impl.movement.longjump;

import fr.ambient.Ambient;
import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.LongJump;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class WatchdogBowLongJump extends ModuleMode {


    public WatchdogBowLongJump(String modeName, Module module) {
        super(modeName, module);
    }


    public int bowTicks = 0;

    public int bowSlot = -1;

    public void onEnable(){
        int bbs = getBowItem();

        if(bbs != -1){
            bowSlot = bbs;
        }else{
            Ambient.getInstance().getModuleManager().getModule(LongJump.class).setEnabled(false);
            return;
        }
        mc.thePlayer.inventory.currentItem = bowSlot;
        bowTicks = 0;
        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
    }


    @SubscribeEvent
    private void onNetworkTick(PreMotionEvent e){
        if(bowTicks <= 5){
            e.setPitch(-90);
            if(bowTicks == 5){
                PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                BlinkComponent.onEnable();
            }
        }



        if(bowTicks == 12){
            mc.thePlayer.jump();
        }
        if(bowTicks > 16 && mc.thePlayer.motionY < 0 && mc.thePlayer.hurtTime != 0){
            BlinkComponent.onDisable();
            Ambient.getInstance().getModuleManager().getModule(LongJump.class).setEnabled(false);
        }
        if(mc.thePlayer.hurtTime == 9){
            MoveUtil.strafe(0.6f);
        }

        bowTicks++;
    }

    @SubscribeEvent
    private void onMoveTick(MovementEvent e){
        if(bowTicks <= 10){
            e.setX(0);
            e.setZ(0);
        }

    }


    public void onDisable(){
        BlinkComponent.onDisable();

    }
    public int getBowItem() {
        int place = -1;
        int stackSize = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBow) {
                if (stackSize < stack.stackSize) {
                    place = i;
                    stackSize = stack.stackSize;
                }
            }
        }
        return place;
    }

}
