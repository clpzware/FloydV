package fr.ambient.module.impl.combat;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.Property;
import fr.ambient.property.impl.CompositeProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.MultiProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.ClickUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import org.lwjglx.input.Mouse;

import java.util.Arrays;
import java.util.HashSet;

public class AutoClicker extends Module {

    public AutoClicker() {
        super(1,"Automatically clicks for you during combat.", ModuleCategory.COMBAT);
        this.registerProperties(mode, cpsMax, cpsMin, clickRandomizationSLoop, clickRandomizationSLoopTime,checks,entityChecks,blockGroup);
    }

    public ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Normal", "Pattern"}, "Normal");

    private final NumberProperty cpsMax = NumberProperty.newInstance("Max CPS", 0f, 16f, 20f, 0.1f, ()->mode.is("Normal"));
    private final NumberProperty cpsMin = NumberProperty.newInstance("Min CPS", 0f, 12f, 20f, 0.1f, ()->mode.is("Normal"));
    private final NumberProperty clickRandomizationSLoop = NumberProperty.newInstance("Random Multiplier", 0.1f, 1f, 5f, 0.1f, ()->mode.is("Normal"));
    private final NumberProperty clickRandomizationSLoopTime = NumberProperty.newInstance("Random Loop Time", 50f, 500f, 5000f, 50f, ()->mode.is("Normal"));

    private final MultiProperty checks = MultiProperty.newInstance("Checks", new String[]{"Only on Entity", "Only on Click", "Not while looking at block", "Not while Using"}, new HashSet<>(Arrays.asList("Not while looking at block")));

    private final MultiProperty entityChecks = MultiProperty.newInstance("Entity Checks", new String[]{"Check if bot ( AntiBot req )", "Check if team"}, new HashSet<>(Arrays.asList("Check if bot")), ()->checks.isSelected("Only on Entity"));

    private final ModeProperty autoBlock = ModeProperty.newInstance("AutoBlock", new String[]{"None", "Normal", "Prevent"}, "None");
    private final NumberProperty blockPercentage = NumberProperty.newInstance("Block Percentage", 0f, 50f, 100f, 1f, ()->autoBlock.is("Normal"));
    private final MultiProperty blockCheck = MultiProperty.newInstance("Block Checks", new String[]{"Only on Entity", "Only in Air"}, new HashSet<>(Arrays.asList("Only on Entity")));






    private final CompositeProperty blockGroup = CompositeProperty.newInstance("Block Group", new Property[]{autoBlock, blockPercentage, blockCheck}, ()->true);

    private final ClickUtil clickUtil = new ClickUtil();

    private boolean lastTickWasBlocking = false;


    @SubscribeEvent
    private void onUpdate(UpdateEvent event){
        clickUtil.setUsePattern(mode.is("Pattern"));
        clickUtil.setSinLoopAmount(clickRandomizationSLoop.getValue());
        clickUtil.setSinLoopDivisor(clickRandomizationSLoopTime.getValue());
        clickUtil.setMinCps(cpsMin.getValue());
        clickUtil.setMaxCps(cpsMax.getValue());
        boolean hasClicked = false;

        if(ableToClick() && !lastTickWasBlocking){
            int a = clickUtil.isAbleToClick();
            if(a > 0){
                mc.clickMouse();
            }
            if(autoBlock.is("Normal") && hasClicked){
                if(blockPercentage.getValue() / 100f > Math.random() && canRightClick()){
                    mc.rightClickMouse();
                }
            }
        }

        lastTickWasBlocking = mc.thePlayer.isUsingItem() || mc.thePlayer.isBlocking();

    }


    public boolean ableToClick(){
        if(checks.isSelected("Only on Entity") && !(mc.pointedEntity instanceof EntityLivingBase)){
            return false;
        }
        if(checks.isSelected("Only on Click") && !Mouse.isButtonDown(0)){
            return false;
        }
        if(checks.isSelected("Not while looking at block") && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
            return false;
        }
        if(checks.isSelected("Not while Using") && (mc.thePlayer.isUsingItem() || mc.thePlayer.isBlocking())){
            return false;
        }
        if (mc.currentScreen != null) {
            return false;
        }


        return !checks.isSelected("Only on Entity") || !(mc.pointedEntity instanceof EntityLivingBase entityLivingBase) || canEntity(entityLivingBase);
    }
    public boolean canRightClick(){
        if(blockCheck.isSelected("Only on Entity") && mc.pointedEntity == null){
            return false;
        }
        return !blockCheck.isSelected("Only in Air") || !mc.thePlayer.onGround;
    }

    public boolean canEntity(Entity entity){
        if(entityChecks.isSelected("Check if bot ( AntiBot req )") && entity instanceof EntityLivingBase entityLivingBase && Ambient.getInstance().getModuleManager().getModule(AntiBot.class).isEnabled() && Ambient.getInstance().getModuleManager().getModule(AntiBot.class).isBot(entityLivingBase)){
            return false;
        }
        return !entityChecks.isSelected("Check if team") || !(entity instanceof EntityLivingBase entityLivingBase) || !PlayerUtil.isEntityTeamSameAsPlayer(entityLivingBase);
    }
}
