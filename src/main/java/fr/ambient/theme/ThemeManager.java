package fr.ambient.theme;

import fr.ambient.Ambient;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.ArrayList;

public class ThemeManager extends ArrayList<Theme> {
    @Setter
    @Getter
    private Theme currentTheme;

    public ThemeManager() {

        this.add(new Theme("Ambient", EnumChatFormatting.AQUA, new Color(143, 156, 201), new Color(207, 195, 252), false));
        this.add(new Theme("Aqua", EnumChatFormatting.AQUA, new Color(91, 134, 229), new Color(54, 209, 220), false));
        this.add(new Theme("Blue", EnumChatFormatting.BLUE, new Color(24, 40, 72), new Color(75, 108, 183), false));
        this.add(new Theme("Gray", EnumChatFormatting.DARK_GRAY, new Color(38, 50, 56), new Color(144, 164, 174), false));
        this.add(new Theme("Green", EnumChatFormatting.DARK_GREEN, new Color(9, 48, 40), new Color(35, 122, 87), false));
        this.add(new Theme("Orange", EnumChatFormatting.RED, new Color(252, 74, 26), new Color(247, 183, 51), false));
        this.add(new Theme("Purple", EnumChatFormatting.DARK_PURPLE, new Color(63, 25, 123), new Color(152, 77, 203), false));
        this.add(new Theme("Pink", EnumChatFormatting.LIGHT_PURPLE, new Color(187, 55, 125), new Color(251, 211, 233), false));
        this.add(new Theme("Red", EnumChatFormatting.DARK_RED, new Color(147, 41, 30), new Color(237, 33, 58), false));


        this.add(Ambient.getInstance().getCustomThemeManager().customTheme);


        currentTheme = this.get(0);
    }

    public Theme getThemeByName(String name) {
        for (Theme t : this) {
            if (name.equalsIgnoreCase(t.getName())) {
                return t;
            }
        }
        return null;
    }
}
