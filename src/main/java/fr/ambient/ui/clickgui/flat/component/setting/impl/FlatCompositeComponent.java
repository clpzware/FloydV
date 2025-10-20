package fr.ambient.ui.clickgui.flat.component.setting.impl;

import fr.ambient.Ambient;
import fr.ambient.property.Property;
import fr.ambient.property.impl.*;
import fr.ambient.ui.clickgui.flat.component.setting.FlatSettingComponent;
import fr.ambient.util.render.MaterialThemePicker;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FlatCompositeComponent extends FlatSettingComponent<CompositeProperty> {

    private boolean open = false;
    private final List<FlatSettingComponent<?>> properties = new ArrayList<>();

    private final Animation animation = new Animation(Easing.EASE_IN_OUT_SINE, 150);
    private Animation rotAnimation = new Animation(Easing.LINEAR, 90);

    public FlatCompositeComponent(CompositeProperty property) {
        super(property);
        this.height = 20;

        for (Property<?> childProperty : property.getChildren()) {
            if (childProperty instanceof BooleanProperty booleanProperty)
                properties.add(new FlatBooleanComponent(booleanProperty));
            if (childProperty instanceof NumberProperty numberProperty)
                properties.add(new FlatNumberComponent(numberProperty));
            if (childProperty instanceof ModeProperty modeProperty)
                properties.add(new FlatModeComponent(modeProperty));
            if (childProperty instanceof ColorProperty colorProperty)
                properties.add(new FlatColorComponent(colorProperty));
            if (childProperty instanceof MultiProperty multiProperty)
                properties.add(new FlatMultiComponent(multiProperty));
            if (childProperty instanceof CompositeProperty compositeProperty)
                properties.add(new FlatCompositeComponent(compositeProperty));
        }
    }

    public void render(int mouseX, int mouseY) {
        animation.run(open ? 1 : 0);
        rotAnimation.run(open ? 90 : 0);

        float compHeights = 0;
        for (FlatSettingComponent<?> property : properties) {
            if (!property.getProperty().isVisible()) continue;
            compHeights += property.getHeight();
        }

        height = 20 + (compHeights * (float) animation.getValue());
        RenderUtil.drawRect(x, y, width, height, new Color(0x1A1A1A));

        MaterialThemePicker.MaterialTheme theme = MaterialThemePicker.findClosestTheme(Ambient.getInstance().getHud().getCurrentTheme().color2);

        Color color = theme.getShade(300);
        Color lighter = theme.getShade(100);

        Fonts.getRobotoRegular(17).drawString(property.getLabel(), x + 7f, y + 7f, lighter.getRGB());

        float centerX = x + width - 13f;
        float centerY = y + 10;
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY, 0);
        GL11.glRotatef((float) rotAnimation.getValue(), 0, 0, 1);
        GL11.glTranslatef(-centerX, -centerY, 0);

        inGameImages.get("play").drawImg(x + width - 17f, y + 6, 8, 8, color);

        GL11.glPopMatrix();

        RenderUtil.renderScissor(() -> {
            if (open || animation.getValue() > 0) {
                float compY = y + 20;

                for (FlatSettingComponent<?> component : properties) {
                    if (!component.getProperty().isVisible())
                        continue;

                    component.setX(x + 2);
                    component.setY(compY);
                    component.setWidth(width - 4);

                    component.render(mouseX, mouseY);

                    compY += component.getHeight();
                }
            }
        }, x, y, x + width, y + height);
    }

    public boolean click(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button != 2) {
            if (mouseY <= y + 20) open = !open;
            else {
                properties.stream()
                    .filter(component -> component.getProperty().isVisible())
                    .forEach(component -> component.click(mouseX, mouseY, button));
                return false;
            }
        }

        return isHovered();
    }

    @Override
    public void release(int mouseX, int mouseY, int state) {
        properties.stream()
                .filter(component -> component.getProperty().isVisible())
                .forEach(component -> component.release(mouseX, mouseY, state));
    }
}
