package fr.ambient.ui.clickgui.flat.component.setting.impl;

import fr.ambient.Ambient;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.ui.clickgui.flat.component.setting.FlatSettingComponent;
import fr.ambient.util.render.MaterialThemePicker;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;

import java.awt.*;

public class FlatNumberComponent extends FlatSettingComponent<NumberProperty> {

    private boolean dragging = false;
    private final Animation offsetAnimation = new Animation(Easing.LINEAR, 100);
    private static final int HEIGHT = 32;
    private static final int SLIDER_HEIGHT = 4;
    private static final int SLIDER_Y_OFFSET = 20;
    private static final int HANDLE_WIDTH = 4;
    private static final int HANDLE_HEIGHT = 9;

    public FlatNumberComponent(NumberProperty property) {
        super(property);
        this.height = HEIGHT;
    }

    public void render(int mouseX, int mouseY) {
        MaterialThemePicker.MaterialTheme theme = MaterialThemePicker.findClosestTheme(Ambient.getInstance().getHud().getCurrentTheme().color2);

        Color color = theme.getShade(300);
        int lighter = theme.getShade(100).getRGB();

        this.height = HEIGHT;
        drawLabel(lighter);
        drawValue(color.getRGB());

        float sliderStartX = x + 7;
        float sliderWidth = width - 14;
        float mouseRelativeX = mouseX - sliderStartX;

        float minValue = property.getMinimumValue();
        float maxValue = property.getMaximumValue();

        if (dragging) {
            updatePropertyValue(mouseRelativeX, sliderWidth, minValue, maxValue);
        }

        float targetWidth = sliderWidth * ((property.getValue() - minValue) / (maxValue - minValue));
        offsetAnimation.run(targetWidth);

        drawSliderBackground();
        drawSliderForeground((float) offsetAnimation.getValue(), color);
        drawSliderHandle((float) offsetAnimation.getValue(), color);
    }

    private void drawLabel(int color) {
        Fonts.getRobotoRegular(18).drawString(property.getLabel(), x + 7, y + 7f, color);
    }

    private void drawValue(int color) {
        String value = String.valueOf(property.getValue());
        float valueWidth = Fonts.getRobotoRegular(18).getWidth(value);
        Fonts.getRobotoRegular(18).drawString(value, x + width - 7 - valueWidth, y + 7f, color);
    }

    private void updatePropertyValue(float mouseRelativeX, float sliderWidth, float minValue, float maxValue) {
        float clampedX = Math.min(sliderWidth, Math.max(0, mouseRelativeX));
        float newValue = (clampedX / sliderWidth) * (maxValue - minValue) + minValue;
        property.setValue(newValue);
    }

    private void drawSliderBackground() {
        RenderUtil.drawRoundedRect(x + 7, y + SLIDER_Y_OFFSET, width - 14, SLIDER_HEIGHT, 2f, new Color(0x252525));
    }

    private void drawSliderForeground(float currentWidth, Color color) {
        RenderUtil.drawRoundedRect(x + 7, y + SLIDER_Y_OFFSET, currentWidth, SLIDER_HEIGHT, 2f, color);
    }

    private void drawSliderHandle(float currentWidth, Color color) {
        float handleX = x + currentWidth + 3;
        handleX = Math.max(x + 7, Math.min(handleX, x + width - 11));
        RenderUtil.drawRoundedRect(handleX, y + SLIDER_Y_OFFSET - 2.5f, HANDLE_WIDTH, HANDLE_HEIGHT, 2f, color);
        RenderUtil.drawRoundedRect(handleX + 1, y + SLIDER_Y_OFFSET - 1.5f, 2, 7, 1f, new Color(0x252525));
    }

    public boolean click(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && mouseX >= x + 7 && button == 0) {
            dragging = true;
        }
        super.click(mouseX, mouseY, button);
        return hovered;
    }

    @Override
    public void release(int mouseX, int mouseY, int state) {
        dragging = false;
        super.release(mouseX, mouseY, state);
    }
}
