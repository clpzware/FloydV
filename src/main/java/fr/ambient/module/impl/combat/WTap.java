package fr.ambient.module.impl.combat;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import net.minecraft.entity.EntityLivingBase;

public class WTap extends Module {

    public final ModeProperty bMode = ModeProperty.newInstance("Mode", new String[]{"Legit input", "Legit sprint", "Ion sprint"}, "Ion sprint");

    public WTap() {
        super(73,"Times your movements to hit enemies with extra knockback.", ModuleCategory.COMBAT);
        this.registerProperties(bMode);
    }

    public void onEnable() {
    }

    public void onDisable() {

    }

    @SubscribeEvent
    private void onPlayerTick(UpdateEvent event) {
        EntityLivingBase livingBase = null;
        if(mc.pointedEntity instanceof EntityLivingBase entityLivingBase){
            livingBase = entityLivingBase;
        }
        if(KillAura.target != null){
            livingBase = KillAura.target;
        }
        switch (bMode.getValue()){
            case "Ion sprint" -> {
                if (mc.thePlayer.ticksExisted - mc.thePlayer.getLastAttackerTime() == 1 && mc.thePlayer.ticksExisted > 10 && mc.thePlayer.getLastAttacker().getHurtTime() == 0) {
                    mc.thePlayer.setSprinting(false);
                }
            }

            case "Legit sprint" -> {
                if (livingBase != null && livingBase.hurtTime == 9) {
                    mc.thePlayer.setSprinting(false);
                }
            }
        }
    }
    @SubscribeEvent
    private void onPlayerTick(MoveInputEvent event) {
        EntityLivingBase livingBase = null;
        if(mc.pointedEntity instanceof EntityLivingBase entityLivingBase){
            livingBase = entityLivingBase;
        }
        if(KillAura.target != null){
            livingBase = KillAura.target;
        }
        switch (bMode.getValue()){
            case "Legit input" -> {
                if (livingBase != null && livingBase.hurtTime == 9) {
                    mc.thePlayer.movementInput.moveForward = 0;
                }
            }
        }


    }
}
