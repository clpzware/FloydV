package fr.ambient.util.render;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.Ambient;
import fr.ambient.module.impl.render.hud.PostProcessing;
import fr.ambient.util.InstanceAccess;
import fr.ambient.util.render.shader.Shader;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;

import static fr.ambient.util.render.RenderUtil.glowShader;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;

@ExcludeFlow
@ExcludeConstant
public class GlowUtil implements InstanceAccess {
    @Setter
    private static float radius;
    @Setter
    private static Framebuffer[] blurFramebuffers;
    private static int lastIterationCount = -1;

    private final Shader maskShader = new Shader("mask.frag");

    public static void refreshFramebuffers() {
        PostProcessing postProcessing = Ambient.getInstance().getModuleManager().getModule(PostProcessing.class);
        int iterations = 5;
        radius = postProcessing.radius.getValue() / iterations;

        if (blurFramebuffers == null || lastIterationCount != iterations) {
            lastIterationCount = iterations;

            if (blurFramebuffers != null) {
                for (Framebuffer fb : blurFramebuffers) {
                    if (fb != null) {
                        fb.deleteFramebuffer();
                    }
                }
            }

            blurFramebuffers = new Framebuffer[iterations];
            for (int i = 0; i < iterations; i++) {
                blurFramebuffers[i] = RenderUtil.createFrameBuffer(null, true);
            }
        } else {
            for (int i = 0; i < blurFramebuffers.length; i++) {
                blurFramebuffers[i] = RenderUtil.createFrameBuffer(blurFramebuffers[i], true);
            }
        }
    }

    public void glow(final Framebuffer framebufferGlow) {
        refreshFramebuffers();

        performGlowPass(0, framebufferGlow.framebufferTexture);

        for (int i = 1; i < blurFramebuffers.length; i++) {
            performGlowPass(i, blurFramebuffers[i - 1].framebufferTexture);
        }

        mc.getFramebuffer().bindFramebuffer(true);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        maskShader.start();

        GlStateManager.setActiveTexture(GL_TEXTURE0);
        GlStateManager.bindTexture(blurFramebuffers[blurFramebuffers.length - 1].framebufferTexture);
        maskShader.setUniformInteger("iChannel0", 0);

        GlStateManager.setActiveTexture(GL_TEXTURE1);
        GlStateManager.bindTexture(framebufferGlow.framebufferTexture);
        maskShader.setUniformInteger("textureMask", 1);

        maskShader.drawQuads();
        maskShader.stop();

        GlStateManager.setActiveTexture(GL_TEXTURE1);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL_TEXTURE0);
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }

    private void performGlowPass(final int index, final int sourceTexture) {
        blurFramebuffers[index].framebufferClear();
        blurFramebuffers[index].bindFramebuffer(true);

        glowShader.start();
        glBindTexture(GL_TEXTURE_2D, sourceTexture);

        setupGlowUniforms(radius + index);

        glowShader.drawQuads();
        glowShader.stop();

        blurFramebuffers[index].unbindFramebuffer();
    }

    private void setupGlowUniforms(final float radius) {
        glowShader.setUniform("iResolution", (float) mc.displayWidth, (float) mc.displayHeight);
        glowShader.setUniform("radius", radius);
    }
}