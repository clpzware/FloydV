package fr.ambient.module.impl.combat;

import fr.ambient.Ambient;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.client.ClientPreRunTickEvent;
import fr.ambient.event.impl.client.ClientRunTickEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.Property;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.CompositeProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.player.PlayerUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.awt.*;

public class TickBase extends Module {
    public TickBase() {
        super(97, ModuleCategory.COMBAT);
        registerProperties(moveOption,targetting, targetDistanceMax, targetDistanceMin, amount, hurtTime, cancelTicks, cooldown,onlyNeccesary);

        this.setDraggable(true);
        this.setWidth(100);
        this.setHeight(25);

        this.setX(100);
        this.setY(100);


    }

    public BooleanProperty attackKillaura = BooleanProperty.newInstance("KillAura", true);
    public BooleanProperty attackPointed = BooleanProperty.newInstance("Cursor", false);
    public BooleanProperty attackTeams = BooleanProperty.newInstance("Team", false, ()->attackPointed.getValue());
    public CompositeProperty targetting = CompositeProperty.newInstance("Target", new Property[]{attackKillaura, attackPointed, attackTeams}, ()->true);
    public NumberProperty targetDistanceMax = NumberProperty.newInstance("Target Distance Max", 0f,3.4f, 6f, 0.1f);
    public NumberProperty targetDistanceMin = NumberProperty.newInstance("Target Distance Min", 0f,3f, 6f, 0.1f);
    public NumberProperty amount = NumberProperty.newInstance("Ticks To Skip", 0f, 1f, 10f, 1f);
    public NumberProperty hurtTime = NumberProperty.newInstance("HurtTime to start", 0f, 0f, 10f, 1f);
    public NumberProperty cancelTicks = NumberProperty.newInstance("Ticks to cancel", 0f, 4f, 10f, 1f);
    public NumberProperty cooldown = NumberProperty.newInstance("Cooldown",  50f,750f, 3000f, 10f);

    public ModeProperty moveOption = ModeProperty.newInstance("MoveMode", new String[]{"Only Up", "Only Down", "Always"}, "Always");


    public BooleanProperty onlyNeccesary = BooleanProperty.newInstance("Only Neccesary", false);


    public EntityLivingBase target = null;
    private boolean flag = false;
    private boolean doTick = false;
    private int tickCounter = 0;
    private final TimeUtil timer = new TimeUtil();

    private AxisAlignedBB lastBoundBox = null;

    private int charged = 0;


    @SubscribeEvent
    private void onRender2D(Render2DEvent event){
        RenderUtil.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), new Color(40,40,40,40));
        Fonts.getOpenSansMedium(24).drawString(charged + " ticks charged", this.getX() + 2, this.getY() + 2, Color.WHITE.getRGB());
    }

    @SubscribeEvent
    private void onPreTickEvent(ClientPreRunTickEvent event){
        if (target == null) {
            doTick = false;
            tickCounter = 0;
            lastBoundBox = null;
            return;
        }




        if (doTick) {
            if (tickCounter > 0) {

                if ((target.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) > targetDistanceMax.getValue() || target.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) < targetDistanceMin.getValue()) || !canAttack(target)) {
                    tickCounter = 0;
                    doTick = false;
                } else {
                    if(playerGoingToTarget(target) && nextTickTarget3() && predictTargetMovement()){
                        event.setCancelled(true);
                        tickCounter--;
                    }
                }
            }
        }
    }

    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMoveInput(MoveInputEvent event){
        if(tickCounter < 0 && target != null && PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) < 3.0){
            event.setForward(0f);
        }
    }

    @SubscribeEvent
    private void onTickEvent(ClientRunTickEvent event){
        target = null;
        if (mc.pointedEntity instanceof EntityLivingBase) {
            if (((EntityLivingBase) mc.pointedEntity).hurtTime == hurtTime.getValue().intValue()) {
                target = (EntityLivingBase) mc.pointedEntity;
            }
        }
        if (KillAura.target != null) {
            if (KillAura.target.hurtTime == hurtTime.getValue()) {
                target = KillAura.target;
            }
        }

        if (!MoveUtil.moving()) {
            target = null;
        }

        if (timer.finished(cooldown.getValue().longValue()) && target != null) {
            doTick = true;
            tickCounter = amount.getValue().intValue();
            timer.reset();
        }

        if(target != null){
            lastBoundBox = target.getEntityBoundingBox();
        }


        if (doTick) {
            if (tickCounter <= 0) {
                if (!flag) {
                    try {
                        for (int i = 0; i < amount.getValue(); i++) {
                            if(playerGoingToTarget(target)){
                                flag = true;
                                mc.runTick(true);
                                if(PlayerUtil.getBiblicallyAccurateDistanceToEntity(target) <= 2.9 && nextTickTarget3()){
                                    flag = false;
                                    break;
                                }
                            }else{
                                flag = false;
                                doTick = false;
                            }
                        }
                        doTick = false;
                    } catch (Exception exception) {
                    }
                } else {
                    flag = false;
                }
            }
        }
    }
    public boolean canAttack(EntityLivingBase entityLivingBase) {

        if (entityLivingBase == mc.thePlayer) {
            return false;
        }
        if (entityLivingBase.getHealth() <= 0f) {
            return false;
        }
        return !Ambient.getInstance().getModuleManager().getModule(AntiBot.class).isBot(entityLivingBase) || !Ambient.getInstance().getModuleManager().getModule(AntiBot.class).isEnabled();
    }
    public boolean shouldTickForw(){
        switch (moveOption.getValue()){
            case "Always" -> {
                return true;
            }
            case "Only Up" -> {
                return mc.thePlayer.motionY > 0;
            }
            case "Only Down" -> {
                return mc.thePlayer.motionY < 0;
            }
        }
        return true;
    }


    public boolean nextTickTarget3(){
        if(target == null || !onlyNeccesary.getValue()){
            return true;
        }
        double lastTDis = PlayerUtil.getDistanceToBoundingBox(lastBoundBox);
        double targetD = PlayerUtil.getBiblicallyAccurateDistanceToEntity(target);
        double diff = lastTDis - targetD;

        return diff > 0 && targetD - diff < 3;
    }
    public boolean predictTargetMovement(){
        /*if(target == null || !onlyNeccesary.getValue()){
            return true;
        }
        double pX = mc.thePlayer.posX + mc.thePlayer.motionX, pY = mc.thePlayer.posY, pZ = mc.thePlayer.posZ;

        AxisAlignedBB targetNextBoundingBox = target.getEntityBoundingBox().addCoord(target.motionX, target.motionY, target.motionZ);
        AxisAlignedBB selfNextBoundingBox = mc.thePlayer.getEntityBoundingBox().addCoord(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);


        if(PlayerUtil.getClosestPointToBoundingBox(targetNextBoundingBox).distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ)) > 3.1){
            return true;
        }
        return false;*/
        return true;

    }


    public boolean playerGoingToTarget(Entity target){
        if(!onlyNeccesary.getValue()){
            return true;
        }

        Vec3 lastPlayerPos = new Vec3(mc.thePlayer.lastTickPosX, 0, mc.thePlayer.lastTickPosZ);
        Vec3 playerPos = new Vec3(mc.thePlayer.posX, 0, mc.thePlayer.posZ);

        Vec3 targetPos = new Vec3(target.posX, 0, target.posZ);

        return targetPos.distanceTo(lastPlayerPos) > targetPos.distanceTo(playerPos);


    }

}
