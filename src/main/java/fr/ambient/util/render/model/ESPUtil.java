package fr.ambient.util.render.model;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.Ambient;
import fr.ambient.util.InstanceAccess;
import fr.ambient.util.math.MathsUtil;
import fr.ambient.util.render.ColorUtil;
import fr.ambient.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjglx.Sys;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@ExcludeFlow
@ExcludeConstant
public class ESPUtil implements InstanceAccess {

    public static java.util.List<Vector3d> getVectors(EntityPlayer player) {
        final AxisAlignedBB boundingBox = getAxisAlignedBB(player);

        return Arrays.asList(
                new Vector3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vector3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vector3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vector3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vector3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vector3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vector3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vector3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        );
    }

    private static AxisAlignedBB getAxisAlignedBB(EntityPlayer player) {
        final double renderX = RenderManager.renderPosX;
        final double renderY = RenderManager.renderPosY;
        final double renderZ = RenderManager.renderPosZ;

        final double interpX = player.lastTickPosX + (player.posX - player.lastTickPosX) * mc.timer.renderPartialTicks - renderX;
        final double interpY = player.lastTickPosY + (player.posY - player.lastTickPosY) * mc.timer.renderPartialTicks - renderY;
        final double interpZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * mc.timer.renderPartialTicks - renderZ;

        final double halfWidth = (player.width + 0.14) / 2;
        final double heightOffset = player.isSneaking() ? -0.1 : 0.2;
        final double playerHeight = player.height + heightOffset + 0.01;

        return new AxisAlignedBB(
                interpX - halfWidth, interpY, interpZ - halfWidth,
                interpX + halfWidth, interpY + playerHeight, interpZ + halfWidth
        );
    }

    public static void point(Vec3 pos, Color color, float size) {
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.disableCull();
        GL11.glDepthMask(false);
        setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
        drawBoundingBox(getDaFuckingRenderPosAxisAlignedFromBB(new AxisAlignedBB(pos.xCoord - size, pos.yCoord - size, pos.zCoord - size, pos.xCoord + size, pos.yCoord + size, pos.zCoord + size)));
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GlStateManager.enableCull();
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static AxisAlignedBB getDaFuckingRenderPosAxisAlignedFromBB(AxisAlignedBB axisAlignedBB) {

        double x1 = axisAlignedBB.minX - RenderManager.renderPosX;
        double y1 = axisAlignedBB.minY - RenderManager.renderPosY;
        double z1 = axisAlignedBB.minZ - RenderManager.renderPosZ;

        double x2 = axisAlignedBB.maxX - RenderManager.renderPosX;
        double y2 = axisAlignedBB.maxY - RenderManager.renderPosY;
        double z2 = axisAlignedBB.maxZ - RenderManager.renderPosZ;

        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    public static void drawPathLine(ArrayList<Vec3> positions, float lineWidth, Color color) {
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


        for (Vec3 vec3 : positions) {
            GL11.glColor4f(color.getRed() / 255.F,
                    color.getGreen() / 255.F,
                    color.getBlue() / 255.F, 1f);
            GL11.glVertex3d(vec3.xCoord - RenderManager.renderPosX, vec3.yCoord - RenderManager.renderPosY, vec3.zCoord - RenderManager.renderPosZ);
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

    public static void drawPathLine(ArrayList<Vec3> positions, float lineWidth) {
        if (positions.size() < 2) return;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(lineWidth);

        ArrayList<Vec3> interpolatedPoints = interpolateCatmullRom(positions, 10);

        GL11.glBegin(GL11.GL_LINE_STRIP);

        for (Vec3 vec3 : interpolatedPoints) {
            Color color = Ambient.getInstance().getHud().getCurrentTheme().getColor(50, (int) (vec3.xCoord * vec3.yCoord));

            GL11.glColor4f(color.getRed() / 255.F,
                    color.getGreen() / 255.F,
                    color.getBlue() / 255.F, 1f);
            GL11.glVertex3d(vec3.xCoord - RenderManager.renderPosX,
                    vec3.yCoord - RenderManager.renderPosY,
                    vec3.zCoord - RenderManager.renderPosZ);
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private static Vec3 catmullRomSpline(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;

        float x = (float) (0.5f * (
                        (2 * p1.xCoord) +
                                (-p0.xCoord + p2.xCoord) * t +
                                (2 * p0.xCoord - 5 * p1.xCoord + 4 * p2.xCoord - p3.xCoord) * t2 +
                                (-p0.xCoord + 3 * p1.xCoord - 3 * p2.xCoord + p3.xCoord) * t3
                ));

        float y = (float) (0.5f * (
                        (2 * p1.yCoord) +
                                (-p0.yCoord + p2.yCoord) * t +
                                (2 * p0.yCoord - 5 * p1.yCoord + 4 * p2.yCoord - p3.yCoord) * t2 +
                                (-p0.yCoord + 3 * p1.yCoord - 3 * p2.yCoord + p3.yCoord) * t3
                ));

        float z = (float) (0.5f * (
                        (2 * p1.zCoord) +
                                (-p0.zCoord + p2.zCoord) * t +
                                (2 * p0.zCoord - 5 * p1.zCoord + 4 * p2.zCoord - p3.zCoord) * t2 +
                                (-p0.zCoord + 3 * p1.zCoord - 3 * p2.zCoord + p3.zCoord) * t3
                ));

        return new Vec3(x, y, z);
    }

    private static ArrayList<Vec3> interpolateCatmullRom(ArrayList<Vec3> points, int subdivisions) {
        ArrayList<Vec3> interpolatedPoints = new ArrayList<>();

        if (points.size() < 2) return points;

        interpolatedPoints.add(points.getFirst());

        for (int i = 1; i < points.size(); i++) {
            Vec3 p0 = points.get(i - 1);
            Vec3 p1 = points.get(i);
            Vec3 p2 = (i < points.size() - 1) ? points.get(i + 1) : p1;
            Vec3 p3 = (i < points.size() - 2) ? points.get(i + 2) : p2;

            for (int j = 1; j <= subdivisions; j++) {
                float t = (float) j / subdivisions;
                Vec3 interpolatedPoint = catmullRomSpline(p0, p1, p2, p3, t);
                interpolatedPoints.add(interpolatedPoint);
            }
        }

        return interpolatedPoints;
    }

    public static void drawPathOrbs(ArrayList<Vec3> positions, float orbRadius) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glEnable(GL11.GL_DEPTH_TEST);

        for (Vec3 vec3 : positions) {
            Color color = Ambient.getInstance().getHud().getCurrentTheme().getColor(6, positions.indexOf(vec3) * 6);
            RenderUtil.drawOrb(vec3, color, orbRadius);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    public static void drawBoundingBox(AxisAlignedBB a) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(a.minX, a.minY, a.minZ);
        GL11.glVertex3d(a.minX, a.minY, a.maxZ);
        GL11.glVertex3d(a.minX, a.maxY, a.maxZ);
        GL11.glVertex3d(a.minX, a.maxY, a.minZ);

        GL11.glVertex3d(a.minX, a.minY, a.maxZ);
        GL11.glVertex3d(a.maxX, a.minY, a.maxZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.maxZ);
        GL11.glVertex3d(a.minX, a.maxY, a.maxZ);

        GL11.glVertex3d(a.maxX, a.minY, a.maxZ);
        GL11.glVertex3d(a.maxX, a.minY, a.minZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.minZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.maxZ);

        GL11.glVertex3d(a.maxX, a.minY, a.minZ);
        GL11.glVertex3d(a.minX, a.minY, a.minZ);
        GL11.glVertex3d(a.minX, a.maxY, a.minZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.minZ);

        GL11.glVertex3d(a.minX, a.maxY, a.minZ);
        GL11.glVertex3d(a.minX, a.maxY, a.maxZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.maxZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.minZ);

        GL11.glVertex3d(a.minX, a.minY, a.minZ);
        GL11.glVertex3d(a.minX, a.minY, a.maxZ);
        GL11.glVertex3d(a.maxX, a.minY, a.maxZ);
        GL11.glVertex3d(a.maxX, a.minY, a.minZ);
        GL11.glEnd();
    }

    public static void drawBoundingBoxOutline(AxisAlignedBB a) {
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINES);

        GL11.glVertex3d(a.minX, a.minY, a.minZ);
        GL11.glVertex3d(a.maxX, a.minY, a.minZ);

        GL11.glVertex3d(a.maxX, a.minY, a.minZ);
        GL11.glVertex3d(a.maxX, a.minY, a.maxZ);

        GL11.glVertex3d(a.maxX, a.minY, a.maxZ);
        GL11.glVertex3d(a.minX, a.minY, a.maxZ);

        GL11.glVertex3d(a.minX, a.minY, a.maxZ);
        GL11.glVertex3d(a.minX, a.minY, a.minZ);

        GL11.glVertex3d(a.minX, a.maxY, a.minZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.minZ);

        GL11.glVertex3d(a.maxX, a.maxY, a.minZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.maxZ);

        GL11.glVertex3d(a.maxX, a.maxY, a.maxZ);
        GL11.glVertex3d(a.minX, a.maxY, a.maxZ);

        GL11.glVertex3d(a.minX, a.maxY, a.maxZ);
        GL11.glVertex3d(a.minX, a.maxY, a.minZ);

        GL11.glVertex3d(a.minX, a.minY, a.minZ);
        GL11.glVertex3d(a.minX, a.maxY, a.minZ);

        GL11.glVertex3d(a.maxX, a.minY, a.minZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.minZ);

        GL11.glVertex3d(a.maxX, a.minY, a.maxZ);
        GL11.glVertex3d(a.maxX, a.maxY, a.maxZ);

        GL11.glVertex3d(a.minX, a.minY, a.maxZ);
        GL11.glVertex3d(a.minX, a.maxY, a.maxZ);

        GL11.glEnd();
        GL11.glDisable(GL_LINE_SMOOTH);
    }


    public static void drawFilledHitbox(AxisAlignedBB bb, Color color, float transparency) {
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.disableCull();
        GL11.glDepthMask(false);
        setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (transparency * 255)));
        drawBoundingBox(bb);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GlStateManager.enableCull();
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static AxisAlignedBB getDaFuckingRenderPosAxisAlignedWithMargin(Entity entity, float expand) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();

        bb = bb.expand(expand, expand, expand);

        float margin = (float) Math.abs(bb.maxX - bb.minX) / 2f;
        float entityheight = (float) Math.abs(bb.minY - bb.maxY);

        double x1 = (entity.lastTickPosX - margin + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks) - RenderManager.renderPosX;
        double y1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks) - RenderManager.renderPosY;
        double z1 = (entity.lastTickPosZ - margin + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks) - RenderManager.renderPosZ;

        double x2 = (entity.lastTickPosX + margin + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks) - RenderManager.renderPosX;
        double y2 = (entity.lastTickPosY + entityheight + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks) - RenderManager.renderPosY;
        double z2 = (entity.lastTickPosZ + margin + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks) - RenderManager.renderPosZ;
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    public static AxisAlignedBB getESPFromVec3(Vec3 position, float expand, float height) {
        double x1 = position.xCoord - expand - RenderManager.renderPosX;
        double y1 = position.yCoord - RenderManager.renderPosY;
        double z1 = position.zCoord - expand - RenderManager.renderPosZ;

        double x2 = position.xCoord + expand - RenderManager.renderPosX;
        double y2 = position.yCoord + height - RenderManager.renderPosY;
        double z2 = position.zCoord + expand - RenderManager.renderPosZ;
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    public static AxisAlignedBB getESPFromVec3(Vec3 position, float expand) {
        double x1 = position.xCoord - expand - RenderManager.renderPosX;
        double y1 = position.yCoord - expand - RenderManager.renderPosY;
        double z1 = position.zCoord - expand - RenderManager.renderPosZ;

        double x2 = position.xCoord + expand - RenderManager.renderPosX;
        double y2 = position.yCoord + expand - RenderManager.renderPosY;
        double z2 = position.zCoord + expand - RenderManager.renderPosZ;
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    public static AxisAlignedBB getRMX(AxisAlignedBB bb) {

        return new AxisAlignedBB(
                bb.minX - RenderManager.renderPosX,
                bb.minY - RenderManager.renderPosY,
                bb.minZ - RenderManager.renderPosZ,
                bb.maxX - RenderManager.renderPosX,
                bb.maxY - RenderManager.renderPosY,
                bb.maxZ - RenderManager.renderPosZ
        );

    }

    public static void filledBlockESP(BlockPos pos, Color color, float transparency) {
        start();
        setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (transparency * 255)));

        Block block = mc.theWorld.getBlockState(pos).getBlock();
        AxisAlignedBB boundingBox = block.getSelectedBoundingBox(mc.theWorld, pos);

        double x1 = boundingBox.minX - RenderManager.renderPosX;
        double y1 = boundingBox.minY - RenderManager.renderPosY;
        double z1 = boundingBox.minZ - RenderManager.renderPosZ;

        double x2 = boundingBox.maxX - RenderManager.renderPosX;
        double y2 = boundingBox.maxY - RenderManager.renderPosY;
        double z2 = boundingBox.maxZ - RenderManager.renderPosZ;

        drawBoundingBox(new AxisAlignedBB(x1, y1, z1, x2, y2, z2));

        stop();
    }

    public static void outlinedBlockESP(BlockPos pos, Color color, float transparency) {
        start();
        setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (transparency * 255)));

        Block block = mc.theWorld.getBlockState(pos).getBlock();
        AxisAlignedBB boundingBox = block.getSelectedBoundingBox(mc.theWorld, pos);

        double x1 = boundingBox.minX - RenderManager.renderPosX;
        double y1 = boundingBox.minY - RenderManager.renderPosY;
        double z1 = boundingBox.minZ - RenderManager.renderPosZ;

        double x2 = boundingBox.maxX - RenderManager.renderPosX;
        double y2 = boundingBox.maxY - RenderManager.renderPosY;
        double z2 = boundingBox.maxZ - RenderManager.renderPosZ;

        drawBoundingBoxOutline(new AxisAlignedBB(x1, y1, z1, x2, y2, z2));

        stop();
    }

    public static void breakProgress(BlockPos pos, Color color, float progress, float transparency) {
        start();
        setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (transparency * 255)));

        Block block = mc.theWorld.getBlockState(pos).getBlock();
        AxisAlignedBB boundingBox = block.getSelectedBoundingBox(mc.theWorld, pos);

        double x1 = boundingBox.minX - RenderManager.renderPosX;
        double y1 = boundingBox.minY - RenderManager.renderPosY;
        double z1 = boundingBox.minZ - RenderManager.renderPosZ;

        double x2 = boundingBox.maxX - RenderManager.renderPosX;
        double y2 = boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * progress - RenderManager.renderPosY;
        double z2 = boundingBox.maxZ - RenderManager.renderPosZ;

        drawBoundingBox(new AxisAlignedBB(x1, y1, z1, x2, y2, z2));

        stop();
    }

    public static void renderFilledBB(AxisAlignedBB boundingBox, Color color, float transparency, boolean outline) {
        start();

        setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (transparency * 255)));

        double x1 = boundingBox.minX - RenderManager.renderPosX;
        double y1 = boundingBox.minY - RenderManager.renderPosY;
        double z1 = boundingBox.minZ - RenderManager.renderPosZ;

        double x2 = boundingBox.maxX - RenderManager.renderPosX;
        double y2 = boundingBox.maxY - RenderManager.renderPosY;
        double z2 = boundingBox.maxZ - RenderManager.renderPosZ;

        drawBoundingBox(new AxisAlignedBB(x1, y1, z1, x2, y2, z2));

        if (outline) {
            setColor(color);
            drawBoundingBoxOutline(new AxisAlignedBB(x1, y1, z1, x2, y2, z2));
        }

        stop();
    }

    public static void filledInterpolatedESP(Entity entity, Color color, float transparency, float margin) {
        start();
        setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (transparency * 255)));
        drawBoundingBox(getDaFuckingRenderPosAxisAlignedWithMargin(entity, margin));
        stop();
    }


    public static void setColor(Color color) {
        float alpha = (color.getRGB() >> 24 & 0xFF) / 255.0F;
        float red = (color.getRGB() >> 16 & 0xFF) / 255.0F;
        float green = (color.getRGB() >> 8 & 0xFF) / 255.0F;
        float blue = (color.getRGB() & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
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

    public static void renderSkeleton(EntityPlayer player, float[][] modelRots, float partialTicks) {
        final float lineWidth = 2.0f;
        final float radianConversionFactor = (float) (180.0 / Math.PI);
        final float legOffsetSneak = -0.235f;
        final float legOffsetNormal = 0.0f;
        final double armYOffset = 0.55D;

        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_POLYGON_SMOOTH);
        glEnable(GL_POINT_SMOOTH);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
        glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_TEXTURE_2D);
        glPushMatrix();
        glLineWidth(lineWidth);

        ColorUtil.setColor(Ambient.getInstance().getHud().getCurrentTheme().getColor2());

        double posX = MathsUtil.interpolate(player.prevPosX, player.posX, partialTicks) - RenderManager.viewerPosX;
        double posY = MathsUtil.interpolate(player.prevPosY, player.posY, partialTicks) - RenderManager.viewerPosY;
        double posZ = MathsUtil.interpolate(player.prevPosZ, player.posZ, partialTicks) - RenderManager.viewerPosZ;
        glTranslated(posX, posY, posZ);

        float bodyYawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * mc.timer.renderPartialTicks;
        glRotatef(-bodyYawOffset, 0.0f, 1.0f, 0.0f);
        glTranslated(0.0, 0.0, player.isSneaking() ? legOffsetSneak : legOffsetNormal);

        float adjustedLegHeight = player.isSneaking() ? 0.6f : 0.75f;

        renderLimb(-0.125, adjustedLegHeight, modelRots[3], radianConversionFactor);
        renderLimb(0.125, adjustedLegHeight, modelRots[4], radianConversionFactor);

        glTranslated(0.0, 0.0, player.isSneaking() ? 0.25 : 0.0);
        glPushMatrix();
        glTranslated(0.0, player.isSneaking() ? -0.05 : 0.0, player.isSneaking() ? -0.01725 : 0.0);

        renderLimb(-0.375, adjustedLegHeight + armYOffset, modelRots[1], radianConversionFactor);
        renderLimb(0.375, adjustedLegHeight + armYOffset, modelRots[2], radianConversionFactor);

        glRotatef(bodyYawOffset - player.rotationYawHead, 0.0f, 1.0f, 0.0f);
        renderLimb(0.0, adjustedLegHeight + armYOffset, modelRots[0], radianConversionFactor, 0.3);

        glPopMatrix();

        if (player.isSneaking()) {
            glRotatef(25.0f, 1.0f, 0.0f, 0.0f);
            glTranslated(0.0, -0.16175, -0.48025);
        }

        renderBodyCenter(adjustedLegHeight);
        renderSpine(adjustedLegHeight, armYOffset);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_POLYGON_SMOOTH);
        glEnable(GL_POINT_SMOOTH);
        glPopMatrix();
    }

    private static void renderLimb(double x, double y, float[] rotations, float radianFactor) {
        renderLimb(x, y, rotations, radianFactor, -0.5);
    }

    private static void renderLimb(double x, double y, float[] rotations, float radianFactor, double length) {
        glPushMatrix();
        glTranslated(x, y, 0.0);
        applyRotations(rotations, radianFactor);
        glBegin(GL_LINES);
        glVertex3d(0.0, 0.0, 0.0);
        glVertex3d(0.0, length, 0.0);
        glEnd();
        glPopMatrix();
    }

    private static void applyRotations(float[] rotations, float radianFactor) {
        if (rotations[0] != 0.0f) glRotatef(rotations[0] * radianFactor, 1.0f, 0.0f, 0.0f);
        if (rotations[1] != 0.0f) glRotatef(rotations[1] * radianFactor, 0.0f, 1.0f, 0.0f);
        if (rotations[2] != 0.0f) glRotatef(rotations[2] * radianFactor, 0.0f, 0.0f, 1.0f);
    }

    private static void renderBodyCenter(float legHeight) {
        glPushMatrix();
        glTranslated(0.0, legHeight, 0.0);
        glBegin(GL_LINES);
        glVertex3d(-0.125, 0.0, 0.0);
        glVertex3d(0.125, 0.0, 0.0);
        glEnd();
        glPopMatrix();
    }

    private static void renderSpine(float legHeight, double armYOffset) {
        glPushMatrix();
        glTranslated(0.0, legHeight, 0.0);
        glBegin(GL_LINES);
        glVertex3d(0.0, 0.0, 0.0);
        glVertex3d(0.0, 0.55, 0.0);
        glEnd();
        glPopMatrix();

        glPushMatrix();
        glTranslated(0.0, legHeight + armYOffset, 0.0);
        glBegin(GL_LINES);
        glVertex3d(-0.375, 0.0, 0.0);
        glVertex3d(0.375, 0.0, 0.0);
        glEnd();
        glPopMatrix();
    }

    public static void drawCircle(Entity entity, double rad, float divisor, float lineWidth, Color color) {
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

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.renderPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.renderPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.renderPosZ;

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


    public static void drawCirclePercentage(Entity entity, double rad, float divisor, float lineWidth,float showPercentage, Color color, Color color2) {
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

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.renderPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.renderPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.renderPosZ;

        for (float i = 0; i < Math.PI * 2; i += (float) (Math.PI * 2 / divisor)) {
            double percentage = i / (Math.PI * 2);
            Color activeColor = percentage > showPercentage ? color2 : color;
            final double vecX = x + rad * Math.cos(i);
            final double vecZ = z + rad * Math.sin(i);

            GL11.glColor4f(activeColor.getRed() / 255.F,
                    activeColor.getGreen() / 255.F,
                    activeColor.getBlue() / 255.F, 1f);

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
}
