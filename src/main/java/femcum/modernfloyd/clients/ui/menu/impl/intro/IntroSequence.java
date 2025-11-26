// File: IntroSequence.java

package femcum.modernfloyd.clients.ui.menu.impl.intro;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.ui.menu.impl.main.MainMenu;
import femcum.modernfloyd.clients.util.animation.Animation;
import femcum.modernfloyd.clients.util.animation.Easing;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.util.shader.RiseShaders;
import femcum.modernfloyd.clients.util.shader.base.ShaderRenderType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class IntroSequence extends GuiScreen {

    // Animation controller for smooth transitions
    private final Animation logoAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, 3000);

    // Timing constants for intro sequence
    private static final long SHADER_FADE_DURATION = 2000;
    private static final long SHADER_SOLO_DURATION = 3000;
    private static final long LOGO_FADE_DURATION = 1500;
    private static final long SPLASH_DISPLAY_DURATION = 3000;

    // State tracking
    private boolean initialized = false;
    private long introStartTime = 0;
    private long shaderStartTime = 0;

    @Override
    public void initGui() {
        if (!initialized) {
            initialized = true;
            logoAnimation.setValue(255);
            logoAnimation.reset();
            introStartTime = System.currentTimeMillis();
            shaderStartTime = System.currentTimeMillis();
            RiseShaders.INTRO_SHADER.update();
            System.out.println("[Floyd] Intro sequence initialized");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        long currentTime = System.currentTimeMillis();
        long timeSinceStart = currentTime - shaderStartTime;

        // Render black background
        drawBackground(sr);

        // Calculate and render shader with fade-in effect
        float shaderAlpha = calculateShaderAlpha(timeSinceStart);
        renderShader(partialTicks, shaderAlpha);

        // Render blur overlay
        renderBlurOverlay(sr);

        // Render splash content after shader solo period
        if (timeSinceStart >= SHADER_SOLO_DURATION) {
            renderSplashContent(sr, timeSinceStart);
        }

        // Check if intro should complete
        checkIntroCompletion(currentTime);
    }

    private void drawBackground(ScaledResolution sr) {
        RenderUtil.rectangle(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), Color.BLACK);
    }

    private float calculateShaderAlpha(long timeSinceStart) {
        return Math.min(1.0f, timeSinceStart / (float) SHADER_FADE_DURATION);
    }

    private void renderShader(float partialTicks, float alpha) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);

        RiseShaders.INTRO_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, null);

        // Reset GL state
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderBlurOverlay(ScaledResolution sr) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.3f);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(sr.getScaledWidth(), 0);
        GL11.glVertex2f(sr.getScaledWidth(), sr.getScaledHeight());
        GL11.glVertex2f(0, sr.getScaledHeight());
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderSplashContent(ScaledResolution sr, long timeSinceStart) {
        long fadeTime = timeSinceStart - SHADER_SOLO_DURATION;
        int alpha = calculateLogoAlpha(fadeTime);

        if (alpha > 0) {
            renderLogo(sr, alpha);

            if (alpha >= 255) {
                renderText(sr);
            }
        }
    }

    private int calculateLogoAlpha(long fadeTime) {
        return (int) Math.min(255, (fadeTime / (double) LOGO_FADE_DURATION) * 255);
    }

    private void renderLogo(ScaledResolution sr, int alpha) {
        // Reset GL state for logo rendering
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        RenderUtil.color(Color.WHITE);
        RenderUtil.image(
                new net.minecraft.util.ResourceLocation("floyd/images/splash.png"),
                sr.getScaledWidth() / 2.0 - 75,
                sr.getScaledHeight() / 2.0 - 25,
                150,
                50,
                new Color(255, 255, 255, alpha)
        );
    }

    private void renderText(ScaledResolution sr) {
        String text = "FloydCEO Presents...";
        int color = Color.WHITE.getRGB();
        float textX = (sr.getScaledWidth() - mc.fontRendererObj.width(text)) / 2.0f;
        float textY = sr.getScaledHeight() / 2.0f + 50;

        drawTextWithStroke(text, textX, textY, color);
    }

    private void drawTextWithStroke(String text, float x, float y, int color) {
        Color shadow = new Color(0, 0, 0, 180);

        // Draw shadow outline
        mc.fontRendererObj.draw(text, x + 1, y + 1, shadow.getRGB());
        mc.fontRendererObj.draw(text, x - 1, y - 1, shadow.getRGB());
        mc.fontRendererObj.draw(text, x + 1, y - 1, shadow.getRGB());
        mc.fontRendererObj.draw(text, x - 1, y + 1, shadow.getRGB());

        // Draw main text
        mc.fontRendererObj.draw(text, x, y, color);
    }

    private void checkIntroCompletion(long currentTime) {
        long totalIntroTime = currentTime - introStartTime;
        long totalDuration = SHADER_SOLO_DURATION + SPLASH_DISPLAY_DURATION;

        if (totalIntroTime >= totalDuration && Floyd.DEVELOPMENT_SWITCH) {
            System.out.println("[Floyd] Transitioning to main menu");
            mc.displayGuiScreen(new MainMenu());
            Floyd.INSTANCE.getConfigManager().setupLatestConfig();
        }
    }

    @Override
    public void onGuiClosed() {
        // Cleanup resources if needed
        System.out.println("[Floyd] Intro sequence closed");
    }
}