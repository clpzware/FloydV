package fr.ambient.ui.clickgui.flat.component;

import fr.ambient.Ambient;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.ui.framework.Component;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import fr.ambient.util.render.opengl.StencilUtil;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjglx.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FlatCategoryComponent extends Component {

    private final ModuleCategory category;

    private final List<FlatModuleComponent> modules = new ArrayList<>();

    private float scrollOffset = 0;
    private float scrollVelocity = 0;
    private static final float SCROLL_DAMPING = 0.95f;
    private static final float MIN_SCROLL_VELOCITY = 0.1f;

    public FlatCategoryComponent(ModuleCategory category, final float x, final float y, final float width, final float height) {
        this.category = category;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        for (Module module : Ambient.getInstance().getModuleManager().getMultipleBy(module -> module.getCategory() == category))
            modules.add(new FlatModuleComponent(module));
    }

    public void render(int mouseX, int mouseY) {
        float totalModuleHeight = 0;
        for (FlatModuleComponent component : modules) {
            totalModuleHeight += component.getHeight();
        }

        height = Math.min(24f + totalModuleHeight, 24f + 22 * 16);

        scrollOffset += scrollVelocity;

        scrollVelocity *= SCROLL_DAMPING;

        if (Math.abs(scrollVelocity) < MIN_SCROLL_VELOCITY) {
            scrollVelocity = 0;
        }

        float maxScrollOffset = -(totalModuleHeight - (height - 24));

        scrollOffset = Math.max(maxScrollOffset, Math.min(scrollOffset, 0));

        if (scrollOffset == 0 || scrollOffset == -maxScrollOffset)
            scrollVelocity = 0;

        StencilUtil.renderStencil(() -> {
            RenderUtil.drawRoundedRectGl(x, y + 24f, width, height - 24f, new float[]{0, 0f, 7.5f, 7.5f}, Color.WHITE);
        }, () -> {
            float yOffset = 24 + scrollOffset;
            for (FlatModuleComponent component : modules) {
                if (component.getHeight() == 0) {
                    component.setBounds(x, y + yOffset, width, 22);
                } else {
                    component.setY(y + yOffset);
                }
                component.render(mouseX, mouseY);
                yOffset += component.getHeight();
            }
        });

        RenderUtil.drawVariableRoundedRect(x, y, width, 24, new float[] {7.5f, 7.5f, 0, 0}, new Color(0x1A1A1A));
        Fonts.getRobotoBold(20).drawString(category.getName(), x + 23, y + 8, -1);
        RenderUtil.drawLine(x, y + 24, width, 0, 2, new Color(0xFF323232));

        inGameImages.get(category.getName().toLowerCase()).drawImg(x + 7, y + 6, 12, 12);
    }

    public boolean click(int mouseX, int mouseY, int button) {
        if (mouseY > y + 24 && mouseY < y + height)
            modules.forEach(component -> component.click(mouseX, mouseY, button));
        return hovered;
    }

    public void release(int mouseX, int mouseY, int state) {
        modules.forEach(component -> component.release(mouseX, mouseY, state));
    }

    public void drag(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        modules.forEach(component -> component.drag(mouseX, mouseY, button, timeSinceLastClick));
    }

    public void scroll() {
        ScaledResolution sr = new ScaledResolution(mc);

        int mouseX = Mouse.getEventX() * sr.getScaledWidth() / mc.displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getEventY() * sr.getScaledHeight() / mc.displayHeight - 1;

        if (isHovered(mouseX, mouseY)) {
            int i = Integer.signum(Mouse.getEventDWheel());
            scrollVelocity += i * 2;
        }
    }
}
