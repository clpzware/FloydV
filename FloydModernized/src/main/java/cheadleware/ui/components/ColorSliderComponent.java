package cheadleware.ui.components;

import cheadleware.property.properties.ColorProperty;
import cheadleware.ui.Component;
import cheadleware.util.font.FontManager;
import cheadleware.util.tenacityshaders.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ColorSliderComponent implements Component {

    private final ModuleComponent parentModule;
    private final ColorProperty property;
    private int offsetY;
    private boolean draggingHue, draggingSat, draggingBri;
    private float hue, saturation, brightness;
    private float hoverAnimation = 0F;
    private float dragAnimation = 0F;
    private long lastFrameTime = System.currentTimeMillis();

    public ColorSliderComponent(ColorProperty property, ModuleComponent parentModule, int offsetY) {
        this.parentModule = parentModule;
        this.offsetY = offsetY;
        this.property = property;

        Color c = new Color(property.getValue());
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
    }

    @Override
    public void draw(java.util.concurrent.atomic.AtomicInteger offset) {
        // Update animations
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000F;
        lastFrameTime = currentTime;

        int mouseX = 0, mouseY = 0;
        try {
            mouseX = org.lwjgl.input.Mouse.getX() * parentModule.category.getWidth() / Minecraft.getMinecraft().displayWidth;
            mouseY = parentModule.category.getWidth() - org.lwjgl.input.Mouse.getY() * parentModule.category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
        } catch (Exception e) {}

        int baseY = parentModule.category.getY() + offsetY + 10;
        int satY = baseY + 6;
        int briY = satY + 6;

        boolean hovered = isHovered(mouseX, mouseY, baseY) ||
                isHovered(mouseX, mouseY, satY) ||
                isHovered(mouseX, mouseY, briY);

        float targetHover = hovered ? 1F : 0F;
        hoverAnimation += (targetHover - hoverAnimation) * Math.min(deltaTime * 12F, 1F);

        boolean isDragging = draggingHue || draggingSat || draggingBri;
        float targetDrag = isDragging ? 1F : 0F;
        dragAnimation += (targetDrag - dragAnimation) * Math.min(deltaTime * 10F, 1F);

        int x = parentModule.category.getX() + 5;
        int y = parentModule.category.getY() + offsetY;
        int width = parentModule.category.getWidth() - 10;

        // Label with Sans font
        String label = property.getName().replace("-", " ") + ": ";
        FontManager.SANS.drawString(
                label,
                x,
                y + 3,
                new Color(170, 170, 170).getRGB()
        );

        if (!draggingHue && !draggingSat && !draggingBri) {
            Color color = new Color(property.getValue());
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            hue = hsb[0];
            saturation = hsb[1];
            brightness = hsb[2];
        }

        // Color preview (squared)
        int colorPreviewSize = 8;
        int colorPreviewX = x + width - colorPreviewSize;
        int colorPreviewY = y + 2;
        int previewColor = Color.HSBtoRGB(hue, saturation, brightness);

        net.minecraft.client.gui.Gui.drawRect(colorPreviewX, colorPreviewY,
                colorPreviewX + colorPreviewSize, colorPreviewY + colorPreviewSize,
                previewColor);

        // Draw sliders
        drawHueBar(x, baseY, width);
        drawPointer(x, baseY, width, hue, draggingHue);

        drawGradientBar(x, satY, width, Color.WHITE.getRGB(),
                Color.getHSBColor(hue, 1f, 1f).getRGB());
        drawPointer(x, satY, width, saturation, draggingSat);

        drawGradientBar(x, briY, width, Color.BLACK.getRGB(),
                Color.getHSBColor(hue, saturation, 1f).getRGB());
        drawPointer(x, briY, width, brightness, draggingBri);
    }

    private void drawHueBar(int x, int y, int width) {
        for (int i = 0; i < width; i++) {
            float hue = (float) i / (float) width;
            int color = Color.HSBtoRGB(hue, 1f, 1f);
            Gui.drawRect(x + i, y, x + i + 1, y + 4, color);
        }
    }

    private void drawGradientBar(int x, int y, int width, int startColor, int endColor) {
        RenderUtil.drawGradientRect(x, y, x + width, y + 4, startColor, endColor);
    }

    private void drawPointer(int x, int y, int width, float value, boolean isDragging) {
        int posX = x + (int) (width * value);

        // Pointer (squared)
        net.minecraft.client.gui.Gui.drawRect(posX - 1, y, posX + 1, y + 4,
                new Color(255, 255, 255, 240).getRGB());

        // Scale effect when dragging
        if (isDragging) {
            net.minecraft.client.gui.Gui.drawRect(posX - 2, y - 1, posX + 2, y + 5,
                    new Color(255, 255, 255, 100).getRGB());
        }
    }

    @Override
    public void update(int mouseX, int mouseY) {
        int baseX = parentModule.category.getX() + 5;
        int width = parentModule.category.getWidth() - 10;
        boolean changed = false;

        if (draggingHue) {
            hue = getSliderValue(mouseX, baseX, width);
            changed = true;
        }
        if (draggingSat) {
            saturation = getSliderValue(mouseX, baseX, width);
            changed = true;
        }
        if (draggingBri) {
            brightness = getSliderValue(mouseX, baseX, width);
            changed = true;
        }

        if (changed) {
            int signed = Color.HSBtoRGB(hue, saturation, brightness);
            property.setValue(new Color(signed).getRGB());
        }
    }

    private float getSliderValue(int mouseX, int startX, int width) {
        double d = Math.min(width, Math.max(0, mouseX - startX));
        return (float) roundToPrecision(d / width, 3);
    }

    private static double roundToPrecision(double v, int precision) {
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (button != 0 || !parentModule.panelExpand) return;
        int baseY = parentModule.category.getY() + offsetY + 10;
        if (isHovered(mouseX, mouseY, baseY)) draggingHue = true;
        else if (isHovered(mouseX, mouseY, baseY + 6)) draggingSat = true;
        else if (isHovered(mouseX, mouseY, baseY + 12)) draggingBri = true;
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        draggingHue = draggingSat = draggingBri = false;
    }

    private boolean isHovered(int mx, int my, int sliderY) {
        int startX = parentModule.category.getX() + 5;
        int endX = startX + parentModule.category.getWidth() - 10;
        return mx >= startX && mx <= endX && my >= sliderY && my <= sliderY + 4;
    }

    @Override
    public boolean isVisible() {
        return property.isVisible();
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 28;
    }
}