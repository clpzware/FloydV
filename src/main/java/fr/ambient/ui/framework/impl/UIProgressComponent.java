package fr.ambient.ui.framework.impl;

import fr.ambient.ui.framework.UIComponent;
import fr.ambient.util.render.RenderUtil;

import java.awt.*;

public class UIProgressComponent extends UIComponent {

    private float progress;
    private Color background;

    public UIProgressComponent progress(float progress) {
        this.progress = Math.max(0, Math.min(progress, 1));
        return this;
    }

    public UIProgressComponent background(int r, int g, int b, int a) {
        this.background = new Color(r, g, b, a);
        return this;
    }

    public UIProgressComponent background(Color background) {
        this.background = background;
        return this;
    }

    @Override
    public void render() {
        RenderUtil.drawRoundedRect(x, y, width, height, rounding, background);

        switch (style) {
            case SOLID:
                RenderUtil.drawRoundedRect(x, y, width * progress, height, rounding, color1);
                break;
            case HORIZONTAL:
                RenderUtil.drawRoundGradient(x, y, width * progress, height, rounding, color1, color2);
                break;
            case VERTICAL:
                RenderUtil.drawRoundVertGradient(x, y, width * progress, height, rounding, color1, color2);
                break;
            case CUSTOM:
                RenderUtil.drawRoundedRect(x, y, width * progress, height, rounding, color1, color2, color3, color4);
                break;
        }
    }
}
