package fr.ambient.theme;

import fr.ambient.util.render.ColorUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;

@Getter
@AllArgsConstructor
public class Theme {
    public String name;
    public EnumChatFormatting chatFormatting;
    public Color color1;
    public Color color2;
    public boolean isRainbow;

    public Color getColor(int time, int offset){
        return ColorUtil.getColorFromIndex(time, offset, color1, color2, false);
    }

    public Color getColor1() {
        if (isRainbow)
            return getColor(3, 0);

        return color1;
    }

    public Color getColor2() {
        if (isRainbow)
            return getColor(3, 250);

        return color2;
    }
}
