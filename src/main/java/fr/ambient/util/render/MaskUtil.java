package fr.ambient.util.render;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.util.InstanceAccess;
import fr.ambient.util.render.shader.Shader;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;

@ExcludeFlow
@ExcludeConstant
public class MaskUtil implements InstanceAccess {

    private static Framebuffer maskBuffer;
    private static Framebuffer renderBuffer;

    private static final Shader maskShader = new Shader("mask.frag");

    public static void refreshFramebuffers() {
        maskBuffer = RenderUtil.createFrameBuffer(maskBuffer,true);
        renderBuffer = RenderUtil.createFrameBuffer(renderBuffer, true);
        maskBuffer.framebufferClear();
        renderBuffer.framebufferClear();
    }

    public static void mask(Runnable mask, Runnable render) {
        refreshFramebuffers();

        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();

        maskBuffer.bindFramebuffer(true);
        mask.run();
        maskBuffer.unbindFramebuffer();

        renderBuffer.bindFramebuffer(true);
        render.run();
        renderBuffer.unbindFramebuffer();

        mc.getFramebuffer().bindFramebuffer(true);

        maskShader.start();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.setActiveTexture(GL_TEXTURE0);
        GlStateManager.bindTexture(renderBuffer.framebufferTexture);
        maskShader.setUniformInteger("iChannel0", 0);

        GlStateManager.setActiveTexture(GL_TEXTURE1);
        GlStateManager.bindTexture(maskBuffer.framebufferTexture);
        maskShader.setUniformInteger("textureMask", 1);

        maskShader.drawQuads();
        maskShader.stop();

        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

        GlStateManager.setActiveTexture(GL_TEXTURE1);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL_TEXTURE0);

        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.bindTexture(0);
    }

}
