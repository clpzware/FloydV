package fr.ambient.module.impl.combat.velocity;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.ArrayList;


public class GhostBlockVelocity extends ModuleMode {
    public GhostBlockVelocity(String modeName, Module module) {
        super(modeName, module);
    }

    public ArrayList<BlockPos> positions = new ArrayList<>();

    @SubscribeEvent()
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity s12PacketEntityVelocity) {
            if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCancelled(false);

                if(!mc.thePlayer.onGround){
                    return;
                }

                BlockPos pos = null;

                double divisor = 32000D;
                while (divisor > 8000D){
                    double addX = (s12PacketEntityVelocity.getMotionX() / divisor); // 2tick prediction
                    double addZ = (s12PacketEntityVelocity.getMotionZ() / divisor); // 2tick prediction

                    BlockPos pp = new BlockPos(mc.thePlayer.posX +addX, mc.thePlayer.posY, mc.thePlayer.posZ + addZ);

                    ChatUtil.display(mc.thePlayer.getEntityBoundingBox().intersectsWith(new AxisAlignedBB(
                            pp.getX(),pp.getY(),pp.getZ(),pp.getX() + 1, pp.getY() + 1, pp.getZ() + 1
                    )));

                    if(!(mc.theWorld.getBlockState(pp.down()).getBlock() instanceof BlockAir)) {
                        pos = pp;
                        break;
                    }



                    divisor -= 8000D;
                }

                if(pos != null){
                    ChatUtil.display("Check");
                    PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(pos.down(), 1, new ItemStack(Item.getItemFromBlock(Blocks.coal_block)), 0,0,0));
                    PacketUtil.sendPacket(new C0APacketAnimation());
                    mc.theWorld.setBlockState(pos, Blocks.coal_block.getDefaultState());
                    positions.add(pos);
                }else{
                    ChatUtil.display("Nope");
                }
            }
        }
    }

    @SubscribeEvent
    private void onTickEvent(UpdateEvent event){
        if(!positions.isEmpty()){
            if(mc.thePlayer.groundTicks > 10){
                positions.forEach(b->mc.theWorld.setBlockToAir(b));
                positions.clear();
            }else{
                positions.forEach(b-> mc.theWorld.setBlockState(b, Blocks.coal_block.getDefaultState()));
            }

        }



    }
}