package cheadleware.ui.components;

import cheadleware.module.Module;
import cheadleware.ui.Component;
import cheadleware.util.font.FontManager;
import cheadleware.util.tenacityshaders.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryComponent {
    private final int MAX_HEIGHT = 300;

    public ArrayList<Component> modulesInCategory = new ArrayList<>();
    public String categoryName;
    private boolean categoryOpened;
    private int width;
    private int y;
    private int x;
    private final int bh;
    public boolean dragging;
    public int xx;
    public int yy;
    public boolean pin = false;
    private int scroll = 0;
    private double animScroll = 0;
    private int height = 0;
    private float openAnimation = 0F;
    private long lastFrameTime = System.currentTimeMillis();

    public CategoryComponent(String category, List<Module> modules) {
        this.categoryName = category;
        this.width = 115;
        this.x = 5;
        this.y = 5;
        this.bh = 22;
        this.xx = 0;
        this.categoryOpened = true;
        this.dragging = false;
        int tY = this.bh;
        for (Module mod : modules) {
            ModuleComponent b = new ModuleComponent(mod, this, tY);
            this.modulesInCategory.add(b);
            tY += 20;
        }
    }

    public ArrayList<Component> getModules() {
        return this.modulesInCategory;
    }

    public void setX(int n) {
        this.x = n;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void mousePressed(boolean d) {
        this.dragging = d;
    }

    public boolean isPin() {
        return this.pin;
    }

    public void setPin(boolean on) {
        this.pin = on;
    }

    public boolean isOpened() {
        return this.categoryOpened;
    }

    public void setOpened(boolean on) {
        this.categoryOpened = on;
    }

    public void render(FontRenderer renderer) {
        this.width = 115;
        update();

        // Update animation
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000F;
        lastFrameTime = currentTime;

        float targetAnim = categoryOpened ? 1F : 0F;
        openAnimation += (targetAnim - openAnimation) * Math.min(deltaTime * 8F, 1F);

        height = 0;
        for (Component moduleRenderManager : this.modulesInCategory) {
            height += moduleRenderManager.getHeight();
        }
        int maxScroll = Math.max(0, height - MAX_HEIGHT);
        if (scroll > maxScroll) scroll = maxScroll;
        if (animScroll > maxScroll) animScroll = maxScroll;
        animScroll += (scroll - animScroll) * 0.2;

        // Draw main background (squared)
        if (!this.modulesInCategory.isEmpty() && openAnimation > 0.01F) {
            int displayHeight = Math.min(height, MAX_HEIGHT);
            int animatedHeight = (int)(displayHeight * openAnimation);

            // Background squared
            net.minecraft.client.gui.Gui.drawRect(this.x, this.y + this.bh,
                    this.x + this.width, this.y + this.bh + animatedHeight,
                    new Color(35, 35, 40, 240).getRGB());
        }

        // Draw header (squared)
        net.minecraft.client.gui.Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.bh,
                new Color(50, 50, 55, 255).getRGB());

        // Category name with Sans font centered
        GlStateManager.pushMatrix();
        float textX = this.x + this.width / 2F - FontManager.SANS.getWidth(this.categoryName) / 2F;
        FontManager.SANS.drawString(this.categoryName, textX, this.y + 7, new Color(255, 255, 255).getRGB());
        GlStateManager.popMatrix();

        if (openAnimation > 0.01F && !this.modulesInCategory.isEmpty()) {
            int renderHeight = 0;
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            double scale = sr.getScaleFactor();
            int displayHeight = Math.min(height, MAX_HEIGHT);
            int animatedHeight = (int)(displayHeight * openAnimation);
            int bottom = this.y + this.bh + animatedHeight;

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int)(this.x * scale), (int)((sr.getScaledHeight() - bottom) * scale),
                    (int)(this.width * scale), (int)(animatedHeight * scale));

            GlStateManager.pushMatrix();
            GlStateManager.color(1F, 1F, 1F, openAnimation);

            for (Component c2 : this.modulesInCategory) {
                int compHeight = c2.getHeight();
                if (renderHeight + compHeight > animScroll &&
                        renderHeight < animScroll + MAX_HEIGHT) {
                    int drawY = (int)(renderHeight - animScroll);
                    c2.setComponentStartAt(this.bh + drawY);
                    c2.draw(new AtomicInteger(0));
                }
                renderHeight += compHeight;
            }

            GlStateManager.popMatrix();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            // Modern scrollbar
            if (height > MAX_HEIGHT) {
                float scrollbarHeight = ((float)MAX_HEIGHT * MAX_HEIGHT / height);
                float scrollY = (float)this.y + this.bh + (float)(animScroll * MAX_HEIGHT / height);

                // Scrollbar background
                net.minecraft.client.gui.Gui.drawRect(this.x + this.width - 3, this.y + this.bh,
                        this.x + this.width, this.y + this.bh + animatedHeight,
                        new Color(25, 25, 30, 150).getRGB());

                // Scrollbar thumb
                net.minecraft.client.gui.Gui.drawRect(this.x + this.width - 3, (int)scrollY,
                        this.x + this.width, (int)(scrollY + scrollbarHeight),
                        new Color(80, 150, 255, 200).getRGB());
            }
        }
    }

    public void update() {
        int offset = this.bh;
        for (Component component : this.modulesInCategory) {
            component.setComponentStartAt(offset);
            offset += component.getHeight();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public void handleDrag(int x, int y) {
        if (this.dragging) {
            this.setX(x - this.xx);
            this.setY(y - this.yy);
        }
    }

    public boolean isHovered(int x, int y) {
        return x >= this.x + 92 - 13 && x <= this.x + this.width && (float)y >= (float)this.y + 2.0F && y <= this.y + this.bh + 1;
    }

    public boolean mousePressed(int x, int y) {
        return x >= this.x + 77 && x <= this.x + this.width - 6 && (float)y >= (float)this.y + 2.0F && y <= this.y + this.bh + 1;
    }

    public boolean insideArea(int x, int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.bh;
    }

    public String getName() {
        return categoryName;
    }

    public void setLocation(int parseInt, int parseInt1) {
        this.x = parseInt;
        this.y = parseInt1;
    }

    public void onScroll(int mouseX, int mouseY, int scrollAmount) {
        if (!categoryOpened || height <= MAX_HEIGHT) return;

        int areaTop = this.y + this.bh;
        int areaBottom = this.y + this.bh + MAX_HEIGHT;

        if (mouseX >= this.x && mouseX <= this.x + width && mouseY >= areaTop && mouseY <= areaBottom) {
            scroll -= scrollAmount * 12;
            scroll = Math.max(0, Math.min(scroll, height - MAX_HEIGHT));
        }
    }
}