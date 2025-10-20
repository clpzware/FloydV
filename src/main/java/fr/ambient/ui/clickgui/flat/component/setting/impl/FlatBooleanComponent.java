package fr.ambient.ui.clickgui.flat.component.setting.impl;

import fr.ambient.Ambient;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.ui.clickgui.flat.component.setting.FlatSettingComponent;
import fr.ambient.util.render.ColorUtil;
import fr.ambient.util.render.MaterialThemePicker;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;

import java.awt.*;

public class FlatBooleanComponent extends FlatSettingComponent<BooleanProperty> {

    private final Animation animation = new Animation(Easing.LINEAR, 120);

    public FlatBooleanComponent(BooleanProperty property) {
        super(property);
        this.height = 20;
        if (property.getValue())
            animation.setValue(1);
    }

    public void render(int mouseX, int mouseY) {
        MaterialThemePicker.MaterialTheme theme = MaterialThemePicker.findClosestTheme(Ambient.getInstance().getHud().getCurrentTheme().color2);

        Color color = theme.getShade(300);
        Color darker = theme.getShade(800);
        int lighter = theme.getShade(100).getRGB();

        animation.run(property.getValue() ? 1 : 0);

        Fonts.getRobotoRegular(17).drawString(property.getLabel(), x + 7, y + 7f, lighter);

        RenderUtil.drawRoundedRect(x + width - 8 - (height - 10), y + 5, height - 10, height - 10, 2, ColorUtil.interpolateColorC(new Color(0x252525), color, (float) animation.getValue()));
        inGameImages.get("check").drawImg(x + width - 9 - (height - 12), y + 6, height - 12, height - 12, ColorUtil.interpolateColorC(new Color(0xFF606060), darker, (float) animation.getValue()));
    }

    public boolean click(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0)
            property.setValue(!property.getValue());

        return hovered;
    }
}