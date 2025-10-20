package fr.ambient.module.impl.combat;

import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.MultiProperty;
import net.minecraft.entity.EntityLivingBase;

public class AntiBot extends Module {
    public AntiBot() {
        super(0,"Avoids attacking anticheat bots.", ModuleCategory.COMBAT);
        registerProperties(checks);
    }


    public MultiProperty checks = MultiProperty.newInstance("Checks", new String[]{"NPC", "Name"});


    public boolean isBot(EntityLivingBase player) {
        if(checks.isSelected("NPC") && !player.hasMoved){
            return true;
        }
        if(checks.isSelected("Name") && player.getName().startsWith("NPC-")){
            return true;
        } // add other modes like that idc
        return false;
    }
}