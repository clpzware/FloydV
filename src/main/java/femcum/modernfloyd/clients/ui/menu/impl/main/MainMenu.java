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
    private MenuTextButton quitButton;
    private MenuButton[] menuButtons;
    private boolean rice;

    @Override
    public void initGui() {
        rice = Math.random() > 0.98;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Button dimensions
        int largeButtonWidth = 140;
        int smallButtonWidth = 90;
        int buttonHeight = 28;
        int spacing = 8;
        int quitButtonWidth = 100;

        // Row 1: Singleplayer and Multiplayer (side by side)
        int row1Y = centerY - buttonHeight - spacing / 2;
        int row1TotalWidth = largeButtonWidth * 2 + spacing;
        int row1StartX = centerX - row1TotalWidth / 2;

        this.singlePlayerButton = new MenuTextButton(
                row1StartX,
                row1Y,
                largeButtonWidth,
                buttonHeight,
                () -> mc.displayGuiScreen(new GuiSelectWorld(this)),
                "Singleplayer"
        );

        this.multiPlayerButton = new MenuTextButton(
                row1StartX + largeButtonWidth + spacing,
                row1Y,
                largeButtonWidth,
                buttonHeight,
                () -> mc.displayGuiScreen(new GuiMultiplayer(this)),
                "Multiplayer"
        );

        // Row 2: Options and Alts (side by side, centered)
        int row2Y = centerY + spacing / 2;
        int row2TotalWidth = smallButtonWidth * 2 + spacing;
        int row2StartX = centerX - row2TotalWidth / 2;

        this.optionsButton = new MenuTextButton(
                row2StartX,
                row2Y,
                smallButtonWidth,
                buttonHeight,
                () -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)),
                "Settings"
        );

        this.altManagerButton = new MenuTextButton(
                row2StartX + smallButtonWidth + spacing,
                row2Y,
                smallButtonWidth,
                buttonHeight,
                () -> mc.displayGuiScreen(new AccountManagerScreen(this)),
                "Alts"
        );

        // Row 3: Quit button (centered below)
        int row3Y = row2Y + buttonHeight + spacing + 4;

        this.quitButton = new MenuTextButton(
                centerX - quitButtonWidth / 2,
                row3Y,
                quitButtonWidth,
                buttonHeight,
                () -> mc.shutdown(),
                "Quit"
        );

        this.animation = new Animation(Easing.EASE_OUT_QUINT, 600);
        this.menuButtons = new MenuButton[]{
                singlePlayerButton,
                multiPlayerButton,
                optionsButton,
                altManagerButton,
                quitButton
        };
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
        final double destination = this.singlePlayerButton.getY() - fontRenderer.height() - 20;
        this.animation.run(destination);
        String name = rice ? "Floyd" : "Floyd";
        final double value = this.animation.getValue();
        final Color color = ColorUtil.withAlpha(Color.WHITE, (int) (value / destination * 200));

        getLayer(REGULAR).add(() -> {
            fontRenderer.drawCentered(name, width / 2.0F, value, color.getRGB());

            // Version info below logo
           // Font versionFont = Fonts.MAIN.get(16, Weight.REGULAR);
           // versionFont.drawCentered("v" + Floyd.VERSION, width / 2.0F, value + fontRenderer.height() + 4,
            //        ColorUtil.withAlpha(Color.WHITE, (int) (value / destination * 150)).getRGB());

            // Credits and copyright in bottom right
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