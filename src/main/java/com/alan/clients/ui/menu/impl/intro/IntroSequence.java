package com.alan.clients.ui.menu.impl.intro;

import com.alan.clients.Client;
import com.alan.clients.ui.menu.impl.main.MainMenu;
import com.alan.clients.util.animation.Animation;
import com.alan.clients.util.animation.Easing;
import com.alan.clients.util.render.RenderUtil;
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
        RenderUtil.image(new ResourceLocation("rise/images/splash.png"), sr.getScaledWidth() / 2D - 75,
                sr.getScaledHeight() / 2D - 25, 150, 50, new Color(255, 255, 255, (int) this.logoAnimation.getValue()));

        if (this.timeTracker.finished(4000) || Client.DEVELOPMENT_SWITCH) {
            mc.displayGuiScreen(new MainMenu());
            Client.INSTANCE.getConfigManager().setupLatestConfig();
        }
    }
}