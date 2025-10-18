package cheadleware.module.modules.Render;

import cheadleware.Cheadleware;
import cheadleware.module.Module;
import cheadleware.property.properties.BooleanProperty;
import cheadleware.property.properties.IntProperty;
import cheadleware.util.tenacityshaders.RenderUtil;
import cheadleware.util.tenacityshaders.blur.BloomUtil;
import cheadleware.util.tenacityshaders.blur.KawaseBloom;
import cheadleware.util.tenacityshaders.blur.KawaseBlur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PostProcessing extends Module {

    protected final Minecraft mc = Minecraft.getMinecraft();

    public final BooleanProperty blur = new BooleanProperty("Blur", true);
    private final IntProperty iterations = new IntProperty("Blur Iterations", 2, 1, 8, blur::getValue);
    private final IntProperty offset = new IntProperty("Blur Offset", 3, 1, 10, blur::getValue);

    public final BooleanProperty bloom = new BooleanProperty("Bloom", true);
    private final IntProperty bloomRadius = new IntProperty("Bloom Radius", 3, 1, 8, bloom::getValue);
    private final IntProperty bloomOffset = new IntProperty("Bloom Offset", 1, 1, 10, bloom::getValue);

    public PostProcessing() {
        super("PostProcessing", false);
    }

    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    // This method will be called by HUD module before it renders text
    public void renderEffects() {
        blurScreen();
    }

    public void blurScreen() {
        if (!enabled) return;

        if (blur.getValue()) {
            stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);

            // Render what should be blurred (backgrounds only)
            stuffToBlur(false);

            stencilFramebuffer.unbindFramebuffer();

            // Apply Kawase blur effect
            KawaseBlur.renderBlur(stencilFramebuffer.framebufferTexture, iterations.getValue(), offset.getValue());
        }

        if (bloom.getValue()) {
            stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);

            // Render what should have bloom (colored bars)
            stuffToBlur(true);

            stencilFramebuffer.unbindFramebuffer();

            // Apply Kawase bloom effect (only using KawaseBloom for now)
            KawaseBloom.renderBlur(stencilFramebuffer.framebufferTexture, bloomRadius.getValue(), bloomOffset.getValue());
        }
    }

    private void stuffToBlur(boolean isBloom) {
        ScaledResolution sr = new ScaledResolution(mc);

        // Reset color state
        RenderUtil.resetColor();

        // Render arraylist backgrounds/bars for blur/bloom
        HUD hud = (HUD) Cheadleware.moduleManager.getModule(HUD.class);
        if (hud != null && hud.isEnabled()) {
            if (isBloom) {
                // For bloom, render the colored bars
                renderArraylistBars(hud, sr);
            } else {
                // For blur, render the backgrounds
                renderArraylistBackgrounds(hud, sr);
            }
        }

        // Reset color again after rendering
        RenderUtil.resetColor();
    }

    private void renderArraylistBackgrounds(HUD hud, ScaledResolution sr) {
        // Get the active modules with animations just like HUD does
        List<Module> activeModules = new ArrayList<>(hud.activeModules);

        if (activeModules.isEmpty()) return;

        boolean topArrayList = hud.posY.getValue() == 0;
        boolean rightSide = hud.posX.getValue() == 1;
        int screenX = sr.getScaledWidth();
        int screenY = sr.getScaledHeight();

        // Use the same calculations as HUD
        int textHeight = getStringHeight(hud, "A") + 2;
        int textOffset = textHeight - 2;
        int offset = topArrayList ? textHeight : -textHeight;
        int y = topArrayList ? hud.offsetY.getValue() : screenY - textOffset - hud.offsetY.getValue();

        for (Module module : activeModules) {
            // Get animation
            cheadleware.util.animations.impl.DecelerateAnimation anim = HUD.moduleAnimations.get(module);
            if (anim == null) continue;
            if (!module.isEnabled() && anim.finished(cheadleware.util.animations.Direction.BACKWARDS)) continue;

            float animProgress = anim.getOutput().floatValue();
            if (animProgress <= 0.01f) continue;

            String name = HUD.displayLabelCache.get(module);
            if (name == null) continue;

            float moduleWidth = getStringWidth(hud, name);

            int bgStartX;
            int bgEndX;

            float slideOffset = (1 - animProgress) * (rightSide ? 20 : -20);

            if (rightSide) {
                bgStartX = (int) (screenX - moduleWidth - hud.offsetX.getValue() - (hud.showBar.getValue() ? 3 : 2) + slideOffset);
                bgEndX = (int) (screenX - hud.offsetX.getValue() + slideOffset);
            } else {
                bgStartX = (int) (hud.offsetX.getValue() + slideOffset);
                bgEndX = (int) (hud.offsetX.getValue() + moduleWidth + (hud.showBar.getValue() ? 3 : 2) + slideOffset);
            }

            int top = y - 2;

            // Draw background rectangle (white for visibility)
            Gui.drawRect(bgStartX, top, bgEndX, y + textOffset, 0xFFFFFFFF);

            y += offset;
        }
    }

    // Helper methods to match HUD's string width/height calculations EXACTLY
    private int getStringWidth(HUD hud, String text) {
        return hud.getStringWidth(text);
    }

    private int getStringHeight(HUD hud, String text) {
        return hud.getStringHeight(text);
    }

    private void renderArraylistBars(HUD hud, ScaledResolution sr) {
        // Get the active modules with animations just like HUD does
        List<Module> activeModules = new ArrayList<>(hud.activeModules);

        if (activeModules.isEmpty()) return;

        boolean topArrayList = hud.posY.getValue() == 0;
        boolean rightSide = hud.posX.getValue() == 1;
        int screenX = sr.getScaledWidth();
        int screenY = sr.getScaledHeight();

        // Use the same calculations as HUD
        int textHeight = getStringHeight(hud, "A") + 2;
        int textOffset = textHeight - 2;
        int offset = topArrayList ? textHeight : -textHeight;
        int y = topArrayList ? hud.offsetY.getValue() : screenY - textOffset - hud.offsetY.getValue();

        long currentMillis = System.currentTimeMillis();
        int visibleModuleIndex = 0;

        for (Module module : activeModules) {
            // Get animation
            cheadleware.util.animations.impl.DecelerateAnimation anim = HUD.moduleAnimations.get(module);
            if (anim == null) continue;
            if (!module.isEnabled() && anim.finished(cheadleware.util.animations.Direction.BACKWARDS)) continue;

            float animProgress = anim.getOutput().floatValue();
            if (animProgress <= 0.01f) continue;

            String name = HUD.displayLabelCache.get(module);
            if (name == null) continue;

            // Get the color from HUD
            Color moduleColor = hud.getColor(currentMillis, visibleModuleIndex);

            int barStartX;
            int barEndX;

            float slideOffset = (1 - animProgress) * (rightSide ? 20 : -20);

            if (rightSide) {
                // Make bars wider for bloom (3 pixels instead of 1)
                barStartX = (int) (screenX - hud.offsetX.getValue() - 3 + slideOffset);
                barEndX = (int) (screenX - hud.offsetX.getValue() + slideOffset);
            } else {
                barStartX = (int) (hud.offsetX.getValue() + slideOffset);
                barEndX = (int) (hud.offsetX.getValue() + 3 + slideOffset);
            }

            int top = y - 2;

            // Draw the colored bar for bloom effect with full opacity
            int barColor = moduleColor.getRGB() | 0xFF000000; // Force full alpha
            Gui.drawRect(barStartX, top, barEndX, y + textOffset, barColor);

            y += offset;

            if (module.isEnabled() || animProgress > 0.1f) {
                visibleModuleIndex++;
            }
        }
    }
}