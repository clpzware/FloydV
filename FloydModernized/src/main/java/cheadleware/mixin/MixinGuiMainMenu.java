package cheadleware.mixin;

import cheadleware.ui.altmanager.GuiAltManager;
import cheadleware.util.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {

    @Unique
    private ResourceLocation cheadleware$BACKGROUND = null;
    @Unique
    private ResourceLocation cheadleware$LOGO = null;

    @Unique
    private boolean cheadleware$textureLoadAttempted = false;

    @Shadow
    private String splashText;

    @Unique
    private final List<String> cheadleware$buttonTexts = new ArrayList<>();
    @Unique
    private final List<Float> cheadleware$buttonX = new ArrayList<>();
    @Unique
    private final List<Float> cheadleware$buttonY = new ArrayList<>();
    @Unique
    private final List<Float> cheadleware$buttonW = new ArrayList<>();
    @Unique
    private final List<Float> cheadleware$buttonH = new ArrayList<>();
    @Unique
    private final List<Float> cheadleware$hoverAnim = new ArrayList<>();

    @Unique
    private void cheadleware$loadTextures() {
        if (cheadleware$textureLoadAttempted) return;
        cheadleware$textureLoadAttempted = true;

        try {
            InputStream bgStream = getClass().getClassLoader().getResourceAsStream("assets/textures/mainmenu.png");
            if (bgStream != null) {
                BufferedImage bgImage = ImageIO.read(bgStream);
                cheadleware$BACKGROUND = mc.getTextureManager().getDynamicTextureLocation("cheadleware_bg", new DynamicTexture(bgImage));
                bgStream.close();
                System.out.println("[MainMenu] Background loaded successfully!");
            } else {
                System.out.println("[MainMenu] Background file not found");
            }
        } catch (Exception e) {
            System.out.println("[MainMenu] Failed to load background: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("assets/textures/cheadleware.png");
            if (logoStream != null) {
                BufferedImage logoImage = ImageIO.read(logoStream);
                cheadleware$LOGO = mc.getTextureManager().getDynamicTextureLocation("cheadleware_logo", new DynamicTexture(logoImage));
                logoStream.close();
                System.out.println("[MainMenu] Logo loaded successfully!");
            } else {
                System.out.println("[MainMenu] Logo file not found");
            }
        } catch (Exception e) {
            System.out.println("[MainMenu] Failed to load logo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void initGui() {
        this.splashText = "";

        if (!cheadleware$textureLoadAttempted) {
            cheadleware$loadTextures();
        }

        cheadleware$buttonTexts.clear();
        cheadleware$buttonX.clear();
        cheadleware$buttonY.clear();
        cheadleware$buttonW.clear();
        cheadleware$buttonH.clear();
        cheadleware$hoverAnim.clear();

        // Row 1
        cheadleware$buttonTexts.add("Singleplayer");
        cheadleware$buttonTexts.add("Multiplayer");
        cheadleware$buttonTexts.add("Options");

        // Row 2
        cheadleware$buttonTexts.add("Alt Manager");
        cheadleware$buttonTexts.add("Exit");

        for (int i = 0; i < cheadleware$buttonTexts.size(); i++) {
            cheadleware$buttonX.add(0f);
            cheadleware$buttonY.add(0f);
            cheadleware$buttonW.add(0f);
            cheadleware$buttonH.add(0f);
            cheadleware$hoverAnim.add(0f);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw background
        if (cheadleware$BACKGROUND != null) {
            try {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(cheadleware$BACKGROUND);
                Gui.drawScaledCustomSizeModalRect(0, 0, 0, 0, this.width, this.height,
                        this.width, this.height, this.width, this.height);
            } catch (Exception e) {
                drawRect(0, 0, this.width, this.height, new Color(20, 20, 25).getRGB());
            }
        } else {
            drawRect(0, 0, this.width, this.height, new Color(20, 20, 25).getRGB());
        }

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        // Draw logo centered at top
        if (cheadleware$LOGO != null) {
            try {
                int logoSize = 80;
                float logoX = this.width / 2f - logoSize / 2f;
                float logoY = this.height / 2f - 120;

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(cheadleware$LOGO);
                Gui.drawScaledCustomSizeModalRect((int)logoX, (int)logoY, 0, 0, logoSize, logoSize,
                        logoSize, logoSize, logoSize, logoSize);
            } catch (Exception e) {
                // Logo failed to render
            }
        }

        // Calculate button positions
        float buttonWidth = 55;
        float buttonHeight = 14;
        float gapX = 8;
        float gapY = 5;

        // Row 1: 3 buttons
        float row1Width = buttonWidth * 3 + gapX * 2;
        float row1StartX = this.width / 2f - row1Width / 2f;
        float row1Y = this.height / 2f - 20;

        // Row 2: 2 buttons
        float row2Width = buttonWidth * 2 + gapX;
        float row2StartX = this.width / 2f - row2Width / 2f;
        float row2Y = row1Y + buttonHeight + gapY;

        // Set button positions
        for (int i = 0; i < 3; i++) {
            cheadleware$buttonX.set(i, row1StartX + i * (buttonWidth + gapX));
            cheadleware$buttonY.set(i, row1Y);
            cheadleware$buttonW.set(i, buttonWidth);
            cheadleware$buttonH.set(i, buttonHeight);
        }

        for (int i = 3; i < 5; i++) {
            cheadleware$buttonX.set(i, row2StartX + (i - 3) * (buttonWidth + gapX));
            cheadleware$buttonY.set(i, row2Y);
            cheadleware$buttonW.set(i, buttonWidth);
            cheadleware$buttonH.set(i, buttonHeight);
        }

        // Draw buttons
        for (int i = 0; i < cheadleware$buttonTexts.size(); i++) {
            float x = cheadleware$buttonX.get(i);
            float y = cheadleware$buttonY.get(i);
            float w = cheadleware$buttonW.get(i);
            float h = cheadleware$buttonH.get(i);

            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

            float currentAnim = cheadleware$hoverAnim.get(i);
            if (hovered && currentAnim < 1.0F) {
                currentAnim += 0.1F;
            } else if (!hovered && currentAnim > 0.0F) {
                currentAnim -= 0.1F;
            }
            currentAnim = Math.max(0F, Math.min(1F, currentAnim));
            cheadleware$hoverAnim.set(i, currentAnim);

            // Button background - subtle dark
            int bgAlpha = (int)(140 + 40 * currentAnim);
            drawRect((int)x, (int)y, (int)(x + w), (int)(y + h),
                    new Color(30, 30, 35, bgAlpha).getRGB());

            // Subtle border on hover
            if (currentAnim > 0) {
                int borderAlpha = (int)(60 * currentAnim);
                // Top border
                drawRect((int)x, (int)y, (int)(x + w), (int)(y + 1),
                        new Color(200, 200, 200, borderAlpha).getRGB());
                // Bottom border
                drawRect((int)x, (int)(y + h - 1), (int)(x + w), (int)(y + h),
                        new Color(200, 200, 200, borderAlpha).getRGB());
            }

            // Button text
            String btnText = cheadleware$buttonTexts.get(i);
            int textAlpha = hovered ? 255 : 200;
            Color textColor = new Color(255, 255, 255, textAlpha);

            try {
                float btnTextW = FontManager.SANS.getWidth(btnText);
                FontManager.SANS.drawString(btnText,
                        x + w / 2 - btnTextW / 2,
                        y + h / 2 - 4,
                        textColor.getRGB());
            } catch (Exception e) {
                mc.fontRendererObj.drawString(btnText,
                        (int)(x + w / 2 - mc.fontRendererObj.getStringWidth(btnText) / 2),
                        (int)(y + h / 2 - 4),
                        textColor.getRGB());
            }
        }

        // Draw "Happy Halloween!" text at bottom
        String halloweenText = "Happy Halloween!";
        float halloweenY = row2Y + buttonHeight + 30;

        try {
            float halloweenW = FontManager.SANS.getWidth(halloweenText);
            FontManager.SANS.drawString(halloweenText,
                    this.width / 2f - halloweenW / 2,
                    halloweenY,
                    new Color(255, 165, 0, 255).getRGB()); // Orange color
        } catch (Exception e) {
            mc.fontRendererObj.drawString(halloweenText,
                    this.width / 2 - mc.fontRendererObj.getStringWidth(halloweenText) / 2,
                    (int)halloweenY,
                    new Color(255, 165, 0, 255).getRGB());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (int i = 0; i < cheadleware$buttonTexts.size(); i++) {
                float x = cheadleware$buttonX.get(i);
                float y = cheadleware$buttonY.get(i);
                float w = cheadleware$buttonW.get(i);
                float h = cheadleware$buttonH.get(i);

                if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                    String buttonText = cheadleware$buttonTexts.get(i);
                    switch (buttonText) {
                        case "Singleplayer":
                            mc.displayGuiScreen(new GuiSelectWorld(this));
                            break;
                        case "Multiplayer":
                            mc.displayGuiScreen(new GuiMultiplayer(this));
                            break;
                        case "Options":
                            mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                            break;
                        case "Alt Manager":
                            mc.displayGuiScreen(new GuiAltManager(this));
                            break;
                        case "Exit":
                            mc.shutdown();
                            break;
                    }
                    break;
                }
            }
        }
    }
}