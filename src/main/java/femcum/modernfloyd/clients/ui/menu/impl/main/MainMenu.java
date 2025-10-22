package femcum.modernfloyd.clients.ui.menu.impl.main;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.font.Fonts;
import femcum.modernfloyd.clients.font.Weight;
import femcum.modernfloyd.clients.ui.menu.Menu;
import femcum.modernfloyd.clients.ui.menu.component.button.MenuButton;
import femcum.modernfloyd.clients.ui.menu.component.button.impl.MenuTextButton;
import femcum.modernfloyd.clients.ui.menu.impl.account.AccountManagerScreen;
import femcum.modernfloyd.clients.util.MouseUtil;
import femcum.modernfloyd.clients.util.animation.Animation;
import femcum.modernfloyd.clients.util.animation.Easing;
import femcum.modernfloyd.clients.util.font.Font;
import femcum.modernfloyd.clients.util.render.ColorUtil;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.util.shader.RiseShaders;
import femcum.modernfloyd.clients.util.shader.base.ShaderRenderType;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.*;
import java.io.IOException;
import static femcum.modernfloyd.clients.layer.Layers.BLUR;
import static femcum.modernfloyd.clients.layer.Layers.REGULAR;

public final class MainMenu extends Menu {
    private Animation animation = new Animation(Easing.EASE_OUT_QUINT, 600);
    private MenuTextButton singlePlayerButton;
    private MenuTextButton multiPlayerButton;
    private MenuTextButton altManagerButton;
    private MenuTextButton optionsButton;
    private MenuButton[] menuButtons;
    private boolean rice;

    @Override
    public void initGui() {
        rice = Math.random() > 0.98;
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 180;
        int buttonHeight = 24;
        int buttonSpacing = 6;
        int buttonX = centerX - buttonWidth / 2;
        int buttonY = centerY - buttonHeight / 2 - buttonSpacing / 2 - buttonHeight / 2;

        this.singlePlayerButton = new MenuTextButton(buttonX, buttonY, buttonWidth, buttonHeight, () -> mc.displayGuiScreen(new GuiSelectWorld(this)), "Singleplayer");
        this.multiPlayerButton = new MenuTextButton(buttonX, buttonY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight, () -> mc.displayGuiScreen(new GuiMultiplayer(this)), "Multiplayer");
        this.altManagerButton = new MenuTextButton(buttonX + buttonWidth / 2 + buttonSpacing / 2, buttonY + buttonHeight * 2 + buttonSpacing * 2, buttonWidth / 2 - buttonSpacing / 2, buttonHeight, () -> mc.displayGuiScreen(new AccountManagerScreen(this)), "Alts");
        this.optionsButton = new MenuTextButton(buttonX, buttonY + buttonHeight * 2 + buttonSpacing * 2, buttonWidth / 2 - buttonSpacing / 2, buttonHeight, () -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)), "Options");
        this.animation = new Animation(Easing.EASE_OUT_QUINT, 600);
        this.menuButtons = new MenuButton[]{singlePlayerButton, multiPlayerButton, altManagerButton, optionsButton};
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.singlePlayerButton == null || this.multiPlayerButton == null || this.altManagerButton == null) {
            return;
        }

        ScaledResolution scaledResolution = mc.scaledResolution;

        // Render background shader
        RiseShaders.MAIN_MENU_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, null);
        getLayer(BLUR).add(() -> RenderUtil.rectangle(0, 0, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), Color.BLACK));

        // Render buttons
        for (MenuButton menuButton : menuButtons) {
            menuButton.draw(mouseX, mouseY, partialTicks);
        }

        // Render logo and text
        Font fontRenderer = Fonts.MAIN.get(64, Weight.REGULAR);
        final double destination = this.singlePlayerButton.getY() - fontRenderer.height();
        this.animation.run(destination);
        String name = rice ? "fl0yD" : Floyd.NAME;
        final double value = this.animation.getValue();
        final Color color = ColorUtil.withAlpha(Color.WHITE, (int) (value / destination * 200));

        getLayer(REGULAR).add(() -> {
            fontRenderer.drawCentered(name, width / 2.0F, value, color.getRGB());
            Fonts.MAIN.get(16, Weight.REGULAR).drawRight(Floyd.CREDITS,
                    scaledResolution.getScaledWidth() - 5, scaledResolution.getScaledHeight() - 20,
                    ColorUtil.withAlpha(TEXT_SUBTEXT, 100).getRGB());
            Fonts.MAIN.get(12, Weight.REGULAR).drawRight(Floyd.COPYRIGHT,
                    scaledResolution.getScaledWidth() - 5, scaledResolution.getScaledHeight() - 10,
                    ColorUtil.withAlpha(TEXT_SUBTEXT, 100).getRGB());
        });
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.menuButtons == null) return;
        if (mouseButton == 0) {
            for (MenuButton menuButton : this.menuButtons) {
                if (MouseUtil.isHovered(menuButton.getX(), menuButton.getY(), menuButton.getWidth(), menuButton.getHeight(), mouseX, mouseY)) {
                    menuButton.runAction();
                    break;
                }
            }
        }
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        switch (keyCode) {
            case 203:
                System.out.println("Reconnecting");
                break;
        }
    }
}