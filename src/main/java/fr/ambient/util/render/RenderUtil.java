package fr.ambient.util.render;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.Ambient;
import fr.ambient.theme.Theme;
import fr.ambient.util.InstanceAccess;
import fr.ambient.util.render.opengl.StencilUtil;
import fr.ambient.util.render.shader.Shader;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjglx.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.*;

@ExcludeFlow
@ExcludeConstant
@SuppressWarnings("unused")
public final class RenderUtil implements InstanceAccess {

    private static final Frustum frustum = new Frustum();
    public static final GlowUtil glowUtil = new GlowUtil();

    //HashMaps
    private static final HashMap<String, Float> blackPeople = new HashMap<>();
    @Getter
    private static final HashMap<String, Integer> cachedHeads = new HashMap<>();
    @Getter
    private static final HashMap<String, Integer> cachedSkinBody = new HashMap<>();
    @Getter
    private static final HashMap<String, Integer> cachedSkinHead = new HashMap<>();

    //Blur
    public static Framebuffer blurFramebuffer = new Framebuffer(1, 1, true);

    //Shaders
    private static final Shader loadingCircleShader2 = new Shader("circleLoading2.frag");
    private static final Shader loadingCircleShader = new Shader("circleLoading.frag");
    public static final Shader roundedShader = new Shader("rounded.frag");
    public static final Shader outlineShader = new Shader("rounded_outline.frag");
    private static final Shader texShader = new Shader("roundedTexture.frag");
    private static final Shader circleShader = new Shader("circle.frag");
    private static final Shader blurShader = new Shader("gaussian.frag");
    public static final Shader glowShader = new Shader("kawase.frag");
    public static final Shader hueShader = new Shader("hue.frag");

    public static float zLevel = 0f;

    //Precompute sin and cos for rectangle and circle (which i dont really need so)
    private static final double[] sin;
    private static final double[] cos;

    @Getter
    public enum Shapes {
        ROUNDED_RECT(RenderUtil.roundedShader),
        ROUNDED_OUTLINE(RenderUtil.outlineShader);

        private final Shader shader;

        Shapes(Shader shader) {
            this.shader = shader;
        }
    }

    static {
        sin = new double[181];
        cos = new double[181];
        for (int angle = 0; angle <= 180; angle += 6) {
            double radians = Math.toRadians(angle);
            sin[angle] = Math.sin(radians);
            cos[angle] = Math.cos(radians);
        }
    }

    // could register to use an event but useless
    public static void onPlayerJoin() {
        for(int texture : cachedHeads.values()){
            GL11.glDeleteTextures(texture);
        }

        cachedHeads.clear();
        blackPeople.clear();
    }

    private static final ArrayList<Runnable> glowRunnables = new ArrayList<>();
    public static void addGlow(Runnable runnable) {
        glowRunnables.add(runnable);
    }

    public static Framebuffer glowFramebuffer = new Framebuffer(1, 1, false);
    public static void drawGlow() {
        if (mc.gameSettings.ofFastRender || glowRunnables.isEmpty())
            return;

        glowFramebuffer = createFrameBuffer(glowFramebuffer, true);
        glowFramebuffer.framebufferClear();

        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();

        glowFramebuffer.bindFramebuffer(true);

        for (Runnable runnable : glowRunnables) {
            runnable.run();
            ColorUtil.setColor(Color.WHITE);
        }

        glowFramebuffer.unbindFramebuffer();

        glowUtil.glow(glowFramebuffer);

        glowRunnables.clear();
    }

    public static boolean isBBInFrustum(final AxisAlignedBB aabb) {
        frustum.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        return frustum.isBoundingBoxInFrustum(aabb);
    }

    public static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = mc.getRenderViewEntity();
        frustum.setPosition(current.posX, current.posY, current.posZ);
        return frustum.isBoundingBoxInFrustum(bb);
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }
    public static void drawImage(int texid, int x, int y, int width, int height) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,texid);
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }

    public static void scale(Runnable runnable, float x, float y, double scale) {
        glPushMatrix();
        glTranslatef(x, y, 0);
        glScaled(scale, scale, 1);
        glTranslatef(-x, -y, 0);
        runnable.run();
        glPopMatrix();
    }

    public static void scale(float x, float y, double scale) {
        glTranslatef(x, y, 0);
        glScaled(scale, scale, 1);
        glTranslatef(-x, -y, 0);
    }

    public static void translate(final Runnable runnable, float x, float y) {
        glPushMatrix();
        glTranslatef(x, y, 0);
        runnable.run();
        glPopMatrix();
    }

    public static void start() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
    }

    public static void stop() {
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void render(final int mode, final Runnable render) {
        glBegin(mode);
        render.run();
        glEnd();
    }

    public static void color(final Color color) {
        final float[] array = ColorUtil.toGLColor(color);
        glColor4f(array[0], array[1], array[2], array[3]);
    }

    public static void color(final Color color, float alpha) {
        final float[] array = ColorUtil.toGLColor(color);
        glColor4f(array[0], array[1], array[2], alpha);
    }

    public static void drawRect(float x, float y, float width, float height, Color color) {
        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        y += height;
        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);

        glBegin(GL_QUADS);
        glVertex2d(x, y);
        glVertex2d(x + width, y);
        glVertex2d(x + width, y - height);
        glVertex2d(x, y - height);
        glEnd();

        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public static void drawVerticalGradient(float x, float y, float width, float height, Color topColor, Color bottomColor) {
        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        y += height;

        glBegin(GL_QUADS);
        glColor4f(topColor.getRed() / 255.0f, topColor.getGreen() / 255.0f, topColor.getBlue() / 255.0f, topColor.getAlpha() / 255.0f);
        glVertex2d(x, y);

        glColor4f(topColor.getRed() / 255.0f, topColor.getGreen() / 255.0f, topColor.getBlue() / 255.0f, topColor.getAlpha() / 255.0f);
        glVertex2d(x + width, y);

        glColor4f(bottomColor.getRed() / 255.0f, bottomColor.getGreen() / 255.0f, bottomColor.getBlue() / 255.0f, bottomColor.getAlpha() / 255.0f);
        glVertex2d(x + width, y - height);

        glColor4f(bottomColor.getRed() / 255.0f, bottomColor.getGreen() / 255.0f, bottomColor.getBlue() / 255.0f, bottomColor.getAlpha() / 255.0f);
        glVertex2d(x, y - height);
        glEnd();

        enableTexture2D();
        disableBlend();
        resetColor();
    }





    public static void drawCircle(Vec3 place, double rad, float divisor, float lineWidth, Color color) {
        Minecraft mc = Minecraft.getMinecraft();

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        final double x = place.xCoord - RenderManager.renderPosX;
        final double y = place.yCoord - RenderManager.renderPosY;
        final double z = place.zCoord - RenderManager.renderPosZ;

        for (float i = 0; i < Math.PI * 2 + divisor; i += (float) (Math.PI * 2 / divisor)) {

            final double vecX = x + rad * Math.cos(i);
            final double vecZ = z + rad * Math.sin(i);
            GL11.glColor4f(color.getRed() / 255.F,
                    color.getGreen() / 255.F,
                    color.getBlue() / 255.F, 1f);
            GL11.glVertex3d(vecX, y, vecZ);
        }
        GL11.glEnd();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    public static void drawRectOutline(float x, float y, float width, float height, float thickness, Color color) {
        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        y += height;
        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);

        glLineWidth(thickness);
        glBegin(GL_LINE_SMOOTH);
        glVertex2d(x, y);
        glVertex2d(x + width, y);
        glVertex2d(x + width, y - height);
        glVertex2d(x, y - height);
        glEnd();

        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public static void drawVariableRoundedRect(final float x, final float y, final float width, final float height, final float[] radii, final Color color) {
        drawVariableRoundedRect(x, y, width, height, radii, color, color, color, color);
    }

    public static void drawRoundedRect(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        drawVariableRoundedRect(x, y, width, height, new float[] {radius, radius, radius, radius}, color, color, color, color);
    }

    public static void drawRoundGradient(final float x, final float y, final float width, final float height, final float radius, final Color color, final Color color1) {
        drawVariableRoundedRect(x, y, width, height, new float[] {radius, radius, radius, radius}, color, color, color1, color1);
    }

    public static void drawRoundVertGradient(final float x, final float y, final float width, final float height, final float radius, final Color color, final Color color1) {
        drawVariableRoundedRect(x, y, width, height, new float[] {radius, radius, radius, radius}, color, color1, color, color1);
    }

    public static void drawRoundedRect(final float x, final float y, final float width, final float height, final float radius, final Color color1, final Color color2, final Color color3, final Color color4) {
        drawVariableRoundedRect(x, y, width, height, new float[] {radius, radius, radius, radius}, color1, color2, color3, color4);
    }

    public static void drawVariableRoundedRect(final float x, final float y, final float width, final float height, final float[] radii, final Color color1, final Color color2, final Color color3, final Color color4) {
        ScaledResolution sr = new ScaledResolution(mc);

        float[] colors1 = ColorUtil.toGLColor(color1);
        float[] colors2 = ColorUtil.toGLColor(color2);
        float[] colors3 = ColorUtil.toGLColor(color3);
        float[] colors4 = ColorUtil.toGLColor(color4);

        for (int i = 0; i < radii.length; i++) {
            radii[i] *= sr.getScaleFactor();
        }

        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        roundedShader.start();
        roundedShader.setUniformFloat("size", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        roundedShader.setUniformFloat("cornerRadii", radii);
        roundedShader.setUniformFloat("color1", colors1[0], colors1[1], colors1[2], colors1[3]);
        roundedShader.setUniformFloat("color2", colors2[0], colors2[1], colors2[2], colors2[3]);
        roundedShader.setUniformFloat("color3", colors3[0], colors3[1], colors3[2], colors3[3]);
        roundedShader.setUniformFloat("color4", colors4[0], colors4[1], colors4[2], colors4[3]);
        roundedShader.drawQuads(x, y, width, height);
        roundedShader.stop();
        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public static void drawVariableRoundedOutline(final float x, final float y, final float width, final float height, final float[] radii, final float strokeWidth, final Color color) {
        drawVariableRoundedOutline(x, y, width, height, radii, strokeWidth, color, color, color, color);
    }

    public static void drawRoundedOutline(final float x, final float y, final float width, final float height, final float radius, final float strokeWidth, final Color color) {
        drawVariableRoundedOutline(x, y, width, height, new float[] {radius, radius, radius, radius}, strokeWidth, color, color, color, color);
    }

    public static void drawRoundedOutline(final float x, final float y, final float width, final float height, final float radius, final float strokeWidth, final Color color1, final Color color2, final Color color3, final Color color4) {
        drawVariableRoundedOutline(x, y, width, height, new float[] {radius, radius, radius, radius}, strokeWidth, color1, color2, color3, color4);
    }

    public static void drawVariableRoundedOutline(final float x, final float y, final float width, final float height, final float[] radii, final float strokeWidth, final Color color1, final Color color2, final Color color3, final Color color4) {
        ScaledResolution sr = new ScaledResolution(mc);

        float[] colors1 = ColorUtil.toGLColor(color1);
        float[] colors2 = ColorUtil.toGLColor(color2);
        float[] colors3 = ColorUtil.toGLColor(color3);
        float[] colors4 = ColorUtil.toGLColor(color4);

        for (int i = 0; i < radii.length; i++) {
            radii[i] *= sr.getScaleFactor();
        }

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, 0);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        outlineShader.start();
        outlineShader.setUniformFloat("size", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        outlineShader.setUniformFloat("round", radii[0]);
        outlineShader.setUniformFloat("width", strokeWidth);
        outlineShader.setUniformFloat("color1", colors1[0], colors1[1], colors1[2], colors1[3]);
        outlineShader.setUniformFloat("color2", colors2[0], colors2[1], colors2[2], colors2[3]);
        outlineShader.setUniformFloat("color3", colors3[0], colors3[1], colors3[2], colors3[3]);
        outlineShader.setUniformFloat("color4", colors4[0], colors4[1], colors4[2], colors4[3]);
        outlineShader.drawQuads(x, y, width, height);
        outlineShader.stop();

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        resetColor();
    }

    public static void drawRoundHue(final float x, final float y, final float width, final float height, final float radius) {
        ScaledResolution sr = new ScaledResolution(mc);

        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        hueShader.start();
        hueShader.setUniformFloat("size", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        hueShader.setUniformFloat("round", radius * sr.getScaleFactor());
        hueShader.drawQuads(x, y, width, height);
        hueShader.stop();
        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public static void drawRoundImage(int textureId, final float x, final float y, final float width, final float height, final float radius, boolean crop) {
        ScaledResolution sr = new ScaledResolution(mc);
        int scaleFactor = sr.getScaleFactor();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        texShader.start();
        GlStateManager.bindTexture(textureId);
        texShader.setUniformFloat("size", width * scaleFactor, height * scaleFactor);
        texShader.setUniformFloat("round", radius * scaleFactor);
        texShader.setUniformBool("useCrop", crop);
        texShader.drawQuads(x, y, width, height);
        texShader.stop();

        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }



    public static void drawRoundedRectGl(float x, float y, float width, float height, float round, Color color) {
        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        round = Math.min(round, Math.min(width / 2, height / 2));


        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);

        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_POLYGON);

        for (int angle = 0; angle <= 90; angle += 6) {
            glVertex2d(x + round - sin[angle] * round, y + round - cos[angle] * round);
        }

        for (int angle = 90; angle <= 180; angle += 6) {
            glVertex2d(x + round - sin[angle] * round, y + height - round - cos[angle] * round);
        }

        for (int angle = 0; angle <= 90; angle += 6) {
            glVertex2d(x + width - round + sin[angle] * round, y + height - round + cos[angle] * round);
        }

        for (int angle = 90; angle <= 180; angle += 6) {
            glVertex2d(x + width - round + sin[angle] * round, y + round + cos[angle] * round);
        }
        glEnd();

        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_LINE_SMOOTH);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRoundedRectGl(
        float x, float y, float width, float height,
        float[] radii,
        Color color) {

        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);

        radii[0] = Math.min(radii[0], Math.min(width / 2, height / 2));
        radii[1] = Math.min(radii[1], Math.min(width / 2, height / 2));
        radii[2] = Math.min(radii[2], Math.min(width / 2, height / 2));
        radii[3] = Math.min(radii[3], Math.min(width / 2, height / 2));

        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);

        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_POLYGON);

        for (int angle = 0; angle <= 90; angle += 6) {
            glVertex2d(x + radii[0] - sin[angle] * radii[0], y + radii[0] - cos[angle] * radii[0]);
        }

        for (int angle = 90; angle <= 180; angle += 6) {
            glVertex2d(x + radii[3] - sin[angle] * radii[3], y + height - radii[3] - cos[angle] * radii[3]);
        }

        for (int angle = 0; angle <= 90; angle += 6) {
            glVertex2d(x + width - radii[2] + sin[angle] * radii[2], y + height - radii[2] + cos[angle] * radii[2]);
        }

        for (int angle = 90; angle <= 180; angle += 6) {
            glVertex2d(x + width - radii[1] + sin[angle] * radii[1], y + radii[1] + cos[angle] * radii[1]);
        }
        glEnd();

        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_LINE_SMOOTH);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }



    public static void drawCircleLoading(final float x, final float y, final float size, final float percentage, final Color color) {
        float[] colors = ColorUtil.toGLColor(color);

        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        loadingCircleShader.start();
        loadingCircleShader.setUniformFloat("iResolution", size, size);
        loadingCircleShader.setUniformFloat("progress",percentage);
        loadingCircleShader.setUniformInteger("isRound", 1);
        loadingCircleShader.setUniformFloat("color1", colors[0], colors[1], colors[2], colors[3]);
        loadingCircleShader.setUniformFloat("color2", colors[0], colors[1], colors[2], colors[3]);
        loadingCircleShader.drawQuads(x, y, size, size);
        loadingCircleShader.stop();
        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public static void drawCircleLoading2(final float x, final float y, final float size) {

        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        loadingCircleShader2.start();
        loadingCircleShader2.setUniformFloat("iResolution", size, size);
        loadingCircleShader2.setUniformFloat("iTime", (System.currentTimeMillis() - loadingCircleShader2.startTime)/1000f);
        loadingCircleShader2.drawQuads(x, y, size, size);
        loadingCircleShader2.stop();
        enableTexture2D();
        disableBlend();
        resetColor();
    }


    public static void drawCircleLoading(final float x, final float y, final float size, final float percentage, final Color color1, final Color color2) {
        float[] colors1 = ColorUtil.toGLColor(color1);
        float[] colors2 = ColorUtil.toGLColor(color2);

        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        loadingCircleShader.start();
        loadingCircleShader.setUniformFloat("iResolution", size, size);
        loadingCircleShader.setUniformFloat("progress",percentage);
        loadingCircleShader.setUniformFloat("color1", colors1[0], colors1[1], colors1[2], colors1[3]);
        loadingCircleShader.setUniformFloat("color2", colors2[0], colors2[1], colors2[2], colors2[3]);
        loadingCircleShader.drawQuads(x, y, size, size);
        loadingCircleShader.stop();
        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public static void drawCircle(final float x, final float y, final float size, final Color color) {
        float[] colors = ColorUtil.toGLColor(color);

        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        circleShader.start();
        circleShader.setUniformFloat("size", size);
        circleShader.setUniformFloat("blur", 1 / size);
        circleShader.setUniformFloat("thickness", 1f);
        circleShader.setUniformFloat("color", colors[0], colors[1], colors[2], colors[3]);
        circleShader.drawQuads(x, y, size, size);
        circleShader.stop();

        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public static void drawLine(final float x, final float y, final float x2, final float y2, final float size, final Color color) {
        disableTexture2D();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);

        glEnable(GL11.GL_LINE_SMOOTH);
        glLineWidth(size);

        glBegin(GL11.GL_LINES);

        glVertex2f(x, y);
        glVertex2f(x + x2, y + y2);

        glEnd();

        glDisable(GL11.GL_LINE_SMOOTH);
        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public static CompletableFuture<Float> getBlackAndBrownPercentageAsync(EntityPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(player.getUniqueID());

            if (playerInfo == null) {
                return 0f;
            }

            if (blackPeople.containsKey(player.getUniqueID().toString())) {
                return blackPeople.get(player.getUniqueID().toString());
            }

            try {
                URL skinURL = new URL("https://skins.mcstats.com/face/" + player.getUniqueID());
                BufferedImage skinImage = ImageIO.read(skinURL);
                if (skinImage == null) {
                    return 0f; // Handle case where skinImage is null
                }

                int width = skinImage.getWidth();
                int height = skinImage.getHeight();
                int blackPixelCount = 0;
                int totalPixels = 0;

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int color = skinImage.getRGB(x, y);
                        int alpha = (color >> 24) & 0xFF;
                        if (alpha == 0) {
                            continue; // Ignore fully transparent pixels
                        }

                        int red = (color >> 16) & 0xFF;
                        int green = (color >> 8) & 0xFF;
                        int blue = color & 0xFF;

                        //black
                        if (red <= 60 && green <= 60 && blue <= 60) {
                            blackPixelCount++;
                        }


                        totalPixels++;
                    }
                }

                if (totalPixels == 0) {
                    return 0f;
                }

                float blackPercentage = (blackPixelCount / (float) totalPixels) * 100;
                blackPeople.put(player.getUniqueID().toString(), blackPercentage);
                return blackPercentage;

            } catch (IOException ignored) {/* */}

            return 0f;
        });
    }

    public static void drawHeadAPI(UUID player, float x, float y, float size) {
        drawHead(player, x, y, size);
    }

    public static int getSkinTexture(EntityPlayer player) {
        try {
            ResourceLocation skinLoc = ((AbstractClientPlayer) player).getLocationSkin();
            if (skinLoc != null) {
                ITextureObject itextureobject = mc.getTextureManager().mapTextureObjects.get(skinLoc);

                return itextureobject.getGlTextureId();
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void drawRoundSkin(EntityPlayer player, float x, float y, float size, float radius) {
        if(player == null){
            return;
        }
        int textureID = getSkinTexture(player);

        drawRoundImage(textureID, x, y, size, size, radius, true);
    }

    public static void drawHead(EntityPlayer player, float x, float y, float size) {
        try {
            GlStateManager.color(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(((AbstractClientPlayer) player).getLocationSkin());
            Gui.drawScaledCustomSizeModalRect(x, y, 4, 4, 4, 4, size, size, 32, 32);
        } catch (Exception e) {
            drawCircleLoading2(x, y, size);
        }
    }

    public static void drawHead(UUID uuid, float x, float y, float size) {
        int textureID;
        if(cachedHeads.containsKey(uuid.toString())) {
            textureID = cachedHeads.get(uuid.toString());
        }else {
            CompletableFuture<Integer> getHead = getHeadTextureID(uuid);
            textureID = getHead.getNow(0);
        }

        if(textureID == 0){
            drawCircleLoading2(x, y, size);
            return;
        }

        GL11.glColor4f(1,1,1,1);
        GlStateManager.bindTexture(textureID);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, size, size, size, size);
    }

    public static void drawHeadFromLink(String link, float x, float y, float size) {
        int textureID;
        if(cachedSkinHead.containsKey(link)) {
            textureID = cachedSkinHead.get(link);
        }else {
            CompletableFuture<Integer> getHead = getSkinHead(link);
            textureID = getHead.getNow(0);
        }

        if(textureID == 0){
            drawCircleLoading2(x, y, size);
            return;
        }

        GL11.glColor4f(1,1,1,1);
        GlStateManager.bindTexture(textureID);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, size, size, size, size);
    }

    public static void drawRoundHead(UUID uuid, float x, float y, float size, float radius) {
        if(uuid == null){
            return;
        }
        int textureID;
        if(cachedHeads.containsKey(uuid.toString())) {
            textureID = cachedHeads.get(uuid.toString());
        }else {
            CompletableFuture<Integer> getHead = getHeadTextureID(uuid);
            textureID = getHead.getNow(0);
        }

        if(textureID == 0){
            drawCircleLoading2(x, y, size);
            return;
        }

        drawRoundImage(textureID, x, y, size, size, radius, false);
    }

    public static void drawBody(String uuid, float x, float y, float w, float h) {
        if(uuid == null){
            return;
        }
        int textureID;
        if(cachedSkinBody.containsKey(uuid)) {
            textureID = cachedSkinBody.get(uuid);
        }else {
            CompletableFuture<Integer> getHead = getBodyTexID(uuid);
            textureID = getHead.getNow(0);
        }

        if(textureID == 0){
            return;
        }

        drawRoundImage(textureID, x,y,w,h,0, false);
    }

    public static void drawItemStack(ItemStack stack, float x, float y) {

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        if (stack != null) {
            mc.ingameGUI.itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }

    private record ImageData(ByteBuffer buffer, int width, int height) {
        //
    }

    private static CompletableFuture<Integer> getHeadTextureID(EntityPlayer player) {
        UUID uuid = player.getUniqueID();
        return getHeadTextureID(uuid);
    }

    private static CompletableFuture<Integer> getHeadTextureID(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if(cachedHeads.containsKey(uuid.toString())) {
                return null;
            }
            cachedHeads.put(uuid.toString(), 0);
            try {
                URL skinURL = new URL("https://skins.mcstats.com/face/" + uuid);
                BufferedImage img = ImageIO.read(skinURL);

                final int width = img.getWidth();
                final int height = img.getHeight();
                int[] pixels = new int[width * height];
                img.getRGB(0, 0, width, height, pixels, 0, width);

                ByteBuffer buffer = ByteBuffer.allocateDirect(4 * width * height);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = pixels[y * width + x];
                        buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
                        buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green component
                        buffer.put((byte) (pixel & 0xFF));         // Blue component
                        buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component
                    }
                }
                buffer.flip();

                return new ImageData(buffer, width, height);
            } catch (IOException ignored) {
                return null;
            }
        }).thenApplyAsync(imageData -> {
            if (imageData == null) return 0;
            int textureID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, imageData.width, imageData.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageData.buffer);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            cachedHeads.put(uuid.toString(), textureID);
            return textureID;
        }, mc::addScheduledTask);
    }

    private static CompletableFuture<Integer> getSkinHead(String avtrl) {
        return CompletableFuture.supplyAsync(() -> {
            if(cachedSkinHead.containsKey(avtrl)) {
                return null;
            }
            cachedSkinHead.put(avtrl, 0);
            try {
                String ulink = avtrl;
                if(ulink.toString().contains("s.namemc.com/i/")){
                    ulink = ulink.replace("https://", "");
                    ulink = ulink.replace("http://", "");
                    ulink = ulink.replace("s.namemc.com/i/", "");
                    ulink = ulink.replace(".png", "");
                    ulink = ("https://s.namemc.com/2d/skin/face.png?id=" + ulink + "&scale=12");
                }

                URL skinURL = new URL(ulink);



                BufferedImage img = ImageIO.read(skinURL);

                final int width = img.getWidth();
                final int height = img.getHeight();
                int[] pixels = new int[width * height];
                img.getRGB(0, 0, width, height, pixels, 0, width);

                ByteBuffer buffer = ByteBuffer.allocateDirect(4 * width * height);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = pixels[y * width + x];
                        buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
                        buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green component
                        buffer.put((byte) (pixel & 0xFF));         // Blue component
                        buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component
                    }
                }
                buffer.flip();

                return new ImageData(buffer, width, height);
            } catch (IOException ignored) {
                return null;
            }
        }).thenApplyAsync(imageData -> {
            if (imageData == null) return 0;
            int textureID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, imageData.width, imageData.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageData.buffer);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            cachedSkinHead.put(avtrl, textureID);
            return textureID;
        }, mc::addScheduledTask);
    }

    private static CompletableFuture<Integer> getBodyTexID(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if(cachedSkinBody.containsKey(uuid)) {
                return null;
            }
            cachedSkinBody.put(uuid, 0);

            try {
                URL skinURL = new URL("https://crafatar.com/renders/body/" + uuid + "?scale=10");
                BufferedImage img = ImageIO.read(skinURL);

                final int width = img.getWidth();
                final int height = img.getHeight();
                int[] pixels = new int[width * height];
                img.getRGB(0, 0, width, height, pixels, 0, width);

                ByteBuffer buffer = ByteBuffer.allocateDirect(4 * width * height);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = pixels[y * width + x];
                        buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
                        buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green component
                        buffer.put((byte) (pixel & 0xFF));         // Blue component
                        buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component
                    }
                }
                buffer.flip();

                return new ImageData(buffer, width, height);
            } catch (IOException ignored) {
                return null;
            }
        }).thenApplyAsync(imageData -> {
            if (imageData == null) return 0;
            int textureID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, imageData.width, imageData.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageData.buffer);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            cachedSkinBody.put(uuid.toString(), textureID);
            return textureID;
        }, mc::addScheduledTask); // hail whoever made this hes a cutie
    }

    public static void drawBlur(final Runnable runnable) {
        if (mc.gameSettings.ofFastRender)
            return;

        StencilUtil.renderStencil(runnable, () -> Blur(12));
    }

    public static void drawBlur(final Runnable runnable, final int intensity) {
        if (mc.gameSettings.ofFastRender)
            return;

        StencilUtil.renderStencil(runnable, () -> Blur(intensity));
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        final boolean needsNewFramebuffer = framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight;

        if (needsNewFramebuffer) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, depth);
        }
        return framebuffer;
    }

    public static void Blur(float radius) {
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);


        blurFramebuffer = createFrameBuffer(blurFramebuffer, false);

        blurFramebuffer.framebufferClear();
        blurFramebuffer.bindFramebuffer(true);
        blurShader.start();
        setupUniformsBlur(1, 0, radius);

        glBindTexture(GL_TEXTURE_2D, mc.getFramebuffer().framebufferTexture);

        blurShader.drawQuads();
        blurFramebuffer.unbindFramebuffer();
        blurShader.stop();

        mc.getFramebuffer().bindFramebuffer(true);
        blurShader.start();
        setupUniformsBlur(0, 1, radius);

        glBindTexture(GL_TEXTURE_2D, blurFramebuffer.framebufferTexture);
        blurShader.drawQuads();
        blurShader.stop();

        glColor4f(1f, 1f, 1f, 1f);
        GlStateManager.bindTexture(0);
    }

    public static void drawRenderHelp(double x, double y){
        drawRect((float) (x - 5), (float) (y - 5), 10,10,Color.RED);
    }

    public static Framebuffer blurBuffer(Framebuffer framebuffer, float radius) {
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);


        blurFramebuffer = createFrameBuffer(blurFramebuffer, false);

        blurFramebuffer.framebufferClear();
        blurFramebuffer.bindFramebuffer(true);
        blurShader.start();
        setupUniformsBlur(1, 0, radius);

        glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture);

        blurShader.drawQuads();
        blurFramebuffer.unbindFramebuffer();
        blurShader.stop();

        mc.getFramebuffer().bindFramebuffer(true);
        blurShader.start();
        setupUniformsBlur(0, 1, radius);

        glBindTexture(GL_TEXTURE_2D, blurFramebuffer.framebufferTexture);
        blurShader.drawQuads();
        blurShader.stop();

        glColor4f(1f, 1f, 1f, 1f);
        GlStateManager.bindTexture(0);
        return blurFramebuffer;
    }

    private static void setupUniformsBlur(float dir1, float dir2, float radius) {
        blurShader.setUniformInteger("textureIn", 0);
        blurShader.setUniformFloat("texelSize", 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        blurShader.setUniformFloat("direction", dir1, dir2);
        blurShader.setUniformFloat("radius", radius);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        OpenGlHelper.glUniform1(blurShader.getUniformf("weights"), weightBuffer);
    }

    private static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    public static void triangle(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3, final Color color) {
        start();

        color(color);
        render(GL_TRIANGLES, () -> {
            glVertex2d(x1, y1);
            glVertex2d(x2, y2);
            glVertex2d(x3, y3);
        });

        stop();
    }

    public static void themeRectangle(final double x, final double y, final double width, final double height, float seconds, float saturation, float brightness) {
        start();

        Theme theme = Ambient.getInstance().getHud().getCurrentTheme();

        render(GL_QUADS, () -> {
            for (double position = x; position <= x + width - 1; position += 1) {
                color(theme.isRainbow()
                        ? ColorUtil.getRainbow(seconds, saturation, brightness, (long) position)
                        : theme.getColor((int) seconds, (int) position));

                glVertex2d(position, y);
                glVertex2d(position + 1, y);
                glVertex2d(position + 1, y + height);
                glVertex2d(position, y + height);
            }
        });

        stop();
    }

    public static void horizontalGradient(final double x, final double y, final double width, final double height, final Color leftColor, final Color rightColor) {
        start();

        glShadeModel(GL_SMOOTH);
        render(GL_QUADS, () -> {
            color(leftColor);
            glVertex2d(x, y);
            glVertex2d(x, y + height);

            color(rightColor);
            glVertex2d(x + width, y + height);
            glVertex2d(x + width, y);
        });
        glShadeModel(GL_FLAT);

        stop();
    }

    public static void verticalGradient(final double x, final double y, final double width, final double height, final Color topColor, final Color bottomColor) {
        start();

        glShadeModel(GL_SMOOTH);
        render(GL_QUADS, () -> {
            color(topColor);
            glVertex2d(x, y);
            glVertex2d(x + width, y);

            color(bottomColor);
            glVertex2d(x + width, y + height);
            glVertex2d(x, y + height);
        });
        glShadeModel(GL_FLAT);

        stop();
    }

    public static void renderScissor(Runnable runnable, final float x, final float y, final float x2, final float y2) {
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int factor = scaledResolution.getScaleFactor();
        glEnable(GL_SCISSOR_TEST);
        glScissor((int) (x * factor), (int) ((scaledResolution.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
        runnable.run();
        glDisable(GL_SCISSOR_TEST);
    }

    public static void renderArrow(float x, float y, Color color, double lineWidth, double arrowLength) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        color(color);
        GL11.glLineWidth((float) lineWidth);

        float halfLength = (float) (arrowLength / 2.0);
        float halfLineWidth = (float) (lineWidth / 2.0);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + halfLength, y - halfLength);

        GL11.glVertex2d(x + halfLength, y - halfLength);
        GL11.glVertex2d(x + arrowLength, y);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    public static void drawQuads() {
        ScaledResolution sr = new ScaledResolution(mc);
        float width = sr.getScaledWidth();
        float height = sr.getScaledHeight();

        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }

    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        GL11.glPushMatrix();
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, zLevel).tex((float) (textureX) * f, (float) (textureY + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, zLevel).tex((float) (textureX + width) * f, (float) (textureY + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, zLevel).tex((float) (textureX + width) * f, (float) (textureY) * f1).endVertex();
        worldrenderer.pos(x, y, zLevel).tex((float) (textureX) * f, (float) (textureY) * f1).endVertex();
        tessellator.draw();
        GL11.glPopMatrix();
    }

    public static void drawOrb(Vec3 position, Color color, float orbRadius) {
        GL11.glPushMatrix();

        GL11.glTranslated(
                position.xCoord - RenderManager.renderPosX,
                position.yCoord - RenderManager.renderPosY,
                position.zCoord - RenderManager.renderPosZ
        );

        GL11.glColor4f(
                color.getRed() / 255.F,
                color.getGreen() / 255.F,
                color.getBlue() / 255.F,
                1f
        );

        orbRadius /= 40;

        int segments = 12;
        for (int lat = 0; lat <= segments; lat++) {
            float theta1 = (float)(lat * Math.PI / segments);
            float theta2 = (float)((lat + 1) * Math.PI / segments);

            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            for (int lon = 0; lon <= segments; lon++) {
                float phi = (float)(lon * 2 * Math.PI / segments);

                float x1 = (float)(Math.sin(theta1) * Math.cos(phi) * orbRadius);
                float y1 = (float)(Math.cos(theta1) * orbRadius);
                float z1 = (float)(Math.sin(theta1) * Math.sin(phi) * orbRadius);
                GL11.glVertex3f(x1, y1, z1);

                float x2 = (float)(Math.sin(theta2) * Math.cos(phi) * orbRadius);
                float y2 = (float)(Math.cos(theta2) * orbRadius);
                float z2 = (float)(Math.sin(theta2) * Math.sin(phi) * orbRadius);
                GL11.glVertex3f(x2, y2, z2);
            }
            GL11.glEnd();
        }

        GL11.glPopMatrix();
    }
}