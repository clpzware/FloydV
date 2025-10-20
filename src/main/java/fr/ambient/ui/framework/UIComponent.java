package fr.ambient.ui.framework;

import fr.ambient.Ambient;
import fr.ambient.module.impl.render.hud.PostProcessing;
import fr.ambient.util.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UIComponent {
    protected float x, y, width, height;
    protected float maxWidth = 0, maxHeight = 0;
    protected float marginLeft = -1, marginTop = -1, marginRight = -1, marginBottom = -1;
    protected float rounding = 0;
    protected Color color1, color2, color3, color4;
    protected List<UIComponent> children = new ArrayList<>();
    protected boolean autoSize = true;
    protected Style style = Style.SOLID;
    protected boolean blur = false, glow = false;
    protected Color glowColor = Color.WHITE;
    protected int blurRadius = 5;

    protected String id = null;

    public UIComponent() {
        this.color1 = new Color(255, 255, 255, 255);
    }

    public UIComponent id(String id) {
        this.id = id;
        return this;
    }

    public UIComponent maxWidth(float maxWidth) {
        maxWidth = maxWidth;
        return this;
    }

    public UIComponent maxHeight(float maxHeight) {
        maxHeight = maxHeight;
        return this;
    }

    public UIComponent position(float x, float y) {
        this.x = x;
        this.y = y;
        this.autoSize = false;
        return this;
    }

    public UIComponent size(float width, float height) {
        this.width = width;
        this.height = height;
        this.autoSize = false;
        return this;
    }

    public UIComponent rounding(float rounding) {
        this.rounding = rounding;
        return this;
    }

    public UIComponent blur(int radius) {
        blur = true;
        blurRadius = radius;
        return this;
    }

    public UIComponent glow(Color color) {
        glow = true;
        glowColor = color;
        return this;
    }

    public UIComponent color(Style style, Color... colors) {
        this.style = style;

        if (colors.length > 0) this.color1 = colors[0];
        if (colors.length > 1) this.color2 = colors[1];
        if (colors.length > 2) this.color3 = colors[2];
        if (colors.length > 3) this.color4 = colors[3];

        return this;
    }

    public UIComponent margin(float left, float top, float right, float bottom) {
        this.marginLeft = left;
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
        return this;
    }

    public UIComponent children(UIComponent... components) {
        this.children.clear();
        Collections.addAll(children, components);
        return this;
    }

    public void layout() {
        children.forEach(child -> child.layout(x, y, width, height));
    }

    public void layout(float parentX, float parentY, float parentWidth, float parentHeight) {
        if (autoSize && marginLeft >= 0 && marginTop >= 0 && marginRight >= 0 && marginBottom >= 0) {
            x = parentX + marginLeft;
            y = parentY + marginTop;

            float availableWidth = parentWidth - (marginLeft + marginRight);
            float availableHeight = parentHeight - (marginTop + marginBottom);

            width = maxWidth > 0 && availableWidth > maxWidth ? maxWidth : Math.max(0, availableWidth);
            height = maxHeight > 0 && availableHeight > maxHeight ? maxHeight : Math.max(0, availableHeight);
        }
    }

    public void render(double scale) {
        layout();

        float centerX = x + width / 2f;
        float centerY = y + height / 2f;
        float scaledWidth = width * (float) scale;
        float scaledHeight = height * (float) scale;

        float rectX = centerX - scaledWidth / 2f;
        float rectY = centerY - scaledHeight / 2f;

        RenderUtil.scale(() -> {
            switch (style) {
                case SOLID:
                    RenderUtil.drawRoundedRect(x, y, width, height, rounding, color1);
                    break;
                case HORIZONTAL:
                    RenderUtil.drawRoundGradient(x, y, width, height, rounding, color1, color2);
                    break;
                case VERTICAL:
                    RenderUtil.drawRoundVertGradient(x, y, width, height, rounding, color1, color2);
                    break;
                case CUSTOM:
                    RenderUtil.drawRoundedRect(x, y, width, height, rounding, color1, color2, color3, color4);
                    break;
            }

            children.forEach(UIComponent::render);
        }, x + width / 2f, y + height / 2f, scale);
    }

    public void render() {
        render(1.0f);
    }

    public <T extends UIComponent> T findChild(Class<T> type, String id) {
        for (UIComponent child : children) {
            if (type.isInstance(child) && (id == null || id.equals(child.id))) {
                return type.cast(child);
            }
        }
        return null;
    }
}