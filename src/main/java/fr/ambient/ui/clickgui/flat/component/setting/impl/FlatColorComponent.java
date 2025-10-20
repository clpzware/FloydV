package fr.ambient.ui.clickgui.flat.component.setting.impl;

import fr.ambient.Ambient;
import fr.ambient.property.impl.ColorProperty;
import fr.ambient.ui.clickgui.flat.component.setting.FlatSettingComponent;
import fr.ambient.util.render.MaterialThemePicker;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;

import java.awt.*;

public class FlatColorComponent extends FlatSettingComponent<ColorProperty> {

    private boolean open = false;

    private boolean draggingHue = false;
    private boolean draggingSB = false;

    private float hue;
    private float brightness;
    private float saturation;

    private Animation openAnimation = new Animation(Easing.EASE_IN_OUT_SINE, 150);
    private Animation hueAnimation = new Animation(Easing.LINEAR, 50);
    private Animation sbXAnimation = new Animation(Easing.LINEAR, 50);
    private Animation sbYAnimation = new Animation(Easing.LINEAR, 50);

    public FlatColorComponent(ColorProperty property) {
        super(property);
        this.height = 20;
        updateHSBValues();
    }

    public void render(int mouseX, int mouseY) {
        updateHSBValues();

        openAnimation.run(open ? 1 : 0);
        height = 20 + (float) (openAnimation.getValue() * 80);

        MaterialThemePicker.MaterialTheme theme = MaterialThemePicker.findClosestTheme(
                Ambient.getInstance().getHud().getCurrentTheme().color2
        );

        int lighterColor = theme.getShade(100).getRGB();

        Fonts.getRobotoRegular(17).drawString(property.getLabel(), x + 7, y + 7f, lighterColor);

        Color topLeft = Color.getHSBColor(hue, 0.0f, 1.0f);
        Color topRight = Color.getHSBColor(hue, 1.0f, 1.0f);
        Color bottomLeft = Color.getHSBColor(hue, 0.0f, 0.0f);
        Color bottomRight = Color.getHSBColor(hue, 1.0f, 0.0f);

        RenderUtil.drawRoundedRect(x + width - 18, y + 5, 10, 10, 2, property.getValue());
        inGameImages.get("pencil").drawImg(x + width - 16.5f, y + 6.5f, 7, 7, property.getValue().darker());

        if (open || openAnimation.getValue() > 0) {
            RenderUtil.renderScissor(() -> {
                float hsbHeight = height - 22f;
                float sbTargetX = Math.min(x + 7 + (width - 29) * saturation, x + width - 28);
                float sbTargetY = Math.min(y + 20 + (hsbHeight * (1 - brightness)), y + 14 + hsbHeight);
                sbXAnimation.run(sbTargetX);
                sbYAnimation.run(sbTargetY);

                RenderUtil.drawRoundedRect(x + 7, y + 20, width - 29, hsbHeight, 3, topLeft, bottomLeft, topRight, bottomRight);
                RenderUtil.drawCircle(
                        sbXAnimation.getFloatValue(),
                        sbYAnimation.getFloatValue(),
                        6, Color.WHITE
                );

                float targetHeight = hsbHeight * hue;
                hueAnimation.run(targetHeight);

                RenderUtil.drawRoundHue(x + width - 18, y + 20, 10, hsbHeight, 2);
                RenderUtil.drawCircle(x + width - 16,
                        Math.min(y + 20 + hueAnimation.getFloatValue(), y + 14 + hsbHeight),
                        6, Color.WHITE);
            }, x, y + 20, x + width, y + height);
        }
    }

    @Override
    public boolean click(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (mouseY <= y + 18) {
                open = !open;
            } else if (openAnimation.isFinished()) {
                float hsbWidth = width - 29;
                float hsbHeight = height - 22f;

                if (mouseX >= x + width - 18 && mouseX <= x + width - 8) {
                    draggingHue = true;
                    hue = Math.max(0, Math.min((mouseY - (y + 20)) / hsbHeight, 1.0f));
                    updateCurrentColor();
                } else if (mouseX >= x + 7 && mouseX <= x + width - 29) {
                    draggingSB = true;
                    brightness = Math.max(0, Math.min((y + 20 + hsbHeight - mouseY) / hsbHeight, 1.0f));
                    saturation = Math.max(0, Math.min((mouseX - (x + 7)) / hsbWidth, 1.0f));
                    updateCurrentColor();
                }
            }
        }
        return hovered;
    }

    @Override
    public void drag(int mouseX, int mouseY, int button, long lastClick) {
        if (draggingHue) {
            float hsbHeight = height - 22f;
            hue = Math.max(0, Math.min((mouseY - (y + 20)) / hsbHeight, 1.0f));
            updateCurrentColor();
        }

        if (draggingSB) {
            float hsbWidth = width - 29;
            float hsbHeight = height - 22f;
            brightness = Math.max(0, Math.min((y + 20 + hsbHeight - mouseY) / hsbHeight, 1.0f));
            saturation = Math.max(0, Math.min((mouseX - (x + 7)) / hsbWidth, 1.0f));
            updateCurrentColor();
        }
    }

    @Override
    public void release(int mouseX, int mouseY, int state) {
        draggingHue = false;
        draggingSB = false;
    }


    private void updateHSBValues() {
        int red = property.getValue().getRed();
        int green = property.getValue().getGreen();
        int blue = property.getValue().getBlue();
        float[] hsbValues = Color.RGBtoHSB(red, green, blue, null);
        hue = hsbValues[0];
        saturation = hsbValues[1];
        brightness = hsbValues[2];
    }

    private void updateCurrentColor() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        property.setValue(new Color(rgb));
    }
}
