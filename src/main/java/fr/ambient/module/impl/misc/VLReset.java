package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;

public class VLReset extends Module {
    public VLReset() {
        super(64, "VLReset", ModuleCategory.MISC);
    }
    public int count = 0;
    public void onEnable(){
        count = 0;
        ChatUtil.display("Please avoid moving / jumping...");
    }
    public void onDisable(){
        ChatUtil.display("Done !");
    }

    @SubscribeEvent
    public void onEvent(UpdateEvent event){
        if(count < 15){
            MoveUtil.strafe(0);
            if(mc.thePlayer.onGround){
                mc.thePlayer.motionY = 0.035f;
                count++;
            }
        }else{
            this.setEnabled(false);
        }
    }

}
