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
public class RoundedOutline implements RenderableShape {

    private final float x, y, width, height, radius, strokeWidth;
    private final float[] color1, color2, color3, color4;

    private RoundedOutline(float x, float y, float width, float height, float radius, float strokeWidth, Color color1, Color color2, Color color3, Color color4) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.strokeWidth = strokeWidth;
        this.color1 = ColorUtil.toGLColor(color1);
        this.color2 = ColorUtil.toGLColor(color2);
        this.color3 = ColorUtil.toGLColor(color3);
        this.color4 = ColorUtil.toGLColor(color4);
    }

    public static RoundedOutline solid(float x, float y, float width, float height, float radius, float strokeWidth,Color color) {
        return new RoundedOutline(x, y, width, height, radius, strokeWidth, color, color, color, color);
    }

    public static RoundedOutline horizontalGradient(float x, float y, float width, float height, float radius, float strokeWidth,Color left, Color right) {
        return new RoundedOutline(x, y, width, height, radius, strokeWidth, left, left, right, right);
    }

    public static RoundedOutline verticalGradient(float x, float y, float width, float height, float radius, float strokeWidth,Color top, Color bottom) {
        return new RoundedOutline(x, y, width, height, radius, strokeWidth, top, bottom, top, bottom);
    }

    public static RoundedOutline custom(float x, float y, float width, float height, float radius, Color color1, float strokeWidth,Color color2, Color color3, Color color4) {
        return new RoundedOutline(x, y, width, height, radius, strokeWidth, color1, color2, color3, color4);
    }

    @Override
    public void render(Shader shader, ScaledResolution sr) {
        shader.setUniformFloat("size", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        shader.setUniformFloat("round", radius * sr.getScaleFactor());
        shader.setUniformFloat("width", strokeWidth);
        shader.setUniformFloat("color1", color1[0], color1[1], color1[2], color1[3]);
        shader.setUniformFloat("color2", color2[0], color2[1], color2[2], color2[3]);
        shader.setUniformFloat("color3", color3[0], color3[1], color3[2], color3[3]);
        shader.setUniformFloat("color4", color4[0], color4[1], color4[2], color4[3]);
        shader.drawQuads(x, y, width, height);
    }
}