package fr.ambient.ui.clickgui.flat.component.setting.impl;


import fr.ambient.Ambient;
import fr.ambient.property.impl.MultiProperty;
import fr.ambient.ui.clickgui.flat.component.setting.FlatSettingComponent;
import fr.ambient.util.render.MaterialThemePicker;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FlatMultiComponent extends FlatSettingComponent<MultiProperty> {

    private boolean open = false;

    private Animation openAnimation = new Animation(Easing.EASE_IN_OUT_SINE, 150);
    private Animation rotAnimation = new Animation(Easing.LINEAR, 90);

    public FlatMultiComponent(MultiProperty property) {
        super(property);
        this.height = 20;
    }

    public void render(int mouseX, int mouseY) {
        MaterialThemePicker.MaterialTheme theme = MaterialThemePicker.findClosestTheme(Ambient.getInstance().getHud().getCurrentTheme().color2);

        Color color = theme.getShade(300);
        int darker = theme.getShade(800).getRGB();
        int lighter = theme.getShade(100).getRGB();

        float extraHeight = property.getValues().length * 18 + 3;
        openAnimation.run(open ? extraHeight : 0);
        rotAnimation.run(open ? -90 : 90);

        height = (float) (20 + openAnimation.getValue());

        Fonts.getRobotoRegular(17).drawString(property.getLabel(), x + 7, y + 7f, lighter);

        String value = String.valueOf(property.getValue().size());
        float originalWidth = Fonts.getRobotoRegular(17).getWidth(value);
        float labelWidth = Fonts.getRobotoRegular(17).getWidth(property.getLabel());

        float availableWidth = width - 40 - labelWidth;
        if (originalWidth > availableWidth) {
            int maxChars = (int) (availableWidth / 6);
            if (maxChars > 3) {
                value = value.substring(0, maxChars) + "...";
            } else {
                value = "...";
            }
        }

        float valueWidth = Fonts.getRobotoRegular(17).getWidth(value);
        Fonts.getRobotoRegular(17).drawString(value, x + width - 20 - valueWidth, y + 7f, color.getRGB());

        GL11.glPushMatrix();

        float centerX = x + width - 18 + 5;
        float centerY = y + 5 + 5;

        GL11.glTranslatef(centerX, centerY, 0);
        GL11.glRotatef((float) rotAnimation.getValue(), 0, 0, 1);
        GL11.glTranslatef(-centerX, -centerY, 0);

        inGameImages.get("chevron").drawImg(x + width - 18, y + 5, 10, 10, color);

        GL11.glPopMatrix();

        if (open || openAnimation.getValue() != 0) {
            RenderUtil.renderScissor(() -> {
                float startY = y + 21;
                for (String valuee : property.getValues()) {
                    RenderUtil.drawRoundedRect(x + 7, startY, width - 14, 15, 3, property.isSelected(valuee) ? color : new Color(0x252525));
                    Fonts.getRobotoRegular(17).drawString(valuee, x + 11.5f, startY + 4.5f, property.isSelected(valuee) ? darker : 0xFF606060);
                    inGameImages.get("check").drawImg(x + width - 22f, startY + 2, 11, 11, property.isSelected(valuee) ? new Color(darker) : new Color(0x555555));
                    startY += 18;
                }
            }, x, y, x + width, y + height);
        }
    }

    public boolean click(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button != 2) {
            if (button == 0) {
                float startY = y + 21;
                for (String value : property.getValues()) {
                    if (mouseY >= startY && mouseY <= startY + 15) {
                        property.toggleValue(value);
                    }
                    startY += 18;
                }
            }
            if (mouseY <= y + 20) {
                open = !open;
            }
        }

        return isHovered();
    }
}
