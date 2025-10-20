package fr.ambient.ui.clickgui.flat;

import fr.ambient.module.ModuleCategory;
import fr.ambient.ui.clickgui.flat.component.FlatCategoryComponent;
import fr.ambient.ui.framework.Component;
import net.minecraft.client.gui.GuiScreen;
import org.lwjglx.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlatUIScreen extends GuiScreen {

    private List<FlatCategoryComponent> categories = new ArrayList<>();

    public FlatUIScreen() {
        makeCategories();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        categories.forEach(component -> component.render(mouseX, mouseY));
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (FlatCategoryComponent category : categories) {
            if (category.click(mouseX, mouseY, mouseButton))
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        categories.forEach(component -> component.release(0, 0, 0));
        super.onGuiClosed();
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        categories.forEach(component -> component.release(mouseX, mouseY, state));
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        categories.forEach(component -> component.drag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        categories.forEach(Component::scroll);
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_F5)
            makeCategories();

        categories.forEach(component -> component.type(typedChar, keyCode));

        if (keyCode == Keyboard.KEY_ESCAPE)
            mc.displayGuiScreen(null);
    }

    public void makeCategories() {
        if (!categories.isEmpty())
            categories.clear();

        float x = 10, y = 10;
        final float width = 130, height = 32;

        for (ModuleCategory category : ModuleCategory.values()) {
            categories.add(new FlatCategoryComponent(category, x, y, width, height));
            x += width + 15;
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}
