package fr.ambient.ui.newaltmanager;

import fr.ambient.util.render.ColorUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;
import lombok.Getter;
import lombok.Setter;
import org.lwjglx.util.vector.Vector4f;

import java.awt.*;

@Getter
public class AltManagerButton {

    private final Animation animation = new Animation(Easing.EASE_IN_OUT_QUAD, 200);
    private final GuiAltManager parent;

    private final String name;
    private final Runnable runnable;
    private Runnable altClick;

    private final float radius = 5f;
    @Setter
    private Vector4f bounds;

    public AltManagerButton(GuiAltManager parent, String name, Vector4f bounds, Runnable runnable) {
        this.name = name;
        this.runnable = runnable;
        this.parent = parent;
        this.bounds = bounds;
        animation.setValue(0);
        animation.setStartPoint(0);
    }

    public AltManagerButton(GuiAltManager parent, String name, Vector4f bounds, Runnable runnable, Runnable altRunnable) {
        this.name = name;
        this.runnable = runnable;
        this.altClick = altRunnable;
        this.parent = parent;
        this.bounds = bounds;
        animation.setValue(0);
        animation.setStartPoint(0);
    }

    public void render(int mouseX, int mouseY) {
        animation.run(isHovered(mouseX, mouseY) ? 1 : 0);

        RenderUtil.drawRoundedRect(bounds.x, bounds.y, bounds.z, bounds.w, radius, new Color(0xA6121212, true));
        RenderUtil.drawRoundedOutline(bounds.x, bounds.y, bounds.z, bounds.w, radius, 1, ColorUtil.interpolateColorC(new Color(0xFF404047, true), new Color(0xFF484F9C, true), (float) animation.getValue()));

        Fonts.getNunito(26).drawCenteredString(name, bounds.x + bounds.z / 2, bounds.y + 7, ColorUtil.interpolateColorC(new Color(0xFF404047, true), new Color(0xFF484F9C, true), (float) animation.getValue()).getRGB());
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0)
                runnable.run();
            else if (button == 1 && altClick != null)
                altClick.run();
        }
    }

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX > bounds.x && mouseX < bounds.x + bounds.z && mouseY > bounds.y && mouseY <= bounds.y + bounds.w;
    }
}