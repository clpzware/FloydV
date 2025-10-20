package fr.ambient.util;


import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

@UtilityClass
public class RenderedRotationUtil {
    public void setRenderedRotation(float yaw, float pitch, String mode){


        yaw = MathHelper.wrapAngleTo180_float(yaw);

        if(mode.equalsIgnoreCase("None")){
            return;
        }
        if(mode.equalsIgnoreCase("HeadOnly") || mode.equalsIgnoreCase("FullBody") || mode.equalsIgnoreCase("45")){
            Minecraft.getMinecraft().thePlayer.rotationYawHead = yaw;
            Minecraft.getMinecraft().thePlayer.renderPitchHead = pitch;
        }
        if(mode.equalsIgnoreCase("FullBody")){
            Minecraft.getMinecraft().thePlayer.renderYawOffset = yaw;
            Minecraft.getMinecraft().thePlayer.prevRenderYawOffset = yaw;
        }
        if(mode.equalsIgnoreCase("45")){
            Minecraft.getMinecraft().thePlayer.renderYawOffset = yaw + Minecraft.getMinecraft().thePlayer.moveStrafing * 45;
            Minecraft.getMinecraft().thePlayer.prevRenderYawOffset = yaw + Minecraft.getMinecraft().thePlayer.moveStrafing * 45;
        }
    }


}
