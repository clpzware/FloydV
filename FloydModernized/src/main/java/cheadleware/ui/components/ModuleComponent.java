package cheadleware.ui.components;

import cheadleware.Cheadleware;
import cheadleware.module.Module;
import cheadleware.module.modules.Render.HUD;
import cheadleware.property.Property;
import cheadleware.property.properties.*;
import cheadleware.ui.Component;
import cheadleware.ui.dataset.impl.FloatSlider;
import cheadleware.ui.dataset.impl.IntSlider;
import cheadleware.ui.dataset.impl.PercentageSlider;
import cheadleware.util.font.FontManager;
import cheadleware.util.tenacityshaders.RenderUtil;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ModuleComponent implements Component {
    public Module mod;
    public CategoryComponent category;
    public int offsetY;
    private final ArrayList<Component> settings;
    public boolean panelExpand;
    private float hoverAnimation = 0F;
    private float enableAnimation = 0F;
    private long lastFrameTime = System.currentTimeMillis();

    public ModuleComponent(Module mod, CategoryComponent category, int offsetY) {
        this.mod = mod;
        this.category = category;
        this.offsetY = offsetY;
        this.settings = new ArrayList<>();
        this.panelExpand = false;
        int y = offsetY + 12;
        if (!Cheadleware.propertyManager.properties.get(mod.getClass()).isEmpty()) {
            for (Property<?> baseProperty : Cheadleware.propertyManager.properties.get(mod.getClass())) {
                if (baseProperty instanceof BooleanProperty) {
                    BooleanProperty property = (BooleanProperty) baseProperty;
                    CheckBoxComponent c = new CheckBoxComponent(property, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof FloatProperty) {
                    FloatProperty property = (FloatProperty) baseProperty;
                    SliderComponent c = new SliderComponent(new FloatSlider(property), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof IntProperty) {
                    IntProperty property = (IntProperty) baseProperty;
                    SliderComponent c = new SliderComponent(new IntSlider(property), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof PercentProperty) {
                    PercentProperty property = (PercentProperty) baseProperty;
                    SliderComponent c = new SliderComponent(new PercentageSlider(property), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof ModeProperty) {
                    ModeProperty property = (ModeProperty) baseProperty;
                    ModeComponent c = new ModeComponent(property, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof ColorProperty) {
                    ColorProperty property = (ColorProperty) baseProperty;
                    ColorSliderComponent c = new ColorSliderComponent(property, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof TextProperty) {
                    TextProperty property = (TextProperty) baseProperty;
                    TextComponent c = new TextComponent(property, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                }
            }
        }

        this.settings.add(new BindComponent(this, y));
    }

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
        int y = this.offsetY + 20;

        for (Component c : this.settings) {
            c.setComponentStartAt(y);
            if (c.isVisible()) {
                y += c.getHeight();
            }
        }
    }

    public void draw(AtomicInteger offset) {
        // Update animations
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000F;
        lastFrameTime = currentTime;

        float targetEnable = this.mod.isEnabled() ? 1F : 0F;
        enableAnimation += (targetEnable - enableAnimation) * Math.min(deltaTime * 8F, 1F);

        int mouseX = 0, mouseY = 0;
        try {
            mouseX = org.lwjgl.input.Mouse.getX() * category.getWidth() / Minecraft.getMinecraft().displayWidth;
            mouseY = category.getWidth() - org.lwjgl.input.Mouse.getY() * category.getWidth() / Minecraft.getMinecraft().displayHeight - 1;
        } catch (Exception e) {}

        boolean hovered = isHovered(mouseX, mouseY);
        float targetHover = hovered ? 1F : 0F;
        hoverAnimation += (targetHover - hoverAnimation) * Math.min(deltaTime * 12F, 1F);

        int startX = this.category.getX() + 2;
        int startY = this.category.getY() + this.offsetY;
        int endX = this.category.getX() + this.category.getWidth() - 2;

        // Background on hover or when enabled (squared)
        if (hoverAnimation > 0.01F || enableAnimation > 0.01F) {
            float alpha = Math.max(hoverAnimation * 0.3F, enableAnimation * 0.5F);
            Color bgColor = this.mod.isEnabled()
                    ? new Color(80, 150, 255, (int)(alpha * 255))
                    : new Color(60, 60, 70, (int)(alpha * 255));

            net.minecraft.client.gui.Gui.drawRect(startX, startY, endX, startY + 20,
                    new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha()).getRGB());
        }

        // Module name with Sans font centered
        int textColor;
        if (this.mod.isEnabled()) {
            textColor = new Color(255, 255, 255, 255).getRGB();
        } else {
            int gray = 160 + (int)(40 * hoverAnimation);
            textColor = new Color(gray, gray, gray).getRGB();
        }

        float textX = this.category.getX() + this.category.getWidth() / 2F -
                FontManager.SANS.getWidth(this.mod.getName()) / 2F;
        FontManager.SANS.drawString(this.mod.getName(), textX, this.category.getY() + this.offsetY + 6, textColor);

        if (this.panelExpand && !this.settings.isEmpty()) {
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    c.draw(offset);
                    offset.incrementAndGet();
                }
            }
        }
    }

    public int getHeight() {
        if (!this.panelExpand) {
            return 20;
        } else {
            int h = 20;
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    h += c.getHeight();
                }
            }
            return h;
        }
    }

    public void update(int mousePosX, int mousePosY) {
        if(!panelExpand) return;
        if (!this.settings.isEmpty()) {
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    c.update(mousePosX, mousePosY);
                }
            }
        }
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0) {
            this.mod.toggle();
        }

        if (this.isHovered(x, y) && button == 1) {
            this.panelExpand = !this.panelExpand;
        }

        if(!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) {
                c.mouseDown(x, y, button);
            }
        }
    }

    public void mouseReleased(int x, int y, int button) {
        if(!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) {
                c.mouseReleased(x, y, button);
            }
        }
    }

    public void keyTyped(char chatTyped, int keyCode) {
        if(!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) {
                c.keyTyped(chatTyped, keyCode);
            }
        }
    }

    public boolean isHovered(int x, int y) {
        return x > this.category.getX() && x < this.category.getX() + this.category.getWidth() &&
                y > this.category.getY() + this.offsetY && y < this.category.getY() + 20 + this.offsetY;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}