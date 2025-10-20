package fr.ambient.module.impl.player.scaffold.tower;

import fr.ambient.Ambient;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.util.BlockUtil;
import fr.ambient.util.CompleteRotationData;
import fr.ambient.util.PosFace;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.RotationUtil;
import fr.ambient.util.player.movecorrect.MoveCorrect;
import fr.ambient.util.render.model.ESPUtil;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.awt.*;

@Slf4j
public class WatchdogTestTower extends ModuleMode {

    public WatchdogTestTower(String modeName, Module module) {
        super(modeName, module);
    }

    public boolean canTower = false;
    public boolean Tower = false;
    private int tickCounter = 0;

    private double[] basePos = null;
    private double[] goalPos = null;

    private double[] baseXZ = null;
    private boolean isReady = false;


    private int step = 0;



    public void onEnable() {
        canTower = false;
        Tower = false;
    }


    public boolean forcePlace = false;

    public void onDisable() {
        Tower = false;
    }

    private boolean conditions() {
        return mc.gameSettings.keyBindJump.isKeyDown()
                && !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() instanceof BlockStairs)
                && !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() instanceof BlockSlab)
                && canTower && !mc.thePlayer.isCollidedHorizontally;
    }


    @SubscribeEvent
    private void onInputEvent(MoveInputEvent event){
        if(conditions()){
            event.setJumping(false);
            event.setForward(0);
            event.setStrafe(0);
        }
    }


    @SubscribeEvent(EventPriority.LOW)
    private void onRotate(PreMotionEvent event){
        if(conditions()){
            double[] goalXZ = edge(mc.thePlayer.posX, mc.thePlayer.posZ);

            double[] tmpGoal = goalXZ;
            if(goalPos != null){
                tmpGoal = goalPos;
            }

            if(new Vec3(mc.thePlayer.posX,0, mc.thePlayer.posZ).distanceTo(new Vec3(tmpGoal[0],0, tmpGoal[1]) ) > 0.01){
                isReady = false;
                if(!mc.thePlayer.onGround){
                    return;
                }
                if(step == 0){
                    goalPos = goalXZ;
                    basePos = new double[]{mc.thePlayer.posX, mc.thePlayer.posZ};
                    baseXZ = basePos;
                }
                if(goalPos == null || basePos == null){
                    ChatUtil.display("null :(");
                    return;
                }
                double[] ret = step(basePos, goalPos, Math.min(3, step));

                mc.thePlayer.setPosition(ret[0], mc.thePlayer.posY, ret[1]);




                if(step < 3){
                    step++;
                }

                return;
            }else{
                isReady = true;
            }


            Scaffold scaffold = Ambient.getInstance().getModuleManager().getModule(Scaffold.class);

            BlockPos bp = new BlockPos(basePos[0], mc.thePlayer.posY - 1.1, basePos[1]);
            PosFace pf = new PosFace(bp,EnumFacing.UP);


            if(tickCounter == 1){
                float y = 0, p = 0;
                float[] rpta = RotationUtil.getRotationDifference(
                        new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),
                        new Vec3(Math.floor(goalXZ[0]) + 0.5, mc.thePlayer.posY - 1, Math.floor(goalXZ[1]) + 0.5),
                        0,
                        0
                );

                Ambient.getInstance().getRotationComponent().setRotations(y - rpta[0], p -rpta[1], MoveCorrect.SILENT);
                forcePlace = true;
            }


        }
    }

    @SubscribeEvent(EventPriority.LOW)
    private void onMove(UpdateEvent event) {
        if (conditions()) {
            if(isReady){
                if(mc.thePlayer.onGround){
                    tickCounter = 0;
                }


                switch (tickCounter){
                    case 0 -> {
                        mc.thePlayer.motionY = 0.42f;
                    }
                    case 1 -> {
                        mc.thePlayer.motionY = 0.33f;
                    }
                    case 2 -> {
                        mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                    }
                    case 3 -> {
                        mc.thePlayer.motionY = -100;
                    }
                }



                Scaffold scaffold = Ambient.getInstance().getModuleManager().getModule(Scaffold.class);

                if(scaffold.isHasPlacedThisTick()){
                    ChatUtil.display(tickCounter + " / " + forcePlace);
                }




                if(forcePlace && !scaffold.isHasPlacedThisTick()){
                    BlockPos bp = new BlockPos(basePos[0], mc.thePlayer.posY - 2, basePos[1]);
                    PosFace pf = new PosFace(bp,EnumFacing.UP);
                    CompleteRotationData c = BlockUtil.getRTS(pf, new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ));
                    scaffold.setSlotOwO(true);
                    if(c != null){
                        mc.thePlayer.swingItem();
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), c.movingObjectPosition.getBlockPos(), c.movingObjectPosition.sideHit, c.movingObjectPosition.hitVec)) {
                            mc.thePlayer.swingItem();
                            forcePlace = false;
                        }
                    }
                }




                tickCounter++;
            }else {
                tickCounter = 0;
            }



        } else {
            if (!canTower && mc.thePlayer.onGround) canTower = true;
            else if (!mc.thePlayer.onGround) canTower = false;
            step = 0;
            isReady = false;
            goalPos = null;
            if(mc.thePlayer.onGround){
                tickCounter = 0;
            }
        }
    }

    @SubscribeEvent
    private void evenRender3D(Render3DEvent event){
        if(basePos != null){
            BlockPos bp = new BlockPos(basePos[0], mc.thePlayer.posY - 0.95, basePos[1]);
            ESPUtil.point(new Vec3(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5), Color.BLUE, 0.5f);

        }
    }

    public double[] edge(double x, double y) { // rawr
        int bx = (int) Math.floor(x), by = (int) Math.floor(y);
        double dl = x - bx, dr = (bx + 1) - x, db = y - by, dt = (by + 1) - y;
        double min = Math.min(Math.min(dl, dr), Math.min(db, dt));
        double offset = 0.05;

        return (min == dl) ? new double[]{bx - offset, y} :
                (min == dr) ? new double[]{bx + 1 + offset, y} :
                        (min == db) ? new double[]{x, by - offset} :
                                new double[]{x, by + 1 + offset};
    }
    public double[] step(double[] current, double[] goal, int step) {
        double factor = step / 3.0;

        double[] ret = new double[2];
        ret[0] = current[0] + (goal[0] - current[0]) * factor;
        ret[1] = current[1] + (goal[1] - current[1]) * factor;

        return ret;
    }

}
