package fr.ambient.module.impl.combat;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import de.florianmichael.vialoadingbase.ViaLoadingBase;
import fr.ambient.Ambient;
import fr.ambient.component.impl.misc.BlackDetectorComponent;
import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.AttackEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.misc.MCF;
import fr.ambient.module.impl.player.Breaker;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.property.Property;
import fr.ambient.property.impl.*;
import fr.ambient.property.impl.wrappers.EnumModeProperty;
import fr.ambient.util.*;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.PlayerUtil;
import fr.ambient.util.player.RotationUtil;
import fr.ambient.util.player.movecorrect.MoveCorrect;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.model.ESPUtil;
import lombok.SneakyThrows;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class KillAura extends Module {
    public KillAura() {
        super(4, "Automatically attacks all nearby enemies or mobs", ModuleCategory.COMBAT);
        registerProperties(targetMode,onlyOnSword,
                rotationDistance, swingDistance, attackDistance, blockDistanceDistance, moveCorrectMode, rotationGroup, attackGroup, autoBlockGroup, targetGroup, renderGroup);
        this.setSuffix(targetMode::getValue);
    }


    public boolean isBlocking = false;
    private boolean isBlinking = false;


    private final ModeProperty targetMode = ModeProperty.newInstance("Selection", new String[]{"Single", "Switch", "Multi"}, "Single");

    private final BooleanProperty onlyOnSword = BooleanProperty.newInstance("Only on Sword", false);


    private final NumberProperty rotationDistance = NumberProperty.newInstance("Rotation Distance", 0f, 4.5f, 6f, 0.1f);
    private final NumberProperty swingDistance = NumberProperty.newInstance("Swing Distance", 0f, 4f, 6f, 0.1f);
    private final NumberProperty attackDistance = NumberProperty.newInstance("Attack Distance", 0f, 3f, 6f, 0.1f);
    private final NumberProperty blockDistanceDistance = NumberProperty.newInstance("Block Distance", 0f, 3f, 6f, 0.1f);
    private final ModeProperty moveCorrectMode = ModeProperty.newInstance("MoveFix Mode", new String[]{"None", "Silent", "Strict", "NoSprint"}, "None");

    private final ModeProperty rotationMode = ModeProperty.newInstance("Rotation", new String[]{"Direct", "Smooth", "Smooth2", "Pattern"}, "Direct");
    private final ModeProperty rotationLimiterMode = ModeProperty.newInstance("Rotation Limiter Mode", new String[]{"None", "Static", "DistDep"}, "None");
    private final NumberProperty rotationLimiterStaticYaw = NumberProperty.newInstance("Static Yaw Amount", .1f, 20f, 180f, 1f, () -> rotationLimiterMode.is("Static"));
    private final NumberProperty rotationLimiterStaticPitch = NumberProperty.newInstance("Static Pitch Amount", .1f, 20f, 90f, 1f, () -> rotationLimiterMode.is("Static"));
    private final NumberProperty rotationLimiterDistDepMultiplier = NumberProperty.newInstance("DistDep Multiplier", .1f, 1f, 5f, .1f, () -> rotationLimiterMode.is("DistDep"));

    private final NumberProperty onlyOnNeededDistance = NumberProperty.newInstance("RotateOnlyOnDist", 0f, 0.4f, 6f, 0.1f);

    private final CompositeProperty rotationLimiterGroup = CompositeProperty.newInstance("Rotation Limiter", new Property<?>[]{rotationLimiterMode, rotationLimiterStaticYaw, rotationLimiterStaticPitch, rotationLimiterDistDepMultiplier, onlyOnNeededDistance}, () -> true);

    private final ModeProperty rotationRandomizationMode = ModeProperty.newInstance("Randomization Mode", new String[]{"None", "MathRandom", "Noise"}, "None");
    private final NumberProperty rotationRandomMathRandomYaw = NumberProperty.newInstance("Random Yaw Amount", 0f, 1f, 10f, 0.1f, () -> rotationRandomizationMode.is("MathRandom"));
    private final NumberProperty rotationRandomMathRandomPitch = NumberProperty.newInstance("Random Pitch Amount", 0f, 1f, 10f, 0.1f, () -> rotationRandomizationMode.is("MathRandom"));
    private final NumberProperty rotationRandomNoiseMultiplier = NumberProperty.newInstance("Noise Multiplier", 0f, 1f, 10f, 0.1f, () -> rotationRandomizationMode.is("Noise"));
    private final CompositeProperty rotationRandomGroup = CompositeProperty.newInstance("Rotation Randomization", new Property<?>[]{rotationRandomizationMode, rotationRandomMathRandomYaw, rotationRandomMathRandomPitch, rotationRandomNoiseMultiplier}, () -> true);

    private final ModeProperty rotationHitVecMode = ModeProperty.newInstance("HitVec Mode", new String[]{"Best", "Head", "Body", "Legs", "Test"}, "Best");
    private final NumberProperty rotationHitVecBestMargin = NumberProperty.newInstance("Rotation HitVec Margin", -0.5f, 0f, 0.5f, 0.1f, () -> rotationHitVecMode.is("Best"));
    private final NumberProperty rotationHitVecPredictionAmount = NumberProperty.newInstance("Prediction Amount", 0f, 0f, 5f, 0.1f);
    private final CompositeProperty rotationHitVecGroup = CompositeProperty.newInstance("HitVec / Aim Point", new Property<?>[]{rotationHitVecMode, rotationHitVecBestMargin, rotationHitVecPredictionAmount}, () -> true);

    private final CompositeProperty rotationGroup = CompositeProperty.newInstance("Rotations", new Property<?>[]{rotationMode, rotationLimiterGroup, rotationRandomGroup, rotationHitVecGroup}, () -> true);

    private final ModeProperty clickMode = ModeProperty.newInstance("Click Mode", new String[]{"Packet", "Click"}, "Packet");
    private final BooleanProperty keepSprint = BooleanProperty.newInstance("KeepSprint", false);
    private final NumberProperty keepSprintAmount = NumberProperty.newInstance("KeepSprint Perc", 0f, 0.6f, 1f, 0.1f, () -> keepSprint.getValue());

    private final ModeProperty clickFreqMode = ModeProperty.newInstance("Click Frequency Mode", new String[]{"CPS", "Pattern", "Perfect"}, "CPS");
    private final NumberProperty cpsMax = NumberProperty.newInstance("Max CPS", 0f, 16f, 20f, 0.1f, () -> clickFreqMode.is("CPS"));
    private final NumberProperty cpsMin = NumberProperty.newInstance("Min CPS", 0f, 12f, 20f, 0.1f, () -> clickFreqMode.is("CPS"));
    private final NumberProperty clickRandomizationSLoop = NumberProperty.newInstance("Random Multiplier", 0.1f, 1f, 5f, 0.1f, () -> clickFreqMode.is("CPS"));
    private final NumberProperty clickRandomizationSLoopTime = NumberProperty.newInstance("Random Loop Time", 50f, 500f, 5000f, 50f, () -> clickFreqMode.is("CPS"));
    private final ModeProperty clickRotationSpeedAdaptator = ModeProperty.newInstance("Rotation Speed Adaptator", new String[]{"None", "Still", "Stop"}, "None");
    private final BooleanProperty perfectClick = BooleanProperty.newInstance("Allow Perfect Hit", false);
    private final BooleanProperty noAttackWhileBlock = BooleanProperty.newInstance("No attack while blocking", false);
    private final CompositeProperty clickAmountGroup = CompositeProperty.newInstance("Click Frequency", new Property<?>[]{clickFreqMode, cpsMax, cpsMin, clickRandomizationSLoop, clickRandomizationSLoopTime, clickRotationSpeedAdaptator, perfectClick,noAttackWhileBlock}, () -> true);

    private final MultiProperty swingMult = MultiProperty.newInstance("Swing", new String[]{"Client", "Server"}, new HashSet<>(Arrays.asList("Client", "Server")));
    private final BooleanProperty raytrace = BooleanProperty.newInstance("Raytrace", false);

    private final ModeProperty interactAfterAttack = ModeProperty.newInstance("Post Attack Interactions", new String[]{"None", "Interact", "Interact-At", "Full"}, "Interact");
    private final CompositeProperty interactGroup = CompositeProperty.newInstance("Interactions", new Property[]{interactAfterAttack}, () -> true);


    private BooleanProperty checkIfUI = BooleanProperty.newInstance("Check if in UI", false);

    private final CompositeProperty attackGroup = CompositeProperty.newInstance("Attack", new Property<?>[]{clickMode, keepSprint, keepSprintAmount, clickAmountGroup, swingMult, raytrace, interactGroup, checkIfUI}, () -> true);

    private final ModeProperty autoBlockMode = ModeProperty.newInstance("AB Mode", new String[]{
            "None",
            "Fake",
            "Watchdog",
            "Vulcan",
            "Vanilla",
            "Bug",}, "Fake");

    private final ModeProperty watchdogAutoBlockMode = ModeProperty.newInstance("Watchdog Mode", new String[]{"1.12 20","1.12 10", "1.8 Pred"}, "Normal", () -> autoBlockMode.is("Watchdog"));

    private final CompositeProperty autoBlockGroup = CompositeProperty.newInstance("AutoBlock", new Property[]{autoBlockMode, watchdogAutoBlockMode}, () -> true);


    private final NumberProperty switchDelay = NumberProperty.newInstance("Switch Delay", 1f, 3f, 10f, 1f, () -> targetMode.is("Switch"));
    private final MultiProperty targetMultiProp = MultiProperty.newInstance("Targets", new String[]{"Players", "Hostile", "Passive"}, new HashSet<>(Arrays.asList("Players")));
    private final MultiProperty targetMultiEffects = MultiProperty.newInstance("Modifiers", new String[]{"Target Invisible", "Target Behind Wall", "Racism", "Target Team", "Check If Dead"}, new HashSet<>(Arrays.asList("Target Invisible", "Target Behind Wall")));
    private final ModeProperty targetSorting = ModeProperty.newInstance("Sorting", new String[]{"Distance", "Health", "HurtTime"}, "Distance");
    private final CompositeProperty targetGroup = CompositeProperty.newInstance("Target", new Property<?>[]{switchDelay, targetMultiProp, targetMultiEffects, targetSorting}, () -> true);


    private final BooleanProperty renderHitVec = BooleanProperty.newInstance("Render HitVec", false);
    private final NumberProperty renderHitVecSize = NumberProperty.newInstance("Render Size", 0.01f, 0.05f, 0.5f, 0.01f, () -> renderHitVec.getValue());

    private final CompositeProperty hitVecRender = CompositeProperty.newInstance("HitVec", new Property[]{renderHitVec, renderHitVecSize}, () -> true);

    private final ModeProperty critParticleMode = ModeProperty.newInstance("Crit Particles", new String[]{"None", "Only on Fall", "Always"}, "Only on Fall");
    private final EnumModeProperty<EnumParticleTypes> critParticles = new EnumModeProperty<EnumParticleTypes>("Particles", EnumParticleTypes.class, EnumParticleTypes.CRIT, () -> !critParticleMode.is("None"));



    private MultiProperty targetESP = MultiProperty.newInstance("Target ESP", new String[]{"Box", "Ring", "HealthRing"});

    private NumberProperty targetESPBoxMargin = NumberProperty.newInstance("Box Margin", 0f, 0.1f, 0.5f, 0.05f, ()->targetESP.isSelected("Box"));

    private NumberProperty targetESPringDivisors = NumberProperty.newInstance("Ring Divisor", 3f, 12f, 180f, 1f, ()->targetESP.isSelected("Ring"));

    private NumberProperty targetESPringHealth = NumberProperty.newInstance("HealthRing Divisor", 3f, 12f, 180f, 1f, ()->targetESP.isSelected("HealthRing"));

    private final CompositeProperty renderTargetESP = CompositeProperty.newInstance("Target ESP Group", new Property[]{targetESP,targetESPBoxMargin,targetESPringDivisors,targetESPringHealth});




    private final CompositeProperty renderGroup = CompositeProperty.newInstance("Render", new Property[]{hitVecRender, critParticleMode, critParticles.getModeProperty(),renderTargetESP}, () -> true); // Placeholder for moof aline all that without changing anything

    public ArrayList<EntityLivingBase> targetList = new ArrayList<>();
    public static EntityLivingBase target = null;
    public static boolean shouldBlockRender = false;
    private float yaw, pitch, lastYaw, lastPitch;
    private Vec3 hitVec3 = null;
    private final FastNoiseLite fastNoiseLite = new FastNoiseLite(1884);
    private final ClickUtil clickUtil = new ClickUtil();
    private final float lastPYaw = 0;
    private float lastYawDelta = 0;
    private float lastPitchDelta = 0;
    private int killAuraTicks = 0;
    private Vec3 lastHitVec, currentHitVec;
    private TimeUtil timeSinceLastPerfect = new TimeUtil();
    private int ticc = 0;
    private int switchindex = 0;
    private int abCount = 0;

    private float lastFuckedPredictedPitch, lastFuckedPredictedYaw, fuckedPredictedYaw, fuckedPredictedPitch;

    public void onEnable() {
        ticc = 0;
        target = null;
        targetList.clear();
        lastYaw = yaw = mc.thePlayer.rotationYaw;
        lastPitch = pitch = mc.thePlayer.rotationPitch;
        switchindex = 0;

        isBlinking = BlinkComponent.isBlinking;

        abCount = 0;

        ticksSinceLag = 5;

    }

    public void onDisable() {
        target = null;
        targetList.clear();
        Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
        onNoTargetOrCleanup();
    }

    public void onNoTargetOrCleanup() {
        shouldBlockRender = false;
        ticc = 0;
        switchindex = 0;
        targetList.clear();
        lastYaw = yaw = mc.thePlayer.rotationYaw;
        lastPitch = pitch = mc.thePlayer.rotationPitch;
        lastYawDelta = 0;
        lastPitchDelta = 0;
        currentHitVec = lastHitVec = null;
        breakOnNextTick = false;

        if (isBlinking) {
            BlinkComponent.onDisable();
            isBlinking = false;
        }
        unblockSword();
        killAuraTicks = 0;


    }

    private int ticksSinceLag = 0;

    private boolean lastCycleWasAttack = false;

    public boolean unblockSword() {
        boolean didBP = false;
        if (isBlocking) {
            if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword) {
                PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                didBP = true;
            }
            isBlocking = false;
        }
        return didBP;
    }

    private final TimeUtil timeSinceLastClick = new TimeUtil();

    @SubscribeEvent
    private void onRender3D(Render3DEvent event) {
        if (target != null && renderHitVec.getValue()) {
            float distance = PlayerUtil.getBiblicallyAccurateDistanceToEntity(target);
            Vec3 vec31 = mc.thePlayer.getVectorForRotation(pitch, yaw);
            Vec3 vec3 = mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
            Vec3 vec311 = mc.thePlayer.getVectorForRotation(lastPitch, lastYaw);
            Vec3 vec312 = mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks);
            Vec3 vec313 = vec312.addVector(vec311.xCoord * distance, vec311.yCoord * distance, vec311.zCoord * distance);

            Vec3 vec69 = new Vec3(vec313.xCoord + ((vec32.xCoord - vec313.xCoord) * mc.timer.renderPartialTicks),
                    vec313.yCoord + ((vec32.yCoord - vec313.yCoord) * mc.timer.renderPartialTicks),
                    vec313.zCoord + ((vec32.zCoord - vec313.zCoord) * mc.timer.renderPartialTicks));

            ESPUtil.point(vec69, Ambient.getInstance().getHud().getCurrentTheme().color1, renderHitVecSize.getValue());
        }
        if(target != null){
            if(targetESP.isSelected("Box")){
                ESPUtil.filledInterpolatedESP(target, Ambient.getInstance().getThemeManager().getCurrentTheme().color1, 0.2f, targetESPBoxMargin.getValue());
            }
            if(targetESP.isSelected("Ring")){
                ESPUtil.drawCircle(target, 0.7f, targetESPringDivisors.getValue(),2f, Ambient.getInstance().getThemeManager().getCurrentTheme().color2);
            }
            if(targetESP.isSelected("HealthRing")){
                target.getHealthAnimation().run(target.getHealth());
                float perc = target.getHealthAnimation().getFloatValue() / target.getMaxHealth();

                ESPUtil.drawCirclePercentage(target, 0.8f, targetESPringHealth.getValue(),2f,perc, Color.RED,Color.WHITE);
            }
        }

    }

    private void critParticle() {
        if (target != null && mc.thePlayer != null && mc.theWorld != null) {
            switch (critParticleMode.getValue()) {
                case "Only on Fall" -> {
                    if (mc.thePlayer.fallDistance > 0 && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava() && !mc.thePlayer.onGround) {
                        mc.effectRenderer.emitParticleAtEntity(target, critParticles.getValue());
                    }
                }
                case "Always" -> mc.effectRenderer.emitParticleAtEntity(target, critParticles.getValue());
            }
        }
    }

    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            ticksSinceLag = 0;
        }
    }


    private void v112(double dst, boolean canAttackRayTrace){
        if(killAuraTicks == 1){

            if (isBlocking) {
                int oldSlot = mc.thePlayer.inventory.currentItem;
                int slot = mc.thePlayer.inventory.currentItem + 1 % 8;

                if (slot < 0) {
                    slot = 0;
                } else if (slot > 8) {
                    slot = 8;
                }

                PacketUtil.sendPacket(new C09PacketHeldItemChange(slot));
                PacketUtil.sendPacket(new C09PacketHeldItemChange(oldSlot));
            }

            PacketUtil.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
            hypixelblockSword();
            isBlocking = true;
        }else{
            if (dst <= attackDistance.getValue() && canAttackRayTrace) {
                final AttackEvent event = new AttackEvent(target);
                Ambient.getInstance().getEventBus().post(event);
                PacketUtil.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                mc.thePlayer.swingItem();
            }
        }

        if(killAuraTicks > 5){
            killAuraTicks = 0;
        }
    }



    private void test(double dst, boolean canAttackRayTrace){
        if (dst <= attackDistance.getValue() && canAttackRayTrace) {
            if(killAuraTicks % 2 == 0) {
                final AttackEvent event = new AttackEvent(target);
                Ambient.getInstance().getEventBus().post(event);
                PacketUtil.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                mc.thePlayer.swingItem();
            }
        }
        if(killAuraTicks % 5 == 0){
            int oldSlot = mc.thePlayer.inventory.currentItem;

            mc.thePlayer.inventory.currentItem = (mc.thePlayer.inventory.currentItem % 8) + 1;
            mc.playerController.syncCurrentPlayItem();

            mc.thePlayer.inventory.currentItem = oldSlot;
            mc.playerController.syncCurrentPlayItem();

            PacketUtil.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
            hypixelblockSword();
            isBlocking = true;
        }
    }

    private void prediction(double dst, boolean canAttackRayTrace){
        if(killAuraTicks % 2 == 0) {
            if(dst <= attackDistance.getValue() && canAttackRayTrace){
                attackEntity(target);
                PacketUtil.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                blockSword();
                BlinkComponent.onDisable();
            }
        }else{
            BlinkComponent.onEnable();
            unblockSword();
        }




    }






    @SubscribeEvent
    private void onTickEvent(UpdateEvent event){
        if(onlyOnSword.getValue()) {
            if (mc.thePlayer.inventory.getCurrentItem() == null || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword)) {
                return;
            }
        }

        ticksSinceLag++;
        boolean scaffold = Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled();

        if(scaffold){
            isBlocking = false;
            onNoTargetOrCleanup();
            return;
        }

        if(target != null){
            float dst = PlayerUtil.getBiblicallyAccurateDistanceToEntity(target);

            MovingObjectPosition movingObjectPosition = RotationUtil.getMouseEntity(1f,dst , new float[]{yaw, pitch});
            boolean canAttackRayTrace = (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) || !raytrace.getValue();

            shouldBlockRender = !autoBlockMode.is("None") && dst < blockDistanceDistance.getValue();
            if(!breakOnNextTick){
                mc.playerController.syncCurrentPlayItem();
            }

            if(checkIfUI.getValue() && mc.currentScreen != null){
                onNoTargetOrCleanup();
                return;
            }

            if(autoBlockMode.is("Watchdog")) {
                if (tickKATicksWatchdog()) {
                    return;
                }

                if (dst <= swingDistance.getValue()) {
                    mc.thePlayer.swingItemClientSide();
                }
                if (dst <= blockDistanceDistance.getValue()) {
                    switch (watchdogAutoBlockMode.getValue()) {
                        case "1.12 20" -> v112(dst, canAttackRayTrace);
                        case "1.12 10" -> test(dst, canAttackRayTrace);
                        case "1.8 Pred" -> prediction(dst, canAttackRayTrace);
                    }
                } else {
                    if (isBlinking) {
                        BlinkComponent.onDisable();
                        isBlinking = false;
                    }
                    if (isBlocking) {
                        unblockSword();
                    }
                }

                return;
            }

            int clicksAllow = getAllowedClicks(true);

                if (noAttackWhileBlock.getValue()) {
                    if (mc.thePlayer.isUsingItem()) {
                        clicksAllow = 0;
                    }
                }


            if((dst <= swingDistance.getValue() && dst > attackDistance.getValue()) || (dst <= attackDistance.getValue() && !canAttackRayTrace)){
                for(int i = 0; i < clicksAllow; i++){
                    swing();
                }
            }else if(dst <= attackDistance.getValue()){
                if(clicksAllow == 0 && target.hurtTime == 0 && timeSinceLastPerfect.finished(250) && PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) > 2.5f){
                    clicksAllow = 1;
                    timeSinceLastPerfect.reset();
                }

                preAttackAutoBlock();
                for(int i = 0; i < clicksAllow; i++){
                    if(targetMode.is("Multi")){
                        for(EntityLivingBase target : targetList){
                            if(PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) <= attackDistance.getValue()){
                                attackEntity(target);
                            }
                        }
                    }else{
                        attackEntity(target);
                    }

                    timeSinceLastClick.reset();
                }
                postAttackAutoBlock();
            }
        }else{
            onNoTargetOrCleanup();
        }
    }

    private void preAttackAutoBlock(){
    }

    private void postAttackAutoBlock(){
        switch (autoBlockMode.getValue()){
            case "Grim" -> {
                BlinkComponent.onDisable();
                blockSword();
                BlinkComponent.onEnable();
                mc.playerController.syncCurrentPlayItem();
                int old = mc.thePlayer.inventory.currentItem;
                mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.currentItem % 8 + 1;
                PacketUtil.sendPacket(new C02PacketUseEntity(target, hitVec3.subtract(target.posX, target.posY, target.posZ)));
                mc.playerController.syncCurrentPlayItem();
                mc.thePlayer.inventory.currentItem = old;
                isBlocking = true;
                isBlinking = true;
                unblockSword();
            }
            case "Vanilla" -> {
                blockSword();
                isBlocking = true;
            }
            case "Bug" -> {
                PacketUtil.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                blockSword();
                isBlocking = true;
                PacketUtil.sendPacket(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            }
            case "Vulcan" -> {
                PacketUtil.sendPacket(new C02PacketUseEntity(target, hitVec3.subtract(target.posX, target.posY, target.posZ)));
                PacketUtil.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                blockSword();
                isBlocking = true;
            }
        }
    }

    private void blockSword() {
        if (!isBlocking && mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword) {
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
        }
        isBlocking = true;
    }
    private void hypixelblockSword() {
        if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword) {
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
        }
        isBlocking = true;
    }


    private boolean breakOnNextTick = false;
    private boolean lookAtBedPlease = false;

    private boolean tickKATicksWatchdog(){
        Breaker breaker = Ambient.getInstance().getModuleManager().getModule(Breaker.class);
        if(breaker.isEnabled() && breaker.breakPos != null){
            if(breaker.blockDamage == 0f && breaker.blockDamageCD == 0) {
                lookAtBedPlease = true;
                if (isBlinking) {
                    BlinkComponent.onDisable();
                    isBlinking = false;
                }
                breaker.startBreak();
                breakOnNextTick = false;
                killAuraTicks = -1;
                return true;
            }
            if(breaker.blockDamage >= 1f) {
                lookAtBedPlease = true;
                BlinkComponent.onDisable();
                isBlinking = false;
                breaker.stopBreak();
                killAuraTicks = -1;
                breakOnNextTick = false;
                return true;
            }
        }
        killAuraTicks++;
        return false;
    }



    private void swing(){
        if(swingMult.isSelected("Server")){
            PacketUtil.sendPacket(new C0APacketAnimation());
        }
        if(swingMult.isSelected("Client")){
            mc.thePlayer.swingItemClientSide();
        }
    }

    private void attackEntity(EntityLivingBase entityLivingBase){
        if (ViaLoadingBase.getInstance().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            swing();
            sendAttack(entityLivingBase);
        } else {
            sendAttack(entityLivingBase);
            swing();
        }
        critParticle();
    }

    private void sendAttack(EntityLivingBase entityLivingBase){
        if(keepSprint.getValue()){
            final AttackEvent event = new AttackEvent(entityLivingBase);
            Ambient.getInstance().getEventBus().post(event);

            mc.thePlayer.motionX *= keepSprintAmount.getValue();
            mc.thePlayer.motionZ *= keepSprintAmount.getValue();
            PacketUtil.sendPacket(new C02PacketUseEntity(entityLivingBase, C02PacketUseEntity.Action.ATTACK));
        }else {
            mc.playerController.attackEntity(mc.thePlayer, entityLivingBase);
        }


        switch (interactAfterAttack.getValue()){
            case "Interact" -> PacketUtil.sendPacket(new C02PacketUseEntity(entityLivingBase, C02PacketUseEntity.Action.INTERACT));
            case "Interact-At" -> PacketUtil.sendPacket(new C02PacketUseEntity(entityLivingBase, hitVec3.subtract(target.posX, target.posY, target.posZ)));
            case "Full" -> {
                PacketUtil.sendPacket(new C02PacketUseEntity(entityLivingBase, hitVec3.subtract(target.posX, target.posY, target.posZ)));
                PacketUtil.sendPacket(new C02PacketUseEntity(entityLivingBase, C02PacketUseEntity.Action.INTERACT));
            }
        }

    }





    @SubscribeEvent
    private void onNetworkTickEvent(PreMotionEvent event){



        boolean scaffold = Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled();

        if(onlyOnSword.getValue()) {
            if (mc.thePlayer.inventory.getCurrentItem() == null || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword)) {
                return;
            }
        }
        target = getTarget();



        ticc++;
        lastYaw = yaw;
        lastPitch = pitch;
        if(target != null && !scaffold){
            hitVec3 = getHitVector(target);
            float[] rotations = getRotations(hitVec3);
            MovingObjectPosition movingObjectPosition = RotationUtil.getMouseEntity(1f,PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) , new float[]{yaw, pitch});





            if(lookAtBedPlease){

                Breaker breaker = Ambient.getInstance().getModuleManager().getModule(Breaker.class);
                if(breaker.isEnabled() && breaker.breakPos != null){
                    float[] rotationsToBlock = BlockUtil.getRotationToBlockDirect(new PosFace(breaker.breakPos, EnumFacing.UP));
                    yaw = rotationsToBlock[0];
                    pitch = rotationsToBlock[1];
                    Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
                    Ambient.getInstance().getRotationComponent().setRotations(yaw, pitch, getMoveCorrectFromSetting());
                    lookAtBedPlease = false;

                    return;
                }

            }


            if (PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) < onlyOnNeededDistance.getValue() && movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY){
                Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
                Ambient.getInstance().getRotationComponent().setRotations(yaw, pitch, getMoveCorrectFromSetting());
                return;
            }





            rotations = RotationUtil.applySensitivity(rotations, new float[]{yaw, pitch});

            yaw = rotations[0];
            pitch = rotations[1];

            lastHitVec = currentHitVec;

            Vec3 vec31 = mc.thePlayer.getVectorForRotation(pitch,yaw);
            Vec3 vec3 = mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks);
            float distance = PlayerUtil.getBiblicallyAccurateDistanceToEntity(target);
            currentHitVec = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);




            Ambient.getInstance().getRotationComponent().setActive(true, this.getClass());
            Ambient.getInstance().getRotationComponent().setRotations(yaw, pitch, getMoveCorrectFromSetting());
        }else{
            Ambient.getInstance().getRotationComponent().setActive(false, this.getClass());
            // empty since not rn pretty please
        }
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

    private int getAllowedClicks(boolean preSwing){
        clickUtil.setUsePattern(clickFreqMode.is("Pattern"));
        clickUtil.setSinLoopAmount(clickRandomizationSLoop.getValue());
        clickUtil.setSinLoopDivisor(clickRandomizationSLoopTime.getValue());
        clickUtil.setMinCps(cpsMin.getValue());
        clickUtil.setMaxCps(cpsMax.getValue());

        if(preSwing){
            return clickUtil.isAbleToClick();
        }


        switch (clickFreqMode.getValue()){
            case "CPS", "Pattern" -> {
                return clickUtil.isAbleToClick();
            }
            case "Perfect" -> {
                return timeSinceLastClick.finished(500) ? 1 : 0;
            }
        }
        return 0;
    }

    public float[] getRotations(Vec3 vec3){
        float deltaYaw = 0;
        float deltaPitch = 0;



        float[] rotationDifference = RotationUtil.getRotationDifference(new Vec3(mc.thePlayer), vec3, yaw, pitch);
        switch (rotationMode.getValue()){
            case "Direct" -> {
                deltaYaw = rotationDifference[0];
                deltaPitch = rotationDifference[1];
            }
            case "Smooth" -> {
                deltaYaw = rotationDifference[0] / 1.5f;
                deltaPitch = rotationDifference[1] / 1.5f;
            }
            case "Smooth2" -> {
                deltaYaw = MathHelper.clamp_float(rotationDifference[0] / 2, -lastYawDelta * 2f, lastYawDelta * 2f);
                deltaPitch = MathHelper.clamp_float(rotationDifference[1] / 2, -lastPitchDelta * 1.6f, lastPitchDelta * 1.6f);
            }
            case "Smooth3" -> {

                if(target.hurtTime < 2 && target.hurtTime != 0 && PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) < 3.2f){
                    deltaYaw = rotationDifference[0] / 1.1f;
                    deltaPitch = rotationDifference[1] / 1.1f;
                }else{
                    deltaYaw = (float) (rotationDifference[0] / 2f);
                    deltaPitch = (float) (rotationDifference[1] / 2f);
                }



            }
            case "Pattern" -> {
                if(Ambient.getInstance().getRotationPatternComponent().usableInProd.isEmpty()){
                    ChatUtil.display("PLEASE SELECT A ROTATION PATTERN");
                    return new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
                }

                ArrayList<float[]> hitList = new ArrayList<>();

                Situation situation = Situation.PRE_AIM;
                if(PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) <= attackDistance.getValue()){
                    situation = Situation.ON_TARGET;
                }

                hitList = Ambient.getInstance().getRotationPatternComponent().usableInProd.get(situation);

                float[] closestDifference = MathHelper.findClosest(hitList, rotationDifference);

                float[] multiplier = new float[]{MathHelper.clamp_float(rotationDifference[0], -1, 1),MathHelper.clamp_float(rotationDifference[1], -1, 1)};


                deltaYaw = closestDifference[0] * multiplier[0];
                deltaPitch = closestDifference[1] * multiplier[1];


            }
        }

        //deltaYaw = MathHelper.wrapAngleTo180_float(deltaYaw);


        switch (rotationLimiterMode.getValue()){
            case "Static" -> {
                deltaYaw = MathHelper.clamp_float(deltaYaw,-rotationLimiterStaticYaw.getValue(), rotationLimiterStaticYaw.getValue());
                deltaPitch = MathHelper.clamp_float(deltaPitch,-rotationLimiterStaticPitch.getValue(), rotationLimiterStaticPitch.getValue());
            }
            case "DistDep" -> {
                float distanceWise = (10 - PlayerUtil.getBiblicallyAccurateDistanceToEntity(target)) * 3 * rotationLimiterDistDepMultiplier.getValue();
                float distanceWisePitch = distanceWise / 3.5f;

                deltaYaw = MathHelper.clamp_float(deltaYaw,-distanceWise, distanceWise);
                deltaPitch = MathHelper.clamp_float(deltaPitch,-distanceWisePitch, distanceWisePitch);
            }
        }

        //deltaYaw = MathHelper.wrapAngleTo180_float(deltaYaw);

        switch (rotationRandomizationMode.getValue()){
            case "MathRandom" -> {
                deltaYaw += randomVal() * rotationRandomMathRandomYaw.getValue();
                deltaPitch += randomVal() * rotationRandomMathRandomPitch.getValue();
            }
            case "Noise" -> {
                deltaYaw += fastNoiseLite.GetNoise(mc.thePlayer.ticksExisted * 2, 0) * rotationRandomNoiseMultiplier.getValue();
                deltaPitch += fastNoiseLite.GetNoise(0, mc.thePlayer.ticksExisted * 2) * rotationRandomNoiseMultiplier.getValue();
            }
        }
        //deltaYaw = MathHelper.wrapAngleTo180_float(deltaYaw);



        lastYawDelta = Math.abs(deltaYaw);
        lastPitchDelta = Math.abs(deltaPitch);

        lastYawDelta = Math.max(lastYawDelta, 1);
        lastPitchDelta = Math.max(lastPitchDelta, 1);


        float realYaw = yaw - deltaYaw;
        float realPitch = MathHelper.clamp_float(pitch - deltaPitch, -90, 90);



        //ChatUtil.display(Math.abs(fuckedPredictedYaw - lastFuckedPredictedYaw));

        return new float[]{realYaw, realPitch};
    }

    public float randomVal(){
        return (float) (-1 + (2 * Math.random()));
    }


    public Vec3 getHitVector(EntityLivingBase enemy){
        Vec3 vector = null;

        switch (rotationHitVecMode.getValue()){
            case "Best" -> vector = PlayerUtil.getClosestPointToEntity(enemy, rotationHitVecBestMargin.getValue());
            case "Head" -> vector = new Vec3(enemy.posX, enemy.posY + enemy.getEyeHeight(), enemy.posZ);
            case "Body" -> vector = new Vec3(enemy.posX, enemy.posY + enemy.height / 2, enemy.posZ);
            case "Legs" -> vector = new Vec3(enemy.posX, enemy.posY + enemy.height / 4, enemy.posZ);
            case "Test" -> vector = PlayerUtil.getPredictedPointOnEntity(enemy, rotationHitVecBestMargin.getValue());
        }

        vector = vector.addVector(enemy.motionX * rotationHitVecPredictionAmount.getValue(),
                enemy.motionY * rotationHitVecPredictionAmount.getValue(),
                enemy.motionZ * rotationHitVecPredictionAmount.getValue());

        return vector;
    }

    @SneakyThrows
    public EntityLivingBase getTarget(){
        targetList.clear();


        for(Entity entity : mc.theWorld.loadedEntityList){

            if(entity instanceof EntityLivingBase entityLivingBase && entity != mc.thePlayer){

                boolean shouldAdd = false;

                if(PlayerUtil.getBiblicallyAccurateDistanceToEntity(entityLivingBase) > rotationDistance.getValue()){
                    continue;
                }
                if(Ambient.getInstance().getModuleManager().getModule(AntiBot.class).isEnabled() && Ambient.getInstance().getModuleManager().getModule(AntiBot.class).isBot(entityLivingBase)){
                    continue;
                }
                if(Ambient.getInstance().getModuleManager().getModule(MCF.class).isEnabled() && entity instanceof EntityPlayer entityPlayer && Ambient.getInstance().getModuleManager().getModule(MCF.class).isFriend(entityPlayer.getGameProfile())){
                    continue;
                }

                if(targetMultiProp.isSelected("Players") && entityLivingBase instanceof EntityPlayer){
                    shouldAdd = true;
                }
                if(targetMultiProp.isSelected("Hostile") && (entityLivingBase instanceof EntityMob || entityLivingBase instanceof EntitySlime)){
                    shouldAdd = true;
                }
                if(targetMultiProp.isSelected("Passive") && (entityLivingBase instanceof EntityAnimal || entityLivingBase instanceof EntityVillager)){
                    shouldAdd = true;
                }

                if(!shouldAdd){
                    continue;
                }

                if(!targetMultiEffects.isSelected("Target Invisible") && entityLivingBase.isInvisible()){
                    continue;
                }
                if(!targetMultiEffects.isSelected("Target Behind Wall") && !mc.thePlayer.canEntityBeSeen(entityLivingBase)){
                    continue;
                }
                if(targetMultiEffects.isSelected("Racism") && entityLivingBase instanceof EntityPlayer pl &&  BlackDetectorComponent.getBlackAndBrownPercentageAsync(pl).get().intValue() < 50){
                    continue;
                }
                if(!targetMultiEffects.isSelected("Target Team") && PlayerUtil.isEntityTeamSameAsPlayer(entityLivingBase)){
                    continue;
                }
                if(targetMultiEffects.isSelected("Check If Dead") && entityLivingBase.getHealth() <= 0){
                    continue;
                }

                targetList.add(entityLivingBase);
            }
        }


        targetList.sort((f1, f2) -> Float.compare(PlayerUtil.getBiblicallyAccurateDistanceToEntity(f1), PlayerUtil.getBiblicallyAccurateDistanceToEntity(f2)));

        switch (targetSorting.getValue()){
            case "Distance" -> targetList.sort((f1, f2) -> Float.compare(PlayerUtil.getBiblicallyAccurateDistanceToEntity(f1), PlayerUtil.getBiblicallyAccurateDistanceToEntity(f2)));
            case "HurtTime" -> targetList.sort((f1, f2) -> Float.compare(f1.hurtTime, f2.hurtTime));
            case "Health" -> targetList.sort((f1, f2) -> Float.compare(f1.getHealth(), f2.getMaxHealth()));
        }


        switch (targetMode.getValue()){
            case "Single" -> {
                if(targetList.isEmpty()){
                    return null;
                }
                if(target == null || PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) > rotationDistance.getValue()){
                    return targetList.getFirst();
                }else {
                    return target;
                }
            }
            case "Switch" -> {

                if(switchindex >= targetList.size()){
                    switchindex = 0;
                }
                if(targetList.isEmpty()){
                    return null;
                }

                EntityLivingBase tempTarget = targetList.get(switchindex);

                if(ticc % switchDelay.getValue().intValue() == 0){
                    switchindex++;
                }

                return tempTarget;
            }
            case "Multi" -> {
                if(targetList.isEmpty()){
                    return null;
                }
                return targetList.get(0);
            }

        }



        if(!targetList.isEmpty()){
            return targetList.get(0);
        }
        return null;
    }


}