package fr.ambient.module.impl.misc;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Freelook extends Module {
    public Freelook() {
        super(51,"Allows you to look around without changing your character’s direction.", ModuleCategory.MISC);
        this.registerProperty(autoF5);

    }


    private BooleanProperty autoF5 = BooleanProperty.newInstance("Auto F5", true);


    private float renderRotationYaw = 0;
    private float renderRotationPitch = 0;
    private float lastRenderRotationYaw = 0;
    private float lastRenderRotationPitch = 0;

    public void onEnable(){
        renderRotationYaw = mc.thePlayer.rotationYaw;
        renderRotationPitch = mc.thePlayer.rotationPitch;
        lastRenderRotationYaw = mc.thePlayer.prevRotationYaw;
        lastRenderRotationPitch = mc.thePlayer.prevRotationPitch;
    }

    public void onDisable(){
        if(autoF5.getValue()){
            mc.gameSettings.thirdPersonView = 0;
        }
    }

    @SubscribeEvent
    private void onTick(UpdateEvent event){
        if(autoF5.getValue()){
            mc.gameSettings.thirdPersonView = 1;
        }
    }

    // remember, left alt is "lmenu"


    public float[] getRenderRotations(){
        if(this.isEnabled()) {
            return new float[]{renderRotationYaw, renderRotationPitch, lastRenderRotationYaw, lastRenderRotationPitch};
        }else if(!Ambient.getInstance().getRotationComponent().canUpdateRotations()){
            return new float[]{Ambient.getInstance().getRotationComponent().rotationYaw, Ambient.getInstance().getRotationComponent().rotationPitch,
                    Ambient.getInstance().getRotationComponent().prevRotationYaw, Ambient.getInstance().getRotationComponent().prevRotationPitch};
        }else{
            return new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch};
        }
    }
}
