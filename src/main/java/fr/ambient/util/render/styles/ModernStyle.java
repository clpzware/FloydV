package fr.ambient.util.render.styles;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.Ambient;
import fr.ambient.theme.Theme;
import fr.ambient.util.InstanceAccess;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

@ExcludeFlow
@ExcludeConstant
public class ModernStyle implements InstanceAccess {

    public static void drawProgress(float progress) {
        ScaledResolution sr = new ScaledResolution(mc);
        drawProgress(sr.getScaledWidth() / 2f, sr.getScaledHeight() / 2f + 50, progress);
    }

    public static void drawProgress(float x, float y, float progress) {
        Theme currentTheme = Ambient.getInstance().getHud().getCurrentTheme();

        RenderUtil.drawRoundedRect(x - 70, y, 140, 8, 3.5f, new Color(0x65000000, true));
        RenderUtil.drawRoundedRect(x - 70, y, 140 * progress, 8, 3.5f, currentTheme.color2, currentTheme.color2, currentTheme.color1, currentTheme.color1);

        Fonts.getNunito(13).drawCenteredString(String.format("%.0f", progress * 100) + "%", (x - 70) + (140 * progress), y - 7, -1);
    }

    public static void drawBackground(float x, float y, float width, float height) {
        float radius = (height / 2 > 7 && width / 2 > 7) ? 7 : Math.max(Math.min(width / 2 - 1, height / 2 - 1), 1);
        RenderUtil.drawRoundedRect(x, y, width, height, 7, new Color(0x90121214, true));
    }
}
