package fr.ambient.ui.clickgui.flat.component;

import fr.ambient.Ambient;
import fr.ambient.module.Module;
import fr.ambient.property.Property;
import fr.ambient.property.impl.*;
import fr.ambient.ui.clickgui.flat.component.setting.FlatSettingComponent;
import fr.ambient.ui.clickgui.flat.component.setting.impl.*;
import fr.ambient.ui.framework.Component;
import fr.ambient.util.render.ColorUtil;
import fr.ambient.util.render.MaterialThemePicker;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FlatModuleComponent extends Component {

    private final Module module;
    private final List<FlatSettingComponent<?>> properties = new ArrayList<>();

    private boolean open;
    private final Animation openAnimation = new Animation(Easing.EASE_IN_OUT_SINE, 250);
    private final Animation hoverAnimation = new Animation(Easing.EASE_OUT_QUAD, 100);
    private final Animation colorAnimation = new Animation(Easing.LINEAR, 120);

    public FlatModuleComponent(final Module module) {
        this.module = module;
        if (module.isEnabled()) colorAnimation.setValue(1);

        for (Property<?> property : module.getPropertyList()) {
            if (property instanceof BooleanProperty booleanProperty)
                properties.add(new FlatBooleanComponent(booleanProperty));
            if (property instanceof NumberProperty numberProperty)
                properties.add(new FlatNumberComponent(numberProperty));
            if (property instanceof ModeProperty modeProperty)
                properties.add(new FlatModeComponent(modeProperty));
            if (property instanceof ColorProperty colorProperty)
                properties.add(new FlatColorComponent(colorProperty));
            if (property instanceof MultiProperty multiProperty)
                properties.add(new FlatMultiComponent(multiProperty));
            if (property instanceof CompositeProperty compositeProperty)
                properties.add(new FlatCompositeComponent(compositeProperty));
        }
    }

    public void render(final int mouseX, final int mouseY) {
        MaterialThemePicker.MaterialTheme theme = MaterialThemePicker.findClosestTheme(Ambient.getInstance().getHud().getCurrentTheme().color2);

        Color color = theme.getShade(300);
        Color darker = theme.getShade(800);

        // Remove it if you want i just need it for now
        hoverAnimation.run(isHovered(mouseX, mouseY) && mouseY <= y + 22 ? 0.5 : 0);
        openAnimation.run(open ? 1 : 0);
        colorAnimation.run(module.isEnabled() ? 1 : 0);

        float compHeights = 0;
        for (FlatSettingComponent<?> property : properties) {
            if (!property.getProperty().isVisible()) continue;
            compHeights += property.getHeight();
        }

        height = 22 + (compHeights * (float) openAnimation.getValue());

        RenderUtil.drawRect(x, y, width, height, new Color(0x1A1A1A));
        RenderUtil.drawRect(x, y, width, 22, ColorUtil.interpolateColorC(new Color(0x222222), color, (float) colorAnimation.getValue()));

        Fonts.getRobotoMedium(19).drawString(module.getName(), (float) (x + 7 + hoverAnimation.getValue() * 2), y + 7f, ColorUtil.interpolateColorC(Color.WHITE, darker, (float) colorAnimation.getValue()).getRGB());

        if (!module.getPropertyList().isEmpty())
            inGameImages.get("more").drawImg((float) (x + width - 21 +  hoverAnimation.getValue() * 2), y + 4, 14, 14, ColorUtil.interpolateColorC(Color.WHITE, darker, (float) colorAnimation.getValue()));

        RenderUtil.renderScissor(() -> {
            if (open || openAnimation.getValue() > 0) {
                float compY = y + 22;

                for (FlatSettingComponent<?> component : properties) {
                    if (!component.getProperty().isVisible())
                        continue;

                    component.setX(x);
                    component.setY(compY);

                    component.setWidth(width);

                    component.render(mouseX, mouseY);

                    compY += component.getHeight();
                }
            }
        }, x, y, x + width, y + height);
    }

    public boolean click(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (mouseY <= y + 22) {
                if (button == 0)
                    module.toggle();
                if (button == 1 && openAnimation.isFinished())
                    open = !open;
            } else {
                properties.forEach(c -> c.click(mouseX, mouseY, button));
            }
        }

        return hovered;
    }

    public void drag(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        properties.stream()
                .filter(component -> component.getProperty().isVisible())
                .forEach(component -> component.drag(mouseX, mouseY, button, timeSinceLastClick));
    }

    public void release(int mouseX, int mouseY, int state) {
        properties.stream()
                .filter(component -> component.getProperty().isVisible())
                .forEach(component -> component.release(mouseX, mouseY, state));
    }
}