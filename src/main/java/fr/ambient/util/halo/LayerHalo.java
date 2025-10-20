package fr.ambient.util.halo;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.protection.ProtectedLaunch;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.img.ImageObject;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@ExcludeFlow
@ExcludeConstant
public class LayerHalo implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer playerRenderer;

    public LayerHalo(RenderPlayer playerRenderer) {
        this.playerRenderer = playerRenderer;
    }

    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {

        GlStateManager.pushMatrix();

        double lastX = entitylivingbaseIn.lastTickPosX + (entitylivingbaseIn.posX - entitylivingbaseIn.lastTickPosX) * partialTicks;
        double lastZ = entitylivingbaseIn.lastTickPosZ + (entitylivingbaseIn.posZ - entitylivingbaseIn.lastTickPosZ) * partialTicks;
        double lastY = entitylivingbaseIn.lastTickPosY + (entitylivingbaseIn.posY - entitylivingbaseIn.lastTickPosY) * partialTicks;

        double scalePixels = 256;
        float rpitch = MathHelper.clamp_float(p_177141_7_, 20, 37);

        double multiplier = ProtectedLaunch.CUSTOM_CAPE.haloScale.getValue();


        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.rotate(p_177141_6_, 0,0,0.2f);
        GlStateManager.rotate(-rpitch, 1,0,0);
        GlStateManager.translate(0, -ProtectedLaunch.CUSTOM_CAPE.haloHeight.getValue(), 0);
        GlStateManager.rotate(90, 1,0,0);
        GlStateManager.scale(1 / scalePixels * multiplier,1 / scalePixels * multiplier,1 / scalePixels * multiplier);

        GlStateManager.scale(scalePixels / multiplier,scalePixels / multiplier,scalePixels / multiplier);
        GlStateManager.rotate(-90, 0,0,1);
        GlStateManager.translate(0, ProtectedLaunch.CUSTOM_CAPE.haloHeight.getValue(), 0);
        GlStateManager.rotate(rpitch, 1,0,0);
        GlStateManager.rotate(-p_177141_6_, 0,0,0.2f);

        GlStateManager.bindTexture(0);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    public boolean shouldCombineTextures() {
        return true;
    }
}
