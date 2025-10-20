package fr.ambient.module.impl.player;

import fr.ambient.Ambient;
import fr.ambient.component.impl.misc.BreakerWhitelistComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.player.RotationUtil;
import fr.ambient.util.player.movecorrect.MoveCorrect;
import fr.ambient.util.render.model.ESPUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.ArrayList;

import static fr.ambient.util.BlockUtil.blockBlacklist;

public class AutoBed extends Module {
    public AutoBed() {
        super(113, ModuleCategory.PLAYER);
        this.registerProperties(placementDistance,whitelistOnly,showPlace);
    }


    private BooleanProperty whitelistOnly = BooleanProperty.newInstance("Whitelist Only", true);
    private BooleanProperty showPlace = BooleanProperty.newInstance("Show Placement", true);
    private NumberProperty placementDistance = NumberProperty.newInstance("Placement Distance", 3f, 4.5f, 6f, 0.1f);


    private BlockPos protectBedBlock = null;
    private BlockPos currentProtBedBlock = null;
    private BlockPos nextToPlace = null;


    private ArrayList<BlockPos> toPlace = new ArrayList<>();

    private int oldSlot = -1;


    public void onDisable(){
        disable();
    }


    private void disable(){
        if(oldSlot != -1){
            mc.thePlayer.inventory.currentItem = oldSlot;
            oldSlot = -1;
        }
        toPlace.clear();
        Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
        nextToPlace = null;
        protectBedBlock = null;
        currentProtBedBlock = null;
    }


    @SubscribeEvent
    private void onTick(PreMotionEvent event){
        protectBedBlock = findBed(placementDistance.getValue());
        nextToPlace = null; // set null for prec

        if(protectBedBlock == null){
            disable();
            return;
        }

        BlockPos[] otherBedBlock = new BlockPos[]{
                protectBedBlock.north(),
                protectBedBlock.south(),
                protectBedBlock.west(),
                protectBedBlock.east(),
        };


        BlockPos[] toCheck = new BlockPos[]{
                protectBedBlock.up(),
                protectBedBlock.east(),
                protectBedBlock.west(),
                protectBedBlock.north(),
                protectBedBlock.south()
        };

        boolean shouldCheckOtherBedBlock = true;
        for(BlockPos p32 : toCheck){
            if((mc.theWorld.getBlockState(p32).getBlock() instanceof BlockAir)){
                shouldCheckOtherBedBlock = false;
            }
        }
        currentProtBedBlock = protectBedBlock;
        if(shouldCheckOtherBedBlock){
            BlockPos otherBedPos = null;
            for(BlockPos pos : otherBedBlock){
                if(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed){
                    otherBedPos = pos;
                    break;
                }
            }
            if(otherBedPos != null){


                currentProtBedBlock = otherBedPos;
                toCheck = new BlockPos[]{
                        otherBedPos.up(),
                        otherBedPos.east(),
                        otherBedPos.west(),
                        otherBedPos.north(),
                        otherBedPos.south()
                };
            }


        }


        toPlace.clear();
        for(BlockPos pos : toCheck){
            if((mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)){
                if(mc.thePlayer.getDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= placementDistance.getValue()){
                    nextToPlace = pos;
                }
                toPlace.add(pos);
            }
        }

        if(nextToPlace == null){
            disable();
            return;
        }
        if(oldSlot == -1){
            oldSlot = mc.thePlayer.inventory.currentItem;
        }
        if(!setSlot()){
            disable();
            return;
        }

        float[] rotations = RotationUtil.getRotationDifference(
                new Vec3(mc.thePlayer.posX,
                        mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                        mc.thePlayer.posZ),
                new Vec3(nextToPlace.getX() + 0.5f,
                        nextToPlace.getY(),
                        nextToPlace.getZ() + 0.5f),
                0, 0);

        Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
        Ambient.getInstance().getRotationComponent().setRotations(0 - rotations[0], 0 - rotations[1], MoveCorrect.SILENT);



    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent event){
        if(showPlace.getValue()){
            for(BlockPos pos : toPlace){
                ESPUtil.point(
                        new Vec3(
                        pos.getX() + 0.5f,
                        pos.getY() + 0.5f,
                        pos.getZ() + 0.5f),
                        Ambient.getInstance().getHud().getCurrentTheme().color1,
                        0.05f
                );
            }
        }
    }

    @SubscribeEvent
    private void onTickEvent(UpdateEvent event){
        if(nextToPlace != null){

            if(currentProtBedBlock.up().equals(nextToPlace) && !mc.thePlayer.isSneaking()){
                return;
            }

            if(mc.playerController.onPlayerRightClick(mc.thePlayer,
                    mc.theWorld,
                    mc.thePlayer.inventory.getCurrentItem(),
                    nextToPlace.down(),
                    EnumFacing.UP,
                    new Vec3(nextToPlace.getX() + Math.random(),
                            nextToPlace.getY(),
                            nextToPlace.getZ() + Math.random()))){
                mc.thePlayer.swingItem();
            }
        }
    }

    @SubscribeEvent
    private void onPlayerInput(MoveInputEvent event){
        if(currentProtBedBlock != null && nextToPlace != null){
            if(currentProtBedBlock.up().equals(nextToPlace)){
                event.setSneaking(true);
            }
        }
    }




    private BlockPos findBed(float distance) {
        float bedDist = 69420;
        BlockPos bedPos = null;

        for (float x = -distance; x < distance; x++) {
            for (float z = -distance; z < distance; z++) {
                for (float y = -distance; y < distance; y++) {
                    BlockPos cPos = new BlockPos(x + mc.thePlayer.posX, y + mc.thePlayer.posY, z + mc.thePlayer.posZ);
                    if (mc.theWorld.getBlockState(cPos).getBlock() instanceof BlockBed) {

                        if(!BreakerWhitelistComponent.isWhitelisted(cPos) && whitelistOnly.getValue()){
                            continue;
                        }

                        double bcd = mc.thePlayer.getDistance(cPos.getX() + 0.5, cPos.getY() + 0.5, cPos.getZ() + 0.5);
                        if (bcd < bedDist) {
                            bedDist = (float) bcd;
                            bedPos = cPos;
                        }
                    }
                }
            }
        }

        return bedPos;
    }
    public boolean setSlot(){
        int tempslot = getBlockItem();

        if(tempslot != -1){
            mc.thePlayer.inventory.currentItem = tempslot;
            mc.playerController.syncCurrentPlayItem();
            return true;
        }
        return false;
    }
    public int getBlockItem() {
        int place = -1;
        int stackSize = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock && isItemStackAllowed(stack)) {
                if (stackSize < stack.stackSize) {
                    place = i;
                    stackSize = stack.stackSize;
                }
            }
        }

        return place;
    }
    public boolean isItemStackAllowed(ItemStack stack) {
        for(Block block : blockBlacklist){
            if(stack.getItem() == Item.getItemFromBlock(block)){
                return false;
            }
        }
        return true;
    }



}
