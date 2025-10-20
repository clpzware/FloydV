package fr.ambient.module.impl.combat;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.Property;
import fr.ambient.property.impl.*;
import fr.ambient.util.FastNoiseLite;
import fr.ambient.util.player.PlayerUtil;
import fr.ambient.util.player.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.util.Vec3;
import org.lwjglx.input.Mouse;

import java.util.Arrays;
import java.util.HashSet;

public class AimAssist extends Module {
    public AimAssist() {
        super(98, ModuleCategory.COMBAT);
        registerProperties(aimPoint,aimPointBestMarg, aimSpeedGroup, noAimWhileLooking,randomGroups,distance, aimTo, checks);
    }

    private final ModeProperty aimPoint = ModeProperty.newInstance("Aim Point", new String[]{"Best", "Head", "Body", "Legs", "BestCenter"}, "Best");
    private final NumberProperty aimPointBestMarg = NumberProperty.newInstance("Margin", -0.5f,0f, 0.5f, 0.1f, ()->aimPoint.is("Best"));




    private final NumberProperty aimSpeedYawMax = NumberProperty.newInstance("Aim Yaw Max", 0f,9f, 10f, .1f);
    private final NumberProperty aimSpeedYawMin = NumberProperty.newInstance("Aim Yaw Min", 0f,8f, 10f, .1f);
    private final NumberProperty aimSpeedPitchMax = NumberProperty.newInstance("Aim Pitch Max", 0f,9f, 10f, .1f);
    private final NumberProperty aimSpeedPitchMin = NumberProperty.newInstance("Aim Pitch Min", 0f,7f, 10f, .1f);

    private final CompositeProperty aimSpeedGroup = CompositeProperty.newInstance("Aim Speed", new Property[]{aimSpeedYawMax, aimSpeedYawMin, aimSpeedPitchMax, aimSpeedPitchMin}, ()->true);


    private final ModeProperty randomization = ModeProperty.newInstance("Randomization", new String[]{"None", "Noise", "MathRandom", "SinWave", "YawAdapt"}, "None");

    private final NumberProperty yawMultiplier = NumberProperty.newInstance("Yaw Random Multiplier", 0f, 1f, 10f, 0.1f, ()->!randomization.is("None"));
    private final NumberProperty pitchMultiplier = NumberProperty.newInstance("Pitch Random Multiplier", 0f, 1f, 10f, 0.1f, ()->!randomization.is("None"));
    private CompositeProperty randomGroups = CompositeProperty.newInstance("Randomization", new Property[]{randomization, yawMultiplier, pitchMultiplier});

    private final BooleanProperty noAimWhileLooking = BooleanProperty.newInstance("Not while already looking", false);


    private final NumberProperty distance = NumberProperty.newInstance("Distance", 0f,4.5f, 6f, 0.1f);


    private final MultiProperty aimTo = MultiProperty.newInstance("Targets", new String[]{"Players", "Hostile", "Passive"}, new HashSet<>(Arrays.asList("Players")));
    private final MultiProperty checks = MultiProperty.newInstance("Checks", new String[]{"Check if team", "Not On Bot", "Only when holding sword", "Only on Click", "Only when mouse move"}, new HashSet<>(Arrays.asList("Check if team", "Check if bot  ( AntiBot Req )")));

    public EntityLivingBase target = null;

    private FastNoiseLite fnl = new FastNoiseLite(133769420);

    @SubscribeEvent
    private void onPlayerTick(UpdateEvent event){

        target = getTarget();

        if(target != null){
            vec = getVec3();
            if(vec != null){
                if(mc.pointedEntity != null && noAimWhileLooking.getValue()){
                    return;
                }
                float[] addRotations = RotationUtil.getRotationDifference(new Vec3(mc.thePlayer), vec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                float yawDivisor = 11 - (getRandomInterm(Math.min(aimSpeedYawMin.getValue(), aimSpeedYawMax.getValue()),Math.max(aimSpeedYawMin.getValue(), aimSpeedYawMax.getValue())));
                float pitchDivisor = 11 - (getRandomInterm(Math.min(aimSpeedPitchMin.getValue(), aimSpeedPitchMax.getValue()),Math.max(aimSpeedPitchMin.getValue(), aimSpeedPitchMax.getValue())));
                float[] toPutRotations = new float[]{mc.thePlayer.rotationYaw - addRotations[0] / (yawDivisor),
                                                    mc.thePlayer.rotationPitch - addRotations[1] / ( pitchDivisor)};

                float[] randomizedRotations = additionalRandomization();

                toPutRotations[0] += randomizedRotations[0] * yawMultiplier.getValue();
                toPutRotations[1] += randomizedRotations[1] * pitchMultiplier.getValue();

                float[] fixed = RotationUtil.applySensitivity(toPutRotations, new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch});

                mc.thePlayer.rotationYaw = fixed[0];
                mc.thePlayer.rotationPitch = fixed[1];
                Ambient.getInstance().getRotationComponent().rotationYaw = mc.thePlayer.rotationYaw;
                Ambient.getInstance().getRotationComponent().rotationPitch = mc.thePlayer.rotationPitch;
            }
        }
    }


    private float getRandomInterm(float min, float max){
        return (float) (min + (max - min) * Math.random());
    }

    public Vec3 vec = null;

    public Vec3 getVec3(){
        switch (aimPoint.getValue()){
            case "Best" -> {
                return PlayerUtil.getClosestPointToEntity(target, aimPointBestMarg.getValue());
            }
            case "Head" -> {
                return new Vec3(target);
            }
            case "Body" -> {
                return new Vec3(target.posX, target.posY + target.height / 2, target.posZ);
            }
            case "Legs" -> {
                return new Vec3(target.posX, target.posY + target.height / 3, target.posZ);
            }
            case "BestCenter" -> {
                return new Vec3(target.posX, PlayerUtil.getClosestPointToEntity(target).yCoord, target.posZ);
            }
        }
        return null;
    }


    public float[] additionalRandomization(){

        // note
        // all of those should be contained between -1 and 1, since all this code is being deleted. peace <3
        // also randomization vals should be yaw / pitch

        switch (randomization.getValue()){
            case "None" -> {
                return new float[]{0,0};
            }
            case "Noise" -> {
                float val = fnl.GetNoise((float) (mc.thePlayer.posX * 10), (float) (mc.thePlayer.posZ * 10));

                System.out.println(val);

                return new float[]{-0.5f + val, -0.5f + val};
            }
            case "MathRandom" -> {
                return new float[]{(float) Math.random(), (float) Math.random()};
            }
            case "SinWave" -> {
                return new float[]{0, (float) Math.sin(System.currentTimeMillis() / 250f)};
            }
            case "YawAdapt" -> {
                return new float[]{0, (float) Math.sin(Math.toRadians(mc.thePlayer.rotationYaw))};
            }
        }
        return null;
    }

    public EntityLivingBase getTarget(){
        EntityLivingBase temp = null;
        for(Entity entity : mc.theWorld.loadedEntityList){
            if(entity instanceof EntityLivingBase entityLivingBase && entityLivingBase != mc.thePlayer && canEntity(entityLivingBase) && PlayerUtil.getBiblicallyAccurateDistanceToEntity(entityLivingBase) <= distance.getValue()) {
                if (temp == null) {
                    temp = entityLivingBase;
                } else {
                    if (PlayerUtil.getBiblicallyAccurateDistanceToEntity(temp) > PlayerUtil.getBiblicallyAccurateDistanceToEntity(entityLivingBase)) {
                        temp = entityLivingBase;
                    }
                }
            }
        }
        return temp;
    }


    private boolean canEntity(EntityLivingBase base){

        if(!aimTo.isSelected("Players") && base instanceof EntityPlayer){
            return false;
        }
        if(!aimTo.isSelected("Hostile") && (base instanceof EntityMob || base instanceof EntitySlime)){
            return false;
        }
        if(!aimTo.isSelected("Passive") && (base instanceof EntityAnimal || base instanceof EntityVillager)){
            return false;
        }

        if(checks.isSelected("Check if team") && PlayerUtil.isEntityTeamSameAsPlayer(base)){
            return false;
        }
        if (mc.currentScreen != null) {
            return false;
        }
        if (base instanceof EntityArmorStand) {
            return false;
        }
        if(checks.isSelected("Check if bot") && Ambient.getInstance().getModuleManager().getModule(AntiBot.class).isEnabled() && Ambient.getInstance().getModuleManager().getModule(AntiBot.class).isBot(base)){
            return false;
        }
        if(checks.isSelected("Only when holding sword") && !(mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword)){
            return false;
        }
        if(checks.isSelected("Only when mouse move") && !(mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword)){
            return false;
        }
        return !checks.isSelected("Only on Click") || Mouse.isButtonDown(0);
    }


}
