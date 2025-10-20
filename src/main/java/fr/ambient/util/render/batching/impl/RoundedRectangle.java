package fr.ambient.util.render.batching.impl;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.util.render.ColorUtil;
import fr.ambient.util.render.batching.RenderableShape;
import fr.ambient.util.render.shader.Shader;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

@ExcludeFlow
@ExcludeConstant
public class RoundedRectangle implements RenderableShape {

    private final float x, y, width, height;
    private final float[] color1, color2, color3, color4, radii;

    private RoundedRectangle(float x, float y, float width, float height, float[] radii, Color color1, Color color2, Color color3, Color color4) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.radii = radii;
        this.color1 = ColorUtil.toGLColor(color1);
        this.color2 = ColorUtil.toGLColor(color2);
        this.color3 = ColorUtil.toGLColor(color3);
        this.color4 = ColorUtil.toGLColor(color4);
    }

    public static RoundedRectangle variableCustom(float x, float y, float width, float height, float[] radii, Color color1, Color color2, Color color3, Color color4) {
        return new RoundedRectangle(x, y, width, height, radii, color1, color2, color3, color4);
    }

    public static RoundedRectangle variableSolid(float x, float y, float width, float height, float[] radii, Color color) {
        return new RoundedRectangle(x, y, width, height, radii, color, color, color, color);
    }

    public static RoundedRectangle variableGradient(float x, float y, float width, float height, float[] radii, Color left, Color right) {
        return new RoundedRectangle(x, y, width, height, radii, left, left, right, right);
    }

    public static RoundedRectangle variableVerticalGradient(float x, float y, float width, float height, float[] radii, Color top, Color bottom) {
        return new RoundedRectangle(x, y, width, height, radii, top, bottom, top, bottom);
    }

    public static RoundedRectangle solid(float x, float y, float width, float height, float radius, Color color) {
        return new RoundedRectangle(x, y, width, height, new float[] {radius, radius, radius, radius}, color, color, color, color);
    }

    public static RoundedRectangle horizontalGradient(float x, float y, float width, float height, float radius, Color left, Color right) {
        return new RoundedRectangle(x, y, width, height, new float[] {radius, radius, radius, radius}, left, left, right, right);
    }

    public static RoundedRectangle verticalGradient(float x, float y, float width, float height, float radius, Color top, Color bottom) {
        return new RoundedRectangle(x, y, width, height, new float[] {radius, radius, radius, radius}, top, bottom, top, bottom);
    }

    public static RoundedRectangle custom(float x, float y, float width, float height, float radius, Color color1, Color color2, Color color3, Color color4) {
        return new RoundedRectangle(x, y, width, height, new float[] {radius, radius, radius, radius}, color1, color2, color3, color4);
    }

    @Override
    public void render(Shader shader, ScaledResolution sr) {
        for (int i = 0; i < radii.length; i++) {
            radii[i] *= sr.getScaleFactor();
        }

        shader.setUniformFloat("size", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        shader.setUniformFloat("cornerRadii", radii);
        shader.setUniformFloat("color1", color1[0], color1[1], color1[2], color1[3]);
        shader.setUniformFloat("color2", color2[0], color2[1], color2[2], color2[3]);
        shader.setUniformFloat("color3", color3[0], color3[1], color3[2], color3[3]);
        shader.setUniformFloat("color4", color4[0], color4[1], color4[2], color4[3]);
        shader.drawQuads(x, y, width, height);
    }
}