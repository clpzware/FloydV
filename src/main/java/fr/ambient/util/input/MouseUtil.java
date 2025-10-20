package fr.ambient.util.input;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import fr.ambient.util.InstanceAccess;
import org.lwjglx.input.Mouse;

@ExcludeFlow
@ExcludeConstant
public class MouseUtil implements InstanceAccess {

    private static float getGuiScale() {
        return mc.gameSettings.guiScale == 0 ? 2 : mc.gameSettings.guiScale;
    }

    public static int getMouseX() {
        return (int) (Mouse.getX() / getGuiScale());
    }

    public static int getMouseY() {
        return (int) ((mc.displayHeight / getGuiScale()) - (Mouse.getY() / getGuiScale()));
    }

    public static boolean isHovering(int mouseX, int mouseY, float x, float y, float width, float height) {
        boolean isWithinX = mouseX >= x && mouseX <= x + width;
        boolean isWithinY = mouseY >= y && mouseY <= y + height;

        return isWithinX && isWithinY;
    }

}
