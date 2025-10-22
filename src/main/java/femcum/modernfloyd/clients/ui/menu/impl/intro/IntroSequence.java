package femcum.modernfloyd.clients.ui.menu.impl.intro;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.ui.menu.impl.main.MainMenu;
import femcum.modernfloyd.clients.util.animation.Animation;
import femcum.modernfloyd.clients.util.animation.Easing;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import rip.vantage.commons.util.time.StopWatch;

import java.awt.*;
public class IntroSequence extends GuiScreen {
    private final StopWatch timeTracker = new StopWatch();
    private boolean started = false;

    private final Animation logoAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, 3000);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!started) {
            this.started = true;

            this.timeTracker.reset();
            this.logoAnimation.setValue(255);
            this.logoAnimation.reset();
        }

        this.logoAnimation.run(0);

        ScaledResolution sr = new ScaledResolution(mc);
        RenderUtil.color(Color.WHITE);
        RenderUtil.rectangle(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), Color.BLACK);
        RenderUtil.image(new ResourceLocation("floyd/images/splash.png"), sr.getScaledWidth() / 2D - 75,
                sr.getScaledHeight() / 2D - 25, 150, 50, new Color(255, 255, 255, (int) this.logoAnimation.getValue()));

        if (this.timeTracker.finished(4000) || Floyd.DEVELOPMENT_SWITCH) {
            mc.displayGuiScreen(new MainMenu());
            Floyd.INSTANCE.getConfigManager().setupLatestConfig();
        }
    }
}