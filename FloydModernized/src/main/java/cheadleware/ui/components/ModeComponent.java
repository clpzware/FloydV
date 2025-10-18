package cheadleware.ui.components;

import cheadleware.Cheadleware;
import cheadleware.module.modules.Render.HUD;
import cheadleware.property.properties.ModeProperty;
import cheadleware.ui.Component;
import cheadleware.util.font.FontManager;
import cheadleware.util.tenacityshaders.RenderUtil;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ModeComponent implements Component {
    private final ModeProperty property;
    private final ModuleComponent parentModule;
    private int x;
    private int y;
    private int offsetY;
    private float hoverAnimation = 0F;
    private float changeAnimation = 0F;
    private long lastFrameTime = System.currentTimeMillis();

    public ModeComponent(ModeProperty desc, ModuleComponent parentModule, int offsetY) {
        this.property = desc;
        this.parentModule = parentModule;
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
            mouseX = org.lwjgl.input.Mouse.getX() * parentModule.category.getWidth() / Minecraft.getMinecraft().displayWidth;
            mouseY = parentModule.category.getWidth() - org.lwjgl.input.Mouse.getY() * parentModule.category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
        } catch (Exception e) {}

        boolean hovered = isHovered(mouseX, mouseY);
        float targetHover = hovered ? 1F : 0F;
        hoverAnimation += (targetHover - hoverAnimation) * Math.min(deltaTime * 12F, 1F);

        if (changeAnimation > 0F) {
            changeAnimation = Math.max(0F, changeAnimation - deltaTime * 4F);
        }

        // Hover background (squared)
        if (hoverAnimation > 0.01F) {
            int hoverAlpha = (int)(40 * hoverAnimation);
            net.minecraft.client.gui.Gui.drawRect(
                    this.parentModule.category.getX() + 2,
                    this.parentModule.category.getY() + this.offsetY,
                    this.parentModule.category.getX() + this.parentModule.category.getWidth() - 2,
                    this.parentModule.category.getY() + this.offsetY + 14,
                    new Color(60, 60, 70, hoverAlpha).getRGB());
        }

        // Change flash effect (squared)
        if (changeAnimation > 0.01F) {
            Color accentColor = ((HUD) Cheadleware.moduleManager.modules.get(HUD.class))
                    .getColor(System.currentTimeMillis(), offset.get());

            int flashAlpha = (int)(50 * changeAnimation);
            net.minecraft.client.gui.Gui.drawRect(
                    this.parentModule.category.getX() + 2,
                    this.parentModule.category.getY() + this.offsetY,
                    this.parentModule.category.getX() + this.parentModule.category.getWidth() - 2,
                    this.parentModule.category.getY() + this.offsetY + 14,
                    new Color(accentColor.getRed(), accentColor.getGreen(),
                            accentColor.getBlue(), flashAlpha).getRGB());
        }

        String mode = this.property.getModeString();
        mode = mode.replace("_", " ");
        mode = mode.substring(0, 1).toUpperCase() + mode.substring(1).toLowerCase();

        // Draw label
        String label = this.property.getName() + ": ";
        Color labelColor = new Color(170, 170, 170);
        FontManager.SANS.drawString(
                label,
                this.parentModule.category.getX() + 5,
                this.parentModule.category.getY() + this.offsetY + 4,
                labelColor.getRGB()
        );

        // Draw mode value
        Color accentColor = ((HUD) Cheadleware.moduleManager.modules.get(HUD.class))
                .getColor(System.currentTimeMillis(), offset.get());

        int modeR = accentColor.getRed();
        int modeG = accentColor.getGreen();
        int modeB = accentColor.getBlue();

        if (hoverAnimation > 0.01F) {
            modeR = Math.min(255, (int)(modeR + 50 * hoverAnimation));
            modeG = Math.min(255, (int)(modeG + 50 * hoverAnimation));
            modeB = Math.min(255, (int)(modeB + 50 * hoverAnimation));
        }

        if (changeAnimation > 0.01F) {
            modeR = Math.min(255, (int)(modeR + 80 * changeAnimation));
            modeG = Math.min(255, (int)(modeG + 80 * changeAnimation));
            modeB = Math.min(255, (int)(modeB + 80 * changeAnimation));
        }

        float modeX = this.parentModule.category.getX() + 5 + FontManager.SANS.getWidth(label);
        FontManager.SANS.drawString(
                mode,
                modeX,
                this.parentModule.category.getY() + this.offsetY + 4,
                new Color(modeR, modeG, modeB).getRGB()
        );

        // Draw arrow indicators on hover
        if (hoverAnimation > 0.3F) {
            int arrowAlpha = (int)(180 * hoverAnimation);

            // Left arrow
            FontManager.SANS.drawString(
                    "<",
                    this.parentModule.category.getX() + this.parentModule.category.getWidth() - 20,
                    this.parentModule.category.getY() + this.offsetY + 4,
                    new Color(150, 150, 150, arrowAlpha).getRGB()
            );

            // Right arrow
            FontManager.SANS.drawString(
                    ">",
                    this.parentModule.category.getX() + this.parentModule.category.getWidth() - 10,
                    this.parentModule.category.getY() + this.offsetY + 4,
                    new Color(150, 150, 150, arrowAlpha).getRGB()
            );
        }
    }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();
    }

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public void mouseDown(int x, int y, int button) {
        if (isHovered(x, y)) {
            if (button == 0) {
                this.property.nextMode();
                changeAnimation = 1F;
            } else if (button == 1) {
                this.property.previousMode();
                changeAnimation = 1F;
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {

    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {

    }

    private boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth() &&
                y > this.y && y < this.y + 14;
    }

    @Override
    public boolean isVisible() {
        return property.isVisible();
    }
}