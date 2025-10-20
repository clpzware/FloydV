package fr.ambient.module.impl.player;

import fr.ambient.Ambient;
import fr.ambient.component.impl.ui.ItemRenderComponent;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.AnchorPoint;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.player.scaffold.sprint.*;
import fr.ambient.module.impl.player.scaffold.tower.*;
import fr.ambient.property.Property;
import fr.ambient.property.impl.*;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;
import fr.ambient.util.BlockUtil;
import fr.ambient.util.CompleteRotationData;
import fr.ambient.util.FastNoiseLite;
import fr.ambient.util.PosFace;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.player.RotationUtil;
import fr.ambient.util.player.movecorrect.MoveCorrect;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import fr.ambient.util.render.model.ESPUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;

import java.awt.*;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static fr.ambient.util.BlockUtil.blockBlacklist;


public class Scaffold extends Module {

    public Scaffold() {
        super(37, "Automatically places blocks below you as you move.", ModuleCategory.PLAYER);
        registerProperties(mode,moveCorrectMode, rotationGroup, slotGroup, blockSearchGroup, placeGroup,sprintGroup, jump, sneakGroup, jumpGroup, towerGroup, renderingGroup);
        this.moduleModeProperties.add(sprintMMP); 
        this.moduleModeProperties.add(towerMMP);

        this.setDraggable(true);
        this.setAnchorPoint(AnchorPoint.CENTER);
        this.setX(500);
        this.setY(250);
        this.setWidth(50);
        this.setHeight(24);
    }
    @Getter
    @Setter
    private boolean hasPlacedThisTick = false;
    @Getter
    @Setter
    private boolean mightPlaceThisTurn = false;


    private ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Normal", "Telly"}, "Normal");

    private ModeProperty moveCorrectMode = ModeProperty.newInstance("MoveFix Mode", new String[]{"None", "Silent", "Strict", "NoSprint"}, "None");

    // rotation group

    private ModeProperty rotations = ModeProperty.newInstance("Rotations", new String[]{"Normal","RayTrace", "GodBridge", "Edge","Edge2","Edge3","Edge4","None"}, "RayTrace");

    private NumberProperty rotationRaytraceDivisor = NumberProperty.newInstance("RayTrace Divisor", 1f, 45f, 90f, 1f, ()->rotations.is("RayTrace"));
    private BooleanProperty directRotationWhileInAir = BooleanProperty.newInstance("Closest rotations in air", false, ()->rotations.is("RayTrace"));
    private BooleanProperty rotateWhenNoPlace = BooleanProperty.newInstance("Rotate on NoPl", false, ()->rotations.is("Edge") || rotations.is("Edge2"));

    // rotation -> rotation limiter

    private NumberProperty rotationLimiterYawMax = NumberProperty.newInstance("Yaw Max", 0f, 30f, 180f, 1f);
    private NumberProperty rotationLimiterYawMin = NumberProperty.newInstance("Yaw Min", 0f, 30f, 180f, 1f);
    private NumberProperty rotationLimiterPitchMax = NumberProperty.newInstance("Pitch Max", 0f, 20f, 90f, 1f);
    private NumberProperty rotationLimiterPitchMin = NumberProperty.newInstance("Pitch Min", 0f, 20f, 90f, 1f);

    private CompositeProperty rotationLimiter = CompositeProperty.newInstance("Rotation Limiter", new Property[]{rotationLimiterYawMax, rotationLimiterYawMin, rotationLimiterPitchMax, rotationLimiterPitchMin});

    // rotation -> pitch max

    private NumberProperty rotationMaxPitch = NumberProperty.newInstance("Max Pitch", 70f, 86f, 90f, .5f);
    private NumberProperty rotationMaxPitchDiag = NumberProperty.newInstance("Max Pitch in Diag", 70f, 86f, 90f, .5f);
    private NumberProperty rotationMaxPitchAir = NumberProperty.newInstance("Max Pitch in Air", 70f, 86f, 90f, .5f);

    private CompositeProperty rotationPitchMax = CompositeProperty.newInstance("Rotation Pitch Limiter", new Property[]{rotationMaxPitch, rotationMaxPitchDiag, rotationMaxPitchAir});

    // rotation -> preRotation

    private BooleanProperty preRotate = BooleanProperty.newInstance("Pre Rotate", false);
    private NumberProperty preRotatePitch = NumberProperty.newInstance("Pitch", 70f, 80f, 90f, .5f, ()->preRotate.getValue());

    private CompositeProperty rotationPreRot = CompositeProperty.newInstance("Rotation Pre Rotate", new Property[]{preRotate, preRotatePitch});

    // rotation -> rotationOffset

    private NumberProperty rotationOffsetNormal = NumberProperty.newInstance("Normal Offset", -180f, 0f, 180f, 5f);
    private NumberProperty rotationOffsetDiag = NumberProperty.newInstance("Diagonal Offset", -180f, 0f, 180f, 5f);
    private NumberProperty rotationOffsetPreRotate = NumberProperty.newInstance("PreRotate Offset", -180f, 0f, 180f, 5f, ()->preRotate.getValue());

    private CompositeProperty rotationOffset = CompositeProperty.newInstance("Rotation Offset", new Property[]{rotationOffsetNormal, rotationOffsetDiag,rotationOffsetPreRotate});

    // rotation -> rotationPrediction

    private NumberProperty rotationPredictionTick = NumberProperty.newInstance("Prediction Ticks", 1f, 3f, 10f, 1f);
    private CompositeProperty rotationPrediction = CompositeProperty.newInstance("Rotation Prediction", new Property[]{rotationPredictionTick});

    // rotation base

    private BooleanProperty snapScaffold = BooleanProperty.newInstance("Snap", false);
    private CompositeProperty rotationGroup = CompositeProperty.newInstance("Rotations", new Property[]{rotations, rotationRaytraceDivisor,directRotationWhileInAir,rotateWhenNoPlace,rotationLimiter, rotationPitchMax, rotationPreRot, rotationOffset, rotationPrediction,snapScaffold});

    // slot switching

    private ModeProperty slotMode = ModeProperty.newInstance("Slot Mode", new String[]{"Switch", "Spoof", "FullSpoof"}, "Switch");
    private ModeProperty slotGetterMode = ModeProperty.newInstance("Slot Switcher", new String[]{"Max", "EndStack"}, "EndStack");
    private MultiProperty slotChangerMode = MultiProperty.newInstance("Slot Change", new String[]{"On Enable", "On Tick", "After Tick"}, Set.of("On Enable", "On Tick"));
    private CompositeProperty slotGroup = CompositeProperty.newInstance("Slots", new Property[]{slotMode, slotGetterMode, slotChangerMode});

    // block search

    private ModeProperty blockSearchMode = ModeProperty.newInstance("Block Search", new String[]{"RightUnder", "Closest"}, "Closest");
    private NumberProperty blockSearchDistance = NumberProperty.newInstance("Block Search Distance", 3f, 6f, 8f, 0.5f, ()->blockSearchMode.is("Closest"));
    public BooleanProperty sameY = BooleanProperty.newInstance("SameY", false);

    private CompositeProperty blockSearchGroup = CompositeProperty.newInstance("Block Search", new Property[]{blockSearchMode, blockSearchDistance,sameY});

    // block placement

    private ModeProperty placeMode = ModeProperty.newInstance("Block Placement Mode", new String[]{"Packet", "PacketCustom", "PacketRay","PacketRayRandom", "PacketForce", "PacketExpandable"}, "Packet");

    private NumberProperty packetCustomHitVecX = NumberProperty.newInstance("Custom HitVec X", 0f, 0.5f, 1f, 0.1f);
    private NumberProperty packetCustomHitVecY = NumberProperty.newInstance("Custom HitVec Y", 0f, 0.5f, 1f, 0.1f);
    private NumberProperty packetCustomHitVecZ = NumberProperty.newInstance("Custom HitVec Z", 0f, 0.5f, 1f, 0.1f);

    private CompositeProperty packetCustomGroup = CompositeProperty.newInstance("PacketCustom", new Property[]{packetCustomHitVecX, packetCustomHitVecY, packetCustomHitVecZ}, ()->placeMode.is("PacketCustom"));

    private MultiProperty placeChecks = MultiProperty.newInstance("Placement Checks", new String[]{"Only Horizontal", "Not on Ground", "Not in Air", "Not above Player"});


    private BooleanProperty clientSideSwing = BooleanProperty.newInstance("ClientSide Swing", true);

    private ModeProperty multigodbypass = ModeProperty.newInstance("MultiPlace GodBypass", new String[]{"Never", "Only on Tower", "Always"}, "Never");


    private CompositeProperty placeGroup = CompositeProperty.newInstance("Place Group", new Property[]{placeMode, packetCustomGroup, placeChecks,multigodbypass,clientSideSwing});

    private BooleanProperty blockCounter = BooleanProperty.newInstance("Block Counter", true);
    private CompositeProperty renderingGroup = CompositeProperty.newInstance("Rendering", new Property[]{blockCounter});

    // gridding

    //private ModeProperty gridding = ModeProperty.newInstance("Grid Mode", new String[]{"None", "View"}, "None");


    // sneak

    private ModeProperty sneakMode = ModeProperty.newInstance("Sneak Mode", new String[]{"None", "Input", "SlowDown"}, "None");

    private NumberProperty sneakInterval = NumberProperty.newInstance("Sneak Interval", 1f, 1f, 10f, 1f);

    private NumberProperty sneakTime = NumberProperty.newInstance("Sneak Time ( ms )", 0f, 100f, 5000f, 50f);

    private CompositeProperty sneakGroup = CompositeProperty.newInstance("Sneak", new Property[]{sneakMode, sneakInterval, sneakTime});

    // jump

    private ModeProperty jumpMode = ModeProperty.newInstance("Jump Mode", new String[]{"None", "Input", "Motion"}, "None");
    private NumberProperty motionJumpAmount = NumberProperty.newInstance("Jump Motion", 0f, 0.42f, 1f, 0.1f, ()->jumpMode.is("Motion"));

    private NumberProperty jumpInterval = NumberProperty.newInstance("Jump Interval", 1f, 1f, 10f, 1f);
    private CompositeProperty jumpGroup = CompositeProperty.newInstance("Jump", new Property[]{jumpMode, motionJumpAmount, jumpInterval});

    // sprint

    private ModuleModeProperty sprintMMP = new ModuleModeProperty(this, "Sprint", "Natural",
            new NaturalSprint("Natural", this),
            new VanillaSprint("Vanilla", this),
            new WatchdogSprint("Watchdog", this),
            new WatchdogSemiSprint("WatchdogSemi", this),
            new WatchdogBoostSprint("Watchdog Boost", this),
            new WatchdogRetardProofBoostSprint("Watchdog Boost 2", this),
            new NoSprint("No Sprint", this),
            new MotionSprint("Motion", this)
    );

    public BooleanProperty jump = BooleanProperty.newInstance("Jump on Enable", false, () -> sprintMMP.getModeProperty().is("Watchdog Boost"));

    public NumberProperty sprintMotion = NumberProperty.newInstance("Speed", 0f, 0.2f, 1f, 0.01f, ()->sprintMMP.getModeProperty().is("Motion"));
    private CompositeProperty sprintGroup = CompositeProperty.newInstance("Sprint Group", new Property[]{sprintMMP.getModeProperty(), sprintMotion});

    // tower

    public final ModuleModeProperty towerMMP = new ModuleModeProperty(
            this, "Tower", "Watchdog Fast",
            new WatchdogFastTower2("Old Watchdog Fast", this),
            new WatchdogFastTower("Watchdog Fast", this),
            new MushMcTower("Mushmc", this),
            new VanillaTower("Vanilla", this),
            new VulcanVanillaTower("Vulcan", this),
            new VerusTower("Verus", this),
            new NoTower("None", this)
    );

    private CompositeProperty towerGroup = CompositeProperty.newInstance("Tower Group", new Property[]{towerMMP.getModeProperty()});
    
    private TimeUtil sneakTimeUtil = new TimeUtil();

    private PosFace currentPosFace, lastPosFace;
    private BlockPos pos = null;
    private int placeY = -1;
    private CompleteRotationData posObj = null;
    public int blocksPlaced = 0;
    private float watchdogYawFixRandom;
    private float[] goalRotations = new float[2];

    private float yaw, pitch, lastYaw, lastPitch;

    private int oldSlot, currentSlot;

    private FastNoiseLite fnl = new FastNoiseLite(1337);


    private int ticksSinceLastSlot = 0;

    public void onEnable(){
        currentPosFace = lastPosFace = null;
        pos = null;
        posObj = null;

        oldSlot = mc.thePlayer.inventory.currentItem;


        if(slotMode.is("Spoof")){
            ItemRenderComponent.spoofedSwordSlot = oldSlot;
        }

        blocksPlaced = 0;

        if(preRotate.getValue()){
            Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
        }
        placeY = (int) (mc.thePlayer.posY - 1);
        yaw = lastYaw = mc.thePlayer.rotationYaw;
        pitch = lastPitch = mc.thePlayer.rotationPitch;
        goalRotations = new float[]{yaw, pitch};
    }

    public void onDisable(){
        currentPosFace = lastPosFace = null;
        pos = null;
        posObj = null;
        ItemRenderComponent.spoofedSwordSlot = -1;

        goalRotations = null;
        mc.thePlayer.inventory.currentItem = oldSlot;

        Ambient.getInstance().getRotationComponent().updateRotations();
        Ambient.getInstance().getRotationComponent().toFix = new Float[]{0f, 0f};
        Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
    }

    public int getTotalBlocksCount() {
        int totalCount = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock && isItemStackAllowed(stack)) {
                totalCount += stack.stackSize;
            }
        }
        return totalCount;
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        String bCount = getTotalBlocksCount() + " §fblocks";
        String placedCount = blocksPlaced + " §fplaced";

        if (blockCounter.getValue()) {
            setWidth(29 + Fonts.getNunito(17).getWidth(getTotalBlocksCount() + ""));
            setHeight(24);

            ItemStack item = mc.thePlayer.getCurrentEquippedItem();
            RenderUtil.drawRoundedRect(getAnchoredX(), getY(), getWidth(), getHeight(), 4, new Color(0x90121214, true));
            RenderUtil.drawItemStack(item, getAnchoredX() + 4, getY() + 4);
            Fonts.getNunito(17).drawString(String.valueOf(getTotalBlocksCount()), getAnchoredX() + 24, getY() + getHeight() / 2f - Fonts.getNunito(17).getHeight("" + getTotalBlocksCount()) / 2f + 0.5f, Ambient.getInstance().getHud().getCurrentTheme().color2.getRGB());
        }
    }


    private Vec3 playerEyePos = null;

    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMoveInput(MoveInputEvent event){

        if (mc.thePlayer.onGround && MoveUtil.moving() && blocksPlaced % jumpInterval.getValue().intValue() == 0) {
            switch (jumpMode.getValue()) {
                case "Input" -> {
                    if (!mc.gameSettings.keyBindJump.isPressed()) {
                        event.setJumping(true);
                    }
                }
                case "Motion" -> {
                    mc.thePlayer.motionY = motionJumpAmount.getValue();
                }
            }
        }

        if(sneakMode.is("Input") && blocksPlaced % sneakInterval.getValue().intValue() == 0 && !event.isSneaking()){
            if ((mc.theWorld.getBlockState(new BlockPos(new Vec3(mc.thePlayer.posX + mc.thePlayer.motionX, mc.thePlayer.posY, mc.thePlayer.posZ + mc.thePlayer.motionZ)).down()).getBlock() instanceof BlockAir && mc.thePlayer.onGround)) {
                sneakTimeUtil.reset();
            }
            if (!sneakTimeUtil.finished(sneakTime.getValue().longValue())) {
                event.setSneaking(true);
            }
        }


        if(lastPosFace != null){

            float[] centerAim = RotationUtil.getRotationDifference(playerEyePos, new Vec3(lastPosFace.pos.getX() + 0.5, 0, lastPosFace.pos.getZ() + 0.5), 0,0);

            float centerAimYaw = MathHelper.wrapAngleTo180_float(centerAim[0]);
            float viewAngleYaw = MathHelper.wrapAngleTo180_float(Ambient.getInstance().getRotationComponent().getRotation()[0]);

            float diff = viewAngleYaw - centerAimYaw;

            if(Math.abs(diff) > 5){
                // will fix later im too lazy rn
            }


        }
    }



    @SubscribeEvent(EventPriority.HIGH)
    private void onNetworkTick(PreMotionEvent event){
        ticksSinceLastSlot++;


        if(slotChangerMode.isSelected("On Enable") && blocksPlaced < 1){
            if(!setSlotOwO(true)){
                this.setEnabled(false);
                return;
            }
        }
        // jump

        if(mc.gameSettings.keyBindJump.pressed){
            placeY = (int) (mc.thePlayer.posY - 1);
        }


        // scaffold prediction $$$
        if(rotationPredictionTick.getValue() > 0){
            for(int i = 0; i < rotationPredictionTick.getValue(); i++){
                playerEyePos = new Vec3(mc.thePlayer.posX + (mc.thePlayer.motionX * i),
                        mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                        mc.thePlayer.posZ + (mc.thePlayer.motionZ * i));
                Vec3 baseYPos = new Vec3(playerEyePos.xCoord, playerEyePos.yCoord - mc.thePlayer.getEyeHeight() - 0.01, playerEyePos.zCoord);

                if(mc.theWorld.getBlockState(new BlockPos(baseYPos)).getBlock() instanceof BlockAir){
                    break;
                }
            }
        }else{
            playerEyePos = new Vec3(mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ);
        }

        pos = getPos();

        if((blocksPlaced < 1 || (pos == null && mc.thePlayer.onGround)) && preRotate.getValue()){

            yaw = Ambient.getInstance().getRotationComponent().rotationYaw - 180;
            pitch = preRotatePitch.getValue();

            Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
            Ambient.getInstance().getRotationComponent().setRotations(yaw, pitch, getMoveCorrectFromSetting());
            return;
        }


        if(pos != null) {
            currentPosFace = BlockUtil.getBlockFacing(pos);

            if (currentPosFace != null) {
                this.posObj = null;
                lastPosFace = currentPosFace;
            }
            this.posObj = getRotationData();

            CompleteRotationData ffff = BlockUtil.getRTS(lastPosFace, playerEyePos);

            if(ffff != null){
                mightPlaceThisTurn = true;
            }else{
                mightPlaceThisTurn = false;
            }


            if(this.posObj != null){
                goalRotations = this.posObj.rotations;
            }
        }


        if(mode.is("Telly")){
            if(mc.thePlayer.onGround || (mc.thePlayer.airTicks < 1 && !mc.gameSettings.keyBindJump.pressed)){
                goalRotations = new float[]{Ambient.getInstance().getRotationComponent().getRotation()[0],Ambient.getInstance().getRotationComponent().getRotation()[1]};
            }
        }


        lastYaw = yaw;
        lastPitch = pitch;

        if(goalRotations != null){
            float[] rota = modulateRotationData(goalRotations, new float[]{yaw, pitch});
            if(snapScaffold.getValue() && !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() instanceof BlockAir)){
                rota = new float[]{Ambient.getInstance().getRotationComponent().getRotation()[0], pitch + fnl.GetNoise((float) (mc.thePlayer.posX * 5f), (float) (mc.thePlayer.posY * 5f))};
            }

            rota = RotationUtil.applySensitivity(rota, new float[]{yaw, pitch});

            yaw = rota[0];
            pitch = rota[1];

            Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
            Ambient.getInstance().getRotationComponent().setRotations(yaw, pitch, getMoveCorrectFromSetting());
        }




    }

    public boolean setSlotOwO(boolean updateNow){
        if (slotGetterMode.is("Max") || (slotGetterMode.is("EndStack") && (mc.thePlayer.inventory.getCurrentItem() == null || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock))) || (mc.thePlayer.inventory.getCurrentItem() == null || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock))) {
            int tempslot = getBlockItem();
            if (tempslot == -1) {
                return false;
            }
            currentSlot = tempslot;
        }
        if(updateNow){
            mc.playerController.syncCurrentPlayItem();
            ticksSinceLastSlot = 0;
        }
        mc.thePlayer.inventory.currentItem = currentSlot;
        return true;
    }


    private void swing(){
        if(clientSideSwing.getValue()){
            mc.thePlayer.swingItem();
        }else{
            PacketUtil.sendPacket(new C0APacketAnimation());
        }
    }

    @SubscribeEvent
    private void onTickEvent(UpdateEvent event){
        hasPlacedThisTick = false;
        // slots & switch shit



        if(multigodbypass.is("Always") || (multigodbypass.is("Only on Tower") && mc.gameSettings.keyBindJump.pressed)){

            int attempts = 0;
            boolean should = true;
            while (attempts < 20 && should){
                pos = getPos();
                if(pos != null && mc.theWorld.getBlockState(pos).getBlock() == Blocks.air) {
                    currentPosFace = BlockUtil.getBlockFacing(pos);


                    if (currentPosFace != null) {
                        this.posObj = null;
                        lastPosFace = currentPosFace;
                    }

                    if(lastPosFace != null){
                        this.posObj = getRotationData();

                        if(this.posObj != null){
                            placeBlock();
                        }
                    }


                }else{
                    should = false;
                }
                attempts++;
            }

            if(attempts > 1){
            }


        }else{
            placeBlock();
        }



        if(slotChangerMode.isSelected("After Tick")){
            if(!setSlotOwO(true)){
                this.setEnabled(false);
            }
        }
    }



    public void placeBlock(){
        if(slotChangerMode.isSelected("On Tick") && ticksSinceLastSlot > 2){ // peak shi
            if(!setSlotOwO(true)){
                this.setEnabled(false);
                return;
            }
        }



        if(placeChecks.isSelected("Only Horizontal") && this.posObj.movingObjectPosition.sideHit == EnumFacing.UP){
            return;
        }
        if(placeChecks.isSelected("Not on Ground") && mc.thePlayer.onGround){
            return;
        }
        if(placeChecks.isSelected("Not in Air") && !mc.thePlayer.onGround){
            return; // having both enabled WILL break scaffold <3
        }
        if(placeChecks.isSelected("Not above Player") && this.posObj.movingObjectPosition.getBlockPos().getY() > mc.thePlayer.posY + 1){
            return;
        }

        switch (placeMode.getValue()){
            case "Packet" -> {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(currentSlot), this.posObj.movingObjectPosition.getBlockPos(), this.posObj.movingObjectPosition.sideHit, this.posObj.movingObjectPosition.hitVec)) {
                    swing();
                    blocksPlaced++;
                    hasPlacedThisTick = true;
                }
            }
            case "PacketRay" -> {
                MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTraceCustomEye(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),4.5f, yaw, pitch);

                if(movingObjectPosition == null){
                    return;
                }
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(currentSlot), movingObjectPosition.getBlockPos(), movingObjectPosition.sideHit, movingObjectPosition.hitVec)) {
                    swing();
                    blocksPlaced++;
                    hasPlacedThisTick = true;
                }
            }
            case "PacketExpandable" -> {

                CompleteRotationData c = BlockUtil.getRTSNS(lastPosFace, playerEyePos);

                if(c == null){
                    return;
                }
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(currentSlot), c.movingObjectPosition.getBlockPos(), c.movingObjectPosition.sideHit, c.movingObjectPosition.hitVec)) {
                    swing();
                    blocksPlaced++;
                    hasPlacedThisTick = true;
                }
            }
            case "PacketRayRandom" -> {
                MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTraceCustomEye(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),4.5f, yaw, pitch);

                if(movingObjectPosition == null){
                    return;
                }
                Vec3 currentHitVec = movingObjectPosition.hitVec;

                currentHitVec.add(new Vec3(0, fnl.GetNoise((float) mc.thePlayer.posX, (float) mc.thePlayer.posZ), 0));


                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(currentSlot), movingObjectPosition.getBlockPos(), movingObjectPosition.sideHit,currentHitVec )) {
                    swing();
                    blocksPlaced++;
                    hasPlacedThisTick = true;
                }
            }
            case "PacketCustom" -> {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(currentSlot), this.posObj.movingObjectPosition.getBlockPos(), this.posObj.movingObjectPosition.sideHit, new Vec3(packetCustomHitVecX.getValue(), packetCustomHitVecY.getValue(), packetCustomHitVecZ.getValue()))) {
                    swing();
                    blocksPlaced++;
                    hasPlacedThisTick = true;
                }
            }
            case "PacketForce" -> {
                float[] rotations = BlockUtil.getRotationToBlockDirect(lastPosFace);
                MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTraceCustomEye(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),4.5f, rotations[0], rotations[1]);
                if(movingObjectPosition == null){
                    return;
                }

                if(movingObjectPosition.sideHit != lastPosFace.getFacing()){
                    return;
                }
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(currentSlot), movingObjectPosition.getBlockPos(), movingObjectPosition.sideHit, movingObjectPosition.hitVec)) {
                    swing();
                    blocksPlaced++;
                    hasPlacedThisTick = true;
                }
            }
        }
    }


    @SubscribeEvent
    private void render3D(Render3DEvent event){
        if(playerEyePos != null){
            //ESPUtil.point(playerEyePos, Color.RED, 0.05f);
        }

        if(currentPosFace != null && playerEyePos != null){
            float[] rotations = BlockUtil.getRotationToBlockEdgeFromEyePos(currentPosFace, playerEyePos);

            MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTraceCustomEye(playerEyePos,6f, rotations[0], rotations[1]);

            ESPUtil.outlinedBlockESP(movingObjectPosition.getBlockPos(), Ambient.getInstance().getHud().getColor1(), 1f);
        }

    }


    public CompleteRotationData getRotationData() {
        Ambient.getInstance().getRotationComponent().toFix = new Float[]{0f, 0f};
        switch (rotations.getValue()) {
            case "Normal" -> {

                //float[] rotations = BlockUtil.getR(lastPosFace);


                float[] rts = BlockUtil.getRotationToBlockDirect(lastPosFace);
                CompleteRotationData ct = new CompleteRotationData(mc.thePlayer.rayTraceCustomEye(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), 4.5, rts[0], rts[1]), rts);

                return ct;
            }
            case "RayTrace" -> {
                CompleteRotationData ct = null;
                if (directRotationWhileInAir.getValue() && !mc.thePlayer.onGround) {
                    float[] rts = BlockUtil.getRotationToBlockDirect(lastPosFace);
                    ct = new CompleteRotationData(mc.thePlayer.rayTraceCustomEye(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), 4.5, rts[0], rts[1]), rts);
                } else {
                    ct = BlockUtil.getRTS2(lastPosFace, yaw, pitch, playerEyePos, rotationRaytraceDivisor.getValue().intValue());
                }

                return ct;
            }
            case "GodBridge" -> {
                BlockPos pos = new BlockPos(mc.thePlayer);

                double playerXDiff = ((pos.getX() + 0.5) % 1) - (mc.thePlayer.posX % 1);
                double playerZDiff = ((pos.getZ() + 0.5) % 1) - (mc.thePlayer.posZ % 1);

                float yaw = this.yaw;
                float pitch = 78f;

                switch (mc.thePlayer.getHorizontalFacingMFIX()) {
                    case EAST:
                        yaw = 135f;
                        if (playerZDiff > 0) {
                            yaw -= 90f;
                            Ambient.getInstance().getRotationComponent().toFix = new Float[]{-1f, 1f};
                        }
                        break;
                    case WEST:
                        yaw = -45;
                        if (playerZDiff < 0) {
                            yaw -= 90f;
                            Ambient.getInstance().getRotationComponent().toFix = new Float[]{-1f, 1f};
                        }
                        break;
                    case SOUTH:
                        yaw = -135;
                        if (playerXDiff < 0) {
                            yaw -= 90f;
                            Ambient.getInstance().getRotationComponent().toFix = new Float[]{-1f, 1f};
                        }
                        break;
                    case NORTH:
                        yaw = 45f;
                        if (playerXDiff > 0) {
                            yaw -= 90f;
                            Ambient.getInstance().getRotationComponent().toFix = new Float[]{-1f, 1f};
                        }
                        break;
                }

                float divisor = Math.abs(Ambient.getInstance().getRotationComponent().getRotation()[0] % 90);
                if (divisor > 10 && divisor < 80) {
                    Ambient.getInstance().getRotationComponent().toFix = new Float[]{0f, 0f};

                    yaw = Ambient.getInstance().getRotationComponent().getRotation()[0] - 180;
                }

                float[] rts = BlockUtil.getRotationToBlockDirect(lastPosFace);

                if (placeMode.is("Packet")) {
                    return new CompleteRotationData(mc.thePlayer.rayTraceCustomEye(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), 4.5, rts[0], rts[1]), new float[]{yaw, pitch});
                } else {
                    return BlockUtil.getRotationsFromYaw(lastPosFace, yaw, playerEyePos);
                }


            }
            case "Edge4" -> {
                float dirYaw = getDirection();

                boolean leftSide;

                Vec3 position = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5); // center pos

                double yawRad = Math.toRadians(dirYaw);
                double dirX = -Math.sin(yawRad);
                double dirZ = Math.cos(yawRad);

                double toEntryX = position.xCoord - mc.thePlayer.posX;
                double toEntryZ = position.zCoord - mc.thePlayer.posZ;

                leftSide = (dirX * toEntryZ - dirZ * toEntryX) > 0;


                if (MoveUtil.isDiag()) {
                    yaw = dirYaw + (leftSide ? 145f : -145f);
                } else if (!MoveUtil.isDiag()) {
                    yaw = dirYaw + (leftSide ? 92  : -92f);
                }

                pitch = 82.2f;

                MovingObjectPosition pos = BlockUtil.getRTS(lastPosFace, playerEyePos).movingObjectPosition;
                return new CompleteRotationData(pos, new float[]{yaw, pitch});
            }
            case "Edge" -> {
                float dirYaw = getDirection();

                boolean leftSide;

                Vec3 position = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5); // center pos

                double yawRad = Math.toRadians(dirYaw);
                double dirX = -Math.sin(yawRad);
                double dirZ = Math.cos(yawRad);

                double toEntryX = position.xCoord - mc.thePlayer.posX;
                double toEntryZ = position.zCoord - mc.thePlayer.posZ;

                leftSide = (dirX * toEntryZ - dirZ * toEntryX) > 0;


                yaw = dirYaw + (leftSide ? 145f : -145f);

                pitch = 82.2f;

                MovingObjectPosition pos = BlockUtil.getRTS(lastPosFace, playerEyePos).movingObjectPosition;
                return new CompleteRotationData(pos, new float[]{yaw, pitch});
            }

            case "None" -> {
                float baseRotation = Ambient.getInstance().getRotationComponent().getRotation()[0];
                yaw = MathHelper.wrapAngleTo180_float(baseRotation + (360));
                MovingObjectPosition pos = Objects.requireNonNull(BlockUtil.getRTS(lastPosFace, playerEyePos)).movingObjectPosition;
                return new CompleteRotationData(pos, new float[]{yaw});
            }
            case "Edge2" -> {
                float[] rotations = BlockUtil.getRotationToBlockEdge(lastPosFace);
                yaw = rotations[0];

                if (MoveUtil.isDiag()) {
                    yaw = MathHelper.wrapAngleTo180_float(getDirection() - 140);
                } else {
                    float dirYaw = getDirection();
                    boolean leftSide;
                    Vec3 position = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5); // center pos


                    double yawRad = Math.toRadians(dirYaw);
                    double dirX = -Math.sin(yawRad);
                    double dirZ = Math.cos(yawRad);

                    double toEntryX = position.xCoord - mc.thePlayer.posX;
                    double toEntryZ = position.zCoord - mc.thePlayer.posZ;

                    leftSide = (dirX * toEntryZ - dirZ * toEntryX) > 0;

                    yaw = dirYaw + (leftSide ? 92  : -92f);
                }

                pitch = 82.2f;

                MovingObjectPosition pos = BlockUtil.getRTS(lastPosFace, playerEyePos).movingObjectPosition;
                return new CompleteRotationData(pos, new float[]{yaw, pitch});
            }
            case "Edge3" -> {
                CompleteRotationData ffff = BlockUtil.getRTS(lastPosFace, playerEyePos);

                boolean skipPPOSCHECK = ffff == null;
                MovingObjectPosition ppos = null;

                if(!skipPPOSCHECK){
                    ppos = ffff.movingObjectPosition;
                }

                if(!MoveUtil.isDiag()){
                    float dirYaw = getDirection();
                    boolean leftSide;
                    Vec3 position = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5); // center pos

                    double yawRad = Math.toRadians(dirYaw);
                    double dirX = -Math.sin(yawRad);
                    double dirZ = Math.cos(yawRad);

                    double toEntryX = position.xCoord - mc.thePlayer.posX;
                    double toEntryZ = position.zCoord - mc.thePlayer.posZ;

                    leftSide = (dirX * toEntryZ - dirZ * toEntryX) > 0;

                    boolean canPlace = false;



                    yaw = dirYaw + (leftSide ? 145 : -145);


                    /*if(canPlace && ppos.sideHit != EnumFacing.UP){
                        yaw = dirYaw + (leftSide ? 90 : -90);
                    }else {
                        yaw = dirYaw + (leftSide ? 145 : -145);


                    }*/
                }else{
                    yaw = getDirection() + 90;
                }
                pitch = 52f;


                return new CompleteRotationData(ppos, new float[]{yaw, pitch});

            }
        }
        return null;
    }

    public float[] modulateRotationData(float[] goal, float[] last){
        float deltaYaw = MathHelper.wrapAngleTo180_float(goal[0] - last[0]);
        float deltaPitch = goal[1] - last[1];

        float rdmYaw = MathHelper.getRandomFloat(rotationLimiterYawMax.getValue(), rotationLimiterYawMin.getValue());
        float rdmPitch = MathHelper.getRandomFloat(rotationLimiterPitchMin.getValue(), rotationLimiterPitchMax.getValue());

        deltaYaw = MathHelper.clamp_float(deltaYaw,-rdmYaw, rdmYaw);
        deltaPitch = MathHelper.clamp_float(deltaPitch,-rdmPitch, rdmPitch);

        return new float[]{last[0] + deltaYaw, last[1] + deltaPitch};
    }



    public BlockPos getPos(){
        switch (blockSearchMode.getValue()){
            case "Closest" -> {
                if(sameY.getValue()){
                    return BlockUtil.getNearestPlaceble(blockSearchDistance.getValue().intValue(), placeY);
                }else{
                    return BlockUtil.getNearestPlaceble(blockSearchDistance.getValue().intValue());
                }
            }
            case "RightUnder" -> {
                if(sameY.getValue()){
                    return new BlockPos(mc.thePlayer.posX,placeY, mc.thePlayer.posZ);
                }else{
                    return new BlockPos(mc.thePlayer.posX,mc.thePlayer.posY, mc.thePlayer.posZ);
                }
            }
        }
        return null;
    }



    public MoveCorrect getMoveCorrectFromSetting(){
        switch (moveCorrectMode.getValue()){
            case "None" -> {
                return MoveCorrect.OFF;
            }
            case "Silent" -> {
                return MoveCorrect.SILENT;
            }
            case "Strict" -> {
                return MoveCorrect.STRICT;
            }
            case "NoSprint" -> {
                return MoveCorrect.SPRINT;
            }
        }
        return MoveCorrect.OFF;
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





    public float getDirection() {
        float direction = mc.thePlayer.rotationYaw;
        float forward = 1.0F;

        if (mc.thePlayer.moveForward < 0.0F) {
            direction += 180.0F;
        }

        if (mc.thePlayer.moveForward < 0.0F) {
            forward = -0.5F;
        } else if (mc.thePlayer.moveForward > 0.0F) {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0.0F) {
            direction -= 90.0F * forward;
        } else if (mc.thePlayer.moveStrafing < 0.0F) {
            direction += 90.0F * forward;
        }

        return direction;
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