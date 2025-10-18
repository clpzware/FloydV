package cheadleware.ui.components;

import cheadleware.Cheadleware;
import cheadleware.module.modules.Render.HUD;
import cheadleware.property.properties.BooleanProperty;
import cheadleware.ui.Component;
import cheadleware.util.font.FontManager;
import cheadleware.util.tenacityshaders.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckBoxComponent implements Component {
    private final BooleanProperty property;
    private final ModuleComponent module;
    private int offsetY;
    private int x;
    private int y;
    private float hoverAnimation = 0F;
    private float checkAnimation = 0F;
    private long lastFrameTime = System.currentTimeMillis();

    public CheckBoxComponent(BooleanProperty property, ModuleComponent parentModule, int offsetY) {
        this.property = property;
        this.module = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
        this.checkAnimation = property.getValue() ? 1F : 0F;
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

        float targetCheck = property.getValue() ? 1F : 0F;
        checkAnimation += (targetCheck - checkAnimation) * Math.min(deltaTime * 10F, 1F);

        int boxSize = 10;
        int boxX = this.module.category.getX() + this.module.category.getWidth() - boxSize - 5;
        int boxY = this.module.category.getY() + this.offsetY + 2;

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

        // Checkbox background (squared)
        net.minecraft.client.gui.Gui.drawRect(boxX, boxY, boxX + boxSize, boxY + boxSize,
                new Color(20, 20, 25, 220).getRGB());

        // Checkbox border
        Color borderColor = checkAnimation > 0.01F
                ? new Color(80, 150, 255)
                : new Color(60, 60, 70);

        drawHollowRect(boxX, boxY, boxX + boxSize, boxY + boxSize, borderColor.getRGB());

        // Filled checkbox when checked (squared)
        if (checkAnimation > 0.01F) {
            int fillSize = (int)((boxSize - 2) * checkAnimation);
            int fillOffset = (boxSize - fillSize) / 2;

            net.minecraft.client.gui.Gui.drawRect(
                    boxX + fillOffset,
                    boxY + fillOffset,
                    boxX + fillOffset + fillSize,
                    boxY + fillOffset + fillSize,
                    new Color(80, 150, 255, (int)(200 * checkAnimation)).getRGB());
        }

        // Checkmark
        if (checkAnimation > 0.4F) {
            float checkAlpha = Math.min(1F, (checkAnimation - 0.4F) / 0.6F);

            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GL11.glLineWidth(1.5F);
            GL11.glBegin(GL11.GL_LINE_STRIP);
            GL11.glColor4f(1F, 1F, 1F, checkAlpha);

            float progress = Math.min(1F, (checkAnimation - 0.4F) / 0.6F);
            GL11.glVertex2f(boxX + 2, boxY + 5);

            if (progress > 0.3F) {
                GL11.glVertex2f(boxX + 4, boxY + 7);
            }

            if (progress > 0.6F) {
                GL11.glVertex2f(boxX + 8, boxY + 3);
            }

            GL11.glEnd();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }

        // Label text with Sans font
        String displayName = this.property.getName().replace("-", " ");

        Color textColor;
        if (checkAnimation > 0.01F) {
            textColor = new Color(220, 220, 220);
        } else {
            int gray = 170 + (int)(30 * hoverAnimation);
            textColor = new Color(gray, gray, gray);
        }

        FontManager.SANS.drawString(
                displayName,
                this.module.category.getX() + 5,
                this.module.category.getY() + this.offsetY + 4,
                textColor.getRGB()
        );
    }

    private void drawHollowRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, top + 1, color);
        Gui.drawRect(left, bottom - 1, right, bottom, color);
        Gui.drawRect(left, top, left + 1, bottom, color);
        Gui.drawRect(right - 1, top, right, bottom, color);
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
            this.property.setValue(!this.property.getValue());
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