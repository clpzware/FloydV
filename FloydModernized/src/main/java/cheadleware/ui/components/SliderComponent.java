package cheadleware.ui.components;

import cheadleware.Cheadleware;
import cheadleware.module.modules.Render.HUD;
import cheadleware.ui.Component;
import cheadleware.ui.dataset.Slider;
import cheadleware.util.font.FontManager;
import cheadleware.util.tenacityshaders.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

public class SliderComponent implements Component {
    private final Slider slider;
    private final ModuleComponent parentModule;
    private int offsetY;
    private int x;
    private int y;
    private boolean dragging = false;
    private double sliderWidth;
    private float hoverAnimation = 0F;
    private float dragAnimation = 0F;
    private long lastFrameTime = System.currentTimeMillis();

    public SliderComponent(Slider slider, ModuleComponent parentModule, int offsetY) {
        this.slider = slider;
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

        boolean hovered = isLeftHalfHovered(mouseX, mouseY) || isRightHalfHovered(mouseX, mouseY);
        float targetHover = hovered ? 1F : 0F;
        hoverAnimation += (targetHover - hoverAnimation) * Math.min(deltaTime * 12F, 1F);

        float targetDrag = dragging ? 1F : 0F;
        dragAnimation += (targetDrag - dragAnimation) * Math.min(deltaTime * 10F, 1F);

        // Hover background (squared)
        if (hoverAnimation > 0.01F) {
            int hoverAlpha = (int)(40 * hoverAnimation);
            net.minecraft.client.gui.Gui.drawRect(
                    this.parentModule.category.getX() + 2,
                    this.parentModule.category.getY() + this.offsetY,
                    this.parentModule.category.getX() + this.parentModule.category.getWidth() - 2,
                    this.parentModule.category.getY() + this.offsetY + 18,
                    new Color(60, 60, 70, hoverAlpha).getRGB());
        }

        // Label with value using Sans font
        String label = this.slider.getName() + ": ";
        String value = this.slider.getValueString();

        Color labelColor = new Color(170, 170, 170);
        FontManager.SANS.drawString(label, this.parentModule.category.getX() + 5,
                this.parentModule.category.getY() + this.offsetY + 3, labelColor.getRGB());

        // Value color
        Color valueColor = new Color(200, 200, 200);
        float valueX = this.parentModule.category.getX() + 5 + FontManager.SANS.getWidth(label);
        FontManager.SANS.drawString(value, valueX,
                this.parentModule.category.getY() + this.offsetY + 3, valueColor.getRGB());

        // Slider bar
        int sliderY = this.parentModule.category.getY() + this.offsetY + 12;
        int sliderLeft = this.parentModule.category.getX() + 5;
        int sliderRight = this.parentModule.category.getX() + this.parentModule.category.getWidth() - 5;

        // Background track (squared)
        net.minecraft.client.gui.Gui.drawRect(sliderLeft, sliderY, sliderRight, sliderY + 3,
                new Color(20, 20, 25, 200).getRGB());

        // Calculate filled portion
        int sliderStart = sliderLeft;
        int sliderEnd = sliderLeft + (int) this.sliderWidth;
        if (sliderEnd - sliderStart > this.parentModule.category.getWidth() - 10) {
            sliderEnd = sliderStart + this.parentModule.category.getWidth() - 10;
        }

        // Filled bar (squared)
        if (sliderEnd > sliderStart) {
            Color accentColor = ((HUD) Cheadleware.moduleManager.modules.get(HUD.class))
                    .getColor(System.currentTimeMillis(), offset.get());

            int brightBoost = (int)(30 * Math.max(hoverAnimation, dragAnimation));
            Color brightColor = new Color(
                    Math.min(255, accentColor.getRed() + brightBoost),
                    Math.min(255, accentColor.getGreen() + brightBoost),
                    Math.min(255, accentColor.getBlue() + brightBoost),
                    230
            );

            net.minecraft.client.gui.Gui.drawRect(sliderStart, sliderY, sliderEnd, sliderY + 3,
                    brightColor.getRGB());
        }

        // Slider thumb (squared)
        if (sliderEnd > sliderStart) {
            int thumbSize = 3 + (int)(2 * dragAnimation);
            int thumbX = sliderEnd - thumbSize / 2;
            int thumbY = sliderY - 1;

            net.minecraft.client.gui.Gui.drawRect(thumbX, thumbY, thumbX + thumbSize, thumbY + 5,
                    new Color(255, 255, 255, 240).getRGB());
        }
    }

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();

        double d = Math.min(this.parentModule.category.getWidth() - 10, Math.max(0, mousePosX - this.x - 5));
        this.sliderWidth = (double)(this.parentModule.category.getWidth() - 10) *
                (this.slider.getInput() - this.slider.getMin()) /
                (this.slider.getMax() - this.slider.getMin());

        if (this.dragging) {
            if (d == 0.0D) {
                this.slider.setValue(this.slider.getMin());
            } else {
                double rawValue = d / (double)(this.parentModule.category.getWidth() - 10)
                        * (this.slider.getMax() - this.slider.getMin())
                        + this.slider.getMin();

                double increment = this.slider.getIncrement();
                if (increment > 0) {
                    rawValue = Math.round(rawValue / increment) * increment;
                }
                double n = roundToPrecision(rawValue, 2);
                n = Math.max(this.slider.getMin(), Math.min(this.slider.getMax(), n));
                this.slider.setValue(n);
            }
        }
    }

    private static double roundToPrecision(double v, int precision) {
        if (precision < 0) {
            return 0.0D;
        } else {
            BigDecimal bd = new BigDecimal(v);
            bd = bd.setScale(precision, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isLeftHalfHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            this.dragging = true;
        }

        if (this.isRightHalfHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            this.dragging = true;
        }
    }

    public void mouseReleased(int x, int y, int button) {
        this.dragging = false;
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {

    }

    public boolean isLeftHalfHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth() / 2 + 1 &&
                y > this.y && y < this.y + 18;
    }

    public boolean isRightHalfHovered(int x, int y) {
        return x > this.x + this.parentModule.category.getWidth() / 2 &&
                x < this.x + this.parentModule.category.getWidth() && y > this.y && y < this.y + 18;
    }

    @Override
    public boolean isVisible() {
        return slider.isVisible();
    }
}