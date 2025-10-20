package fr.ambient.module.impl.combat;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.property.impl.MultiProperty;
import fr.ambient.property.impl.NumberProperty;

import java.util.Arrays;
import java.util.HashSet;

public class FakeLag extends Module {
    public FakeLag() {
        super(67, ModuleCategory.COMBAT);
        registerProperties(delayMs, checks);
    }

    public NumberProperty delayMs = NumberProperty.newInstance("Delay MS", 0f, 100f, 5000f, 1f);
    public MultiProperty checks = MultiProperty.newInstance("Disable On", new String[]{"Damage","C08", "C02", "Fall", "HypixelFix", "Scaffold"}, new HashSet<>(Arrays.asList("HypixelFix", "C02")));

    public void onEnable(){
        Ambient.getInstance().getOutgoingPacketComponent().setEnabled(true);
    }
    public void onDisable(){
        Ambient.getInstance().getOutgoingPacketComponent().setEnabled(false);
    }

    @SubscribeEvent
    private void onTickEvent(UpdateEvent event){
        if(mc.thePlayer.ticksExisted < 5 || shouldStopForReason()){
            Ambient.getInstance().getOutgoingPacketComponent().setEnabled(false);
        }else{
            Ambient.getInstance().getOutgoingPacketComponent().setDelayMs(delayMs.getValue().intValue());
            Ambient.getInstance().getOutgoingPacketComponent().setEnabled(true);

        }
    }

    public boolean shouldStopForReason(){
        if(checks.isSelected("Damage") && mc.thePlayer.hurtTime > 4){
            return true;
        }
        if(checks.isSelected("C08") && Ambient.getInstance().getOutgoingPacketComponent().contains08()){
            return true;
        }
        if(checks.isSelected("C02") && Ambient.getInstance().getOutgoingPacketComponent().contains02()){
            return true;
        }
        if(checks.isSelected("Fall") && mc.thePlayer.fallDistance > 2.5f){
            return true;
        }
        if(checks.isSelected("HypixelFix") && Ambient.getInstance().getOutgoingPacketComponent().getTimeSinceLastReset().finished(2000)){
            return true;
        }
        return checks.isSelected("Scaffold") && Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled();
    }

}
