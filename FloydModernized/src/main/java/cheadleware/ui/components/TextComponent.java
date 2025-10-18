package cheadleware.ui.components;

import cheadleware.Cheadleware;
import cheadleware.module.modules.Render.HUD;
import cheadleware.property.properties.TextProperty;
import cheadleware.ui.ClickGui;
import cheadleware.ui.Component;
import cheadleware.ui.callback.GuiInput;
import cheadleware.util.font.FontManager;
import cheadleware.util.tenacityshaders.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TextComponent implements Component {
    private final TextProperty property;
    private final ModuleComponent module;
    private int offsetY;
    private int x;
    private int y;
    private float hoverAnimation = 0F;
    private long lastFrameTime = System.currentTimeMillis();

    public TextComponent(TextProperty property, ModuleComponent parentModule, int offsetY) {
        this.property = property;
        this.module = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        // Update animations
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000F;
        lastFrameTime = currentTime;

        int mouseX = 0, mouseY = 0;
        try {
            mouseX = org.lwjgl.input.Mouse.getX() * module.category.getWidth() / Minecraft.getMinecraft().displayWidth;
            mouseY = module.category.getWidth() - org.lwjgl.input.Mouse.getY() * module.category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
        } catch (Exception e) {}

        boolean hovered = isHovered(mouseX, mouseY);
        float targetHover = hovered ? 1F : 0F;
        hoverAnimation += (targetHover - hoverAnimation) * Math.min(deltaTime * 12F, 1F);

        // Hover background (squared)
        if (hoverAnimation > 0.01F) {
            int hoverAlpha = (int)(40 * hoverAnimation);
            net.minecraft.client.gui.Gui.drawRect(
                    this.module.category.getX() + 2,
                    this.module.category.getY() + this.offsetY,
                    this.module.category.getX() + this.module.category.getWidth() - 2,
                    this.module.category.getY() + this.offsetY + 14,
                    new Color(60, 60, 70, hoverAlpha).getRGB());
        }

        // Edit icon on the right
        int iconX = this.module.category.getX() + this.module.category.getWidth() - 12;
        int iconY = this.module.category.getY() + this.offsetY + 3;
        int iconSize = 8;

        Color accentColor = ((HUD) Cheadleware.moduleManager.modules.get(HUD.class))
                .getColor(System.currentTimeMillis(), offset.get());

        // Icon background (squared)
        int iconAlpha = 150 + (int)(50 * hoverAnimation);
        net.minecraft.client.gui.Gui.drawRect(iconX, iconY, iconX + iconSize, iconY + iconSize,
                new Color(40, 40, 50, iconAlpha).getRGB());

        // Draw pencil icon
        if (hoverAnimation > 0.01F) {
            int pencilAlpha = (int)(200 * hoverAnimation);

            // Simple pencil representation
            Gui.drawRect(iconX + 3, iconY + 2, iconX + 4, iconY + 3,
                    new Color(accentColor.getRed(), accentColor.getGreen(),
                            accentColor.getBlue(), pencilAlpha).getRGB());
            Gui.drawRect(iconX + 2, iconY + 3, iconX + 3, iconY + 4,
                    new Color(180, 180, 180, pencilAlpha).getRGB());
            Gui.drawRect(iconX + 1, iconY + 4, iconX + 2, iconY + 5,
                    new Color(160, 160, 160, pencilAlpha).getRGB());
        }

        String displayName = this.property.getName().replace("-", " ");
        String value = this.property.formatValue();

        // Draw label
        String label = displayName + ": ";
        FontManager.SANS.drawString(
                label,
                this.module.category.getX() + 5,
                this.module.category.getY() + this.offsetY + 4,
                new Color(170, 170, 170).getRGB()
        );

        // Draw value
        int valueR = accentColor.getRed();
        int valueG = accentColor.getGreen();
        int valueB = accentColor.getBlue();

        if (hoverAnimation > 0.01F) {
            valueR = Math.min(255, (int)(valueR + 40 * hoverAnimation));
            valueG = Math.min(255, (int)(valueG + 40 * hoverAnimation));
            valueB = Math.min(255, (int)(valueB + 40 * hoverAnimation));
        }

        // Truncate value if too long
        String displayValue = value;
        float maxWidth = this.module.category.getWidth() - 30;
        if (FontManager.SANS.getWidth(displayValue) > maxWidth) {
            while (FontManager.SANS.getWidth(displayValue + "...") > maxWidth && displayValue.length() > 0) {
                displayValue = displayValue.substring(0, displayValue.length() - 1);
            }
            displayValue += "...";
        }

        float valueX = this.module.category.getX() + 5 + FontManager.SANS.getWidth(label);
        FontManager.SANS.drawString(
                displayValue,
                valueX,
                this.module.category.getY() + this.offsetY + 4,
                new Color(valueR, valueG, valueB).getRGB()
        );
    }

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.module.category.getY() + this.offsetY;
        this.x = this.module.category.getX();
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0 && this.module.panelExpand) {
            GuiInput.prompt(property.getName().replace("-", " "), property.getValue(), property::setValue, ClickGui.getInstance());
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {

    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {

    }

    public boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.module.category.getWidth() &&
                y > this.y && y < this.y + 14;
    }

    @Override
    public boolean isVisible() {
        return property.isVisible();
    }
}