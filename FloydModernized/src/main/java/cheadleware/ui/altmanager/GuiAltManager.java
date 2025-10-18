package cheadleware.ui.altmanager;

import cheadleware.util.account.SessionChanger;
import cheadleware.util.account.APIUtils;
import cheadleware.util.account.impl.MicrosoftAccount;
import cheadleware.util.font.FontManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GuiAltManager extends GuiScreen {
    private final GuiScreen previousScreen;
    private GuiTextField usernameField;
    private GuiTextField ssidField;
    private List<StoredAccount> accounts = new ArrayList<>();
    private int scrollOffset = 0;
    private AccountType selectedType = AccountType.CRACKED;

    private ResourceLocation BACKGROUND = null;
    private boolean textureLoadAttempted = false;

    private String[] cookieData = null;
    private String statusMessage = "";

    private MicrosoftAccount microsoftAccount = null;
    private boolean waitingForMicrosoft = false;
    private long loginTimeout = 0;

    private float[] buttonHoverAnim = new float[20];

    public GuiAltManager(GuiScreen previous) {
        this.previousScreen = previous;
    }

    private void loadBackground() {
        if (textureLoadAttempted) return;
        textureLoadAttempted = true;

        try {
            InputStream bgStream = getClass().getClassLoader().getResourceAsStream("assets/textures/mainmenu.png");
            if (bgStream != null) {
                BufferedImage bgImage = ImageIO.read(bgStream);
                BACKGROUND = mc.getTextureManager().getDynamicTextureLocation("altmanager_bg", new DynamicTexture(bgImage));
                bgStream.close();
            }
        } catch (Exception e) {
        }
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        drawRect((int)x, (int)y, (int)(x + width), (int)(y + height), color.getRGB());

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void updateScreen() {
        if (usernameField != null) this.usernameField.updateCursorCounter();
        if (selectedType == AccountType.SSID && ssidField != null) {
            this.ssidField.updateCursorCounter();
        }

        if (microsoftAccount != null && microsoftAccount.isValid()) {
            if (waitingForMicrosoft) {
                statusMessage = "Microsoft login successful: " + microsoftAccount.getName();
                waitingForMicrosoft = false;
            }
        }

        if (waitingForMicrosoft && loginTimeout > 0 && System.currentTimeMillis() > loginTimeout) {
            statusMessage = "Login timeout - please try again";
            waitingForMicrosoft = false;
            microsoftAccount = null;
        }

        // Update button animations
        for (int i = 0; i < buttonHoverAnim.length; i++) {
            if (buttonHoverAnim[i] > 0) {
                buttonHoverAnim[i] = Math.max(0, buttonHoverAnim[i] - 0.05f);
            }
        }
    }

    public void initGui() {
        org.lwjgl.input.Keyboard.enableRepeatEvents(true);
        loadBackground();

        int centerX = this.width / 2;
        int fieldY = 120;

        this.usernameField = new GuiTextField(0, this.fontRendererObj, centerX - 100, fieldY, 200, 20);
        this.usernameField.setMaxStringLength(128);
        this.usernameField.setFocused(selectedType != AccountType.SSID);

        if (selectedType == AccountType.SSID) {
            this.ssidField = new GuiTextField(5, this.fontRendererObj, centerX - 100, fieldY + 35, 200, 20);
            this.ssidField.setMaxStringLength(32767);
            this.ssidField.setFocused(false);
        }
    }

    public void onGuiClosed() {
        org.lwjgl.input.Keyboard.enableRepeatEvents(false);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw background
        if (BACKGROUND != null) {
            try {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(BACKGROUND);
                drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);
            } catch (Exception e) {
                drawRect(0, 0, this.width, this.height, new Color(20, 20, 25).getRGB());
            }
        } else {
            drawRect(0, 0, this.width, this.height, new Color(20, 20, 25).getRGB());
        }

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Main panel - greyish
        float panelWidth = 450;
        float panelHeight = 350;
        float panelX = this.width / 2f - panelWidth / 2f;
        float panelY = 30;

        drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 15, new Color(40, 40, 45, 200));
        drawRoundedRect(panelX - 2, panelY - 2, panelWidth + 4, panelHeight + 4, 15, new Color(100, 100, 100, 100));
        drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 15, new Color(40, 40, 45, 200));

        // Title
        String title = "Alt Manager";
        try {
            float titleW = FontManager.LARGE_FR.getWidth(title);
            FontManager.LARGE_FR.drawStringWithShadow(title, this.width / 2f - titleW / 2f, panelY + 10,
                    new Color(255, 255, 255, 255).getRGB());
        } catch (Exception e) {
            drawCenteredString(fontRendererObj, title, this.width / 2, (int)(panelY + 10), 0xFFFFFF);
        }

        // Current session indicator
        String currentUser = "Logged in as: " + mc.getSession().getUsername();
        try {
            float userW = FontManager.SMALL_FR.getWidth(currentUser);
            FontManager.SMALL_FR.drawStringWithShadow(currentUser, this.width / 2f - userW / 2f, panelY + 30,
                    new Color(100, 255, 100, 255).getRGB());
        } catch (Exception e) {
            drawCenteredString(fontRendererObj, currentUser, this.width / 2, (int)(panelY + 30), 0x00FF00);
        }

        // Account type buttons - TINY like main menu
        int centerX = this.width / 2;
        int btnY = (int)(panelY + 50);
        int btnW = 55;  // Small like main menu
        int btnH = 14;  // Small like main menu
        int gap = 5;

        String[] types = {"Cracked", "Microsoft", "SSID", "Cookie"};
        AccountType[] typeEnums = {AccountType.CRACKED, AccountType.MICROSOFT, AccountType.SSID, AccountType.COOKIE};

        int totalWidth = btnW * 4 + gap * 3;
        int startX = centerX - totalWidth / 2;

        for (int i = 0; i < types.length; i++) {
            int btnX = startX + i * (btnW + gap);
            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
            boolean selected = selectedType == typeEnums[i];

            if (hovered && buttonHoverAnim[i] < 1.0f) {
                buttonHoverAnim[i] += 0.1f;
            } else if (!hovered && buttonHoverAnim[i] > 0.0f) {
                buttonHoverAnim[i] -= 0.1f;
            }
            buttonHoverAnim[i] = Math.max(0f, Math.min(1f, buttonHoverAnim[i]));

            // Button background - greyish
            int bgAlpha = selected ? 200 : (int)(140 + 40 * buttonHoverAnim[i]);
            Color btnBg = selected ? new Color(80, 80, 85, bgAlpha) : new Color(30, 30, 35, bgAlpha);
            drawRect(btnX, btnY, btnX + btnW, btnY + btnH, btnBg.getRGB());

            // Subtle border on hover
            if (buttonHoverAnim[i] > 0 || selected) {
                int borderAlpha = (int)(60 * (selected ? 1 : buttonHoverAnim[i]));
                drawRect(btnX, btnY, btnX + btnW, btnY + 1, new Color(200, 200, 200, borderAlpha).getRGB());
                drawRect(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, new Color(200, 200, 200, borderAlpha).getRGB());
            }

            Color textColor = new Color(255, 255, 255, hovered || selected ? 255 : 200);
            try {
                float txtW = FontManager.SANS.getWidth(types[i]);
                FontManager.SANS.drawString(types[i], btnX + btnW / 2f - txtW / 2f, btnY + btnH / 2f - 4, textColor.getRGB());
            } catch (Exception e) {
                drawCenteredString(fontRendererObj, types[i], btnX + btnW / 2, btnY + 3, textColor.getRGB());
            }
        }

        // Input fields
        if (selectedType != AccountType.MICROSOFT) {
            String label = selectedType == AccountType.SSID ? "Username:" :
                    selectedType == AccountType.COOKIE ? "Cookie File:" : "Username:";

            try {
                FontManager.SMALL_FR.drawString(label, centerX - 100, 108, new Color(180, 180, 180).getRGB());
            } catch (Exception e) {
                drawString(fontRendererObj, label, centerX - 100, 108, 0xB0B0B0);
            }

            this.usernameField.drawTextBox();
        }

        if (selectedType == AccountType.SSID && ssidField != null) {
            try {
                FontManager.SMALL_FR.drawString("Session Token:", centerX - 100, 143, new Color(180, 180, 180).getRGB());
            } catch (Exception e) {
                drawString(fontRendererObj, "Session Token:", centerX - 100, 143, 0xB0B0B0);
            }

            this.ssidField.drawTextBox();
        }

        // Action buttons - TINY
        drawActionButtons(mouseX, mouseY, centerX, (int)panelY);

        // Status message
        if (!statusMessage.isEmpty()) {
            Color statusColor = statusMessage.contains("failed") || statusMessage.contains("Invalid") || statusMessage.contains("Error")
                    ? new Color(255, 85, 85) : new Color(100, 255, 100);

            try {
                float statusW = FontManager.SMALL_FR.getWidth(statusMessage);
                FontManager.SMALL_FR.drawStringWithShadow(statusMessage, this.width / 2f - statusW / 2f, panelY + panelHeight - 25,
                        statusColor.getRGB());
            } catch (Exception e) {
                drawCenteredString(fontRendererObj, statusMessage, this.width / 2, (int)(panelY + panelHeight - 25), statusColor.getRGB());
            }
        }

        // Microsoft login status
        if (waitingForMicrosoft) {
            drawCenteredString(fontRendererObj, "Link copied to clipboard!", this.width / 2, this.height / 2, 0x00FFFF);

            long remaining = (loginTimeout - System.currentTimeMillis()) / 1000;
            if (remaining > 0) {
            }
        }

        // Account list
        drawAccountList(mouseX, mouseY);

        // Back button - TINY
        int backBtnX = this.width / 2 - 30;
        int backBtnY = (int)(panelY + panelHeight + 15);
        int backBtnW = 60;
        int backBtnH = 18;
        boolean backHovered = mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= backBtnY && mouseY <= backBtnY + backBtnH;

        if (backHovered && buttonHoverAnim[19] < 1.0f) {
            buttonHoverAnim[19] += 0.1f;
        } else if (!backHovered && buttonHoverAnim[19] > 0.0f) {
            buttonHoverAnim[19] -= 0.1f;
        }
        buttonHoverAnim[19] = Math.max(0f, Math.min(1f, buttonHoverAnim[19]));

        int bgAlpha = (int)(140 + 40 * buttonHoverAnim[19]);
        drawRect(backBtnX, backBtnY, backBtnX + backBtnW, backBtnY + backBtnH, new Color(30, 30, 35, bgAlpha).getRGB());

        if (buttonHoverAnim[19] > 0) {
            int borderAlpha = (int)(60 * buttonHoverAnim[19]);
            drawRect(backBtnX, backBtnY, backBtnX + backBtnW, backBtnY + 1, new Color(200, 200, 200, borderAlpha).getRGB());
            drawRect(backBtnX, backBtnY + backBtnH - 1, backBtnX + backBtnW, backBtnY + backBtnH, new Color(200, 200, 200, borderAlpha).getRGB());
        }

        try {
            float backW = FontManager.SANS.getWidth("Back");
            FontManager.SANS.drawString("Back", backBtnX + backBtnW / 2f - backW / 2f, backBtnY + backBtnH / 2f - 4,
                    new Color(255, 255, 255, backHovered ? 255 : 200).getRGB());
        } catch (Exception e) {
            drawCenteredString(fontRendererObj, "Back", backBtnX + backBtnW / 2, backBtnY + 5, backHovered ? 0xFFFFFF : 0xC8C8C8);
        }

        GlStateManager.disableBlend();
    }

    private void drawActionButtons(int mouseX, int mouseY, int centerX, int panelY) {
        int btnStartY = selectedType == AccountType.SSID ? 180 :
                selectedType == AccountType.MICROSOFT ? 140 :
                        selectedType == AccountType.COOKIE ? 165 : 165;
        int btnW = 55;
        int btnH = 14;
        int gap = 5;

        if (selectedType == AccountType.CRACKED) {
            drawTinyButton(centerX - btnW - gap/2, btnStartY, btnW, btnH, "Add", mouseX, mouseY, 4);
            drawTinyButton(centerX + gap/2, btnStartY, btnW, btnH, "Login", mouseX, mouseY, 5);
            drawTinyButton(centerX - 65, btnStartY + 19, 130, btnH, "Generate Random", mouseX, mouseY, 6);
        } else if (selectedType == AccountType.SSID) {
            drawTinyButton(centerX - btnW - gap/2, btnStartY, btnW, btnH, "Add", mouseX, mouseY, 4);
            drawTinyButton(centerX + gap/2, btnStartY, btnW, btnH, "Login", mouseX, mouseY, 5);
            drawTinyButton(centerX - 65, btnStartY + 19, 130, btnH, "Import Clipboard", mouseX, mouseY, 7);
        } else if (selectedType == AccountType.COOKIE) {
            drawTinyButton(centerX - 70, btnStartY, 140, btnH, "Select Cookie File", mouseX, mouseY, 4);
            drawTinyButton(centerX - 30, btnStartY + 19, btnW, btnH, "Login", mouseX, mouseY, 5);
        } else if (selectedType == AccountType.MICROSOFT) {
            drawTinyButton(centerX - 65, btnStartY, 130, btnH, "Browser Login", mouseX, mouseY, 4);
        }
    }

    private void drawTinyButton(int x, int y, int w, int h, String text, int mouseX, int mouseY, int animIndex) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        if (hovered && buttonHoverAnim[animIndex] < 1.0f) {
            buttonHoverAnim[animIndex] += 0.1f;
        } else if (!hovered && buttonHoverAnim[animIndex] > 0.0f) {
            buttonHoverAnim[animIndex] -= 0.1f;
        }
        buttonHoverAnim[animIndex] = Math.max(0f, Math.min(1f, buttonHoverAnim[animIndex]));

        int bgAlpha = (int)(140 + 40 * buttonHoverAnim[animIndex]);
        drawRect(x, y, x + w, y + h, new Color(30, 30, 35, bgAlpha).getRGB());

        if (buttonHoverAnim[animIndex] > 0) {
            int borderAlpha = (int)(60 * buttonHoverAnim[animIndex]);
            drawRect(x, y, x + w, y + 1, new Color(200, 200, 200, borderAlpha).getRGB());
            drawRect(x, y + h - 1, x + w, y + h, new Color(200, 200, 200, borderAlpha).getRGB());
        }

        Color textColor = new Color(255, 255, 255, hovered ? 255 : 200);
        try {
            float txtW = FontManager.SANS.getWidth(text);
            FontManager.SANS.drawString(text, x + w / 2f - txtW / 2f, y + h / 2f - 4, textColor.getRGB());
        } catch (Exception e) {
            drawCenteredString(fontRendererObj, text, x + w / 2, y + 3, textColor.getRGB());
        }
    }

    private void drawAccountList(int mouseX, int mouseY) {
        if (accounts.isEmpty()) return;

        int listX = this.width - 240;
        int listY = 80;
        int listW = 220;
        int listH = 250;

        drawRoundedRect(listX, listY, listW, listH, 10, new Color(40, 40, 45, 200));
        drawRoundedRect(listX - 2, listY - 2, listW + 4, listH + 4, 10, new Color(100, 100, 100, 80));
        drawRoundedRect(listX, listY, listW, listH, 10, new Color(40, 40, 45, 200));

        try {
            FontManager.MEDIUM_FR.drawStringWithShadow("Saved Accounts (" + accounts.size() + ")",
                    listX + 10, listY + 10, new Color(255, 255, 255).getRGB());
        } catch (Exception e) {
            drawString(fontRendererObj, "Saved Accounts (" + accounts.size() + ")", listX + 10, listY + 10, 0xFFFFFF);
        }

        int accountStartY = listY + 35;
        for (int i = scrollOffset; i < Math.min(scrollOffset + 7, accounts.size()); i++) {
            StoredAccount acc = accounts.get(i);
            int y = accountStartY + (i - scrollOffset) * 28;

            boolean hovering = mouseX >= listX + 5 && mouseX <= listX + listW - 5 && mouseY >= y && mouseY <= y + 24;

            if (hovering) {
                drawRoundedRect(listX + 5, y, listW - 10, 24, 6, new Color(80, 80, 85, 80));
            }

            String typeTag = acc.type == AccountType.MICROSOFT ? "[MS]" :
                    acc.type == AccountType.CRACKED ? "[C]" :
                            acc.type == AccountType.SSID ? "[S]" : "[?]";

            Color typeColor = acc.type == AccountType.MICROSOFT ? new Color(0, 200, 255) :
                    acc.type == AccountType.CRACKED ? new Color(100, 255, 100) :
                            new Color(255, 200, 0);

            try {
                FontManager.SMALL_FR.drawString(typeTag, listX + 10, y + 5, typeColor.getRGB());
                FontManager.SMALL_FR.drawString(acc.username, listX + 45, y + 5, hovering ? 0xFFFFFF : 0xCCCCCC);
                FontManager.SMALL_FR.drawString("[X]", listX + listW - 30, y + 5, 0xFF5555);
            } catch (Exception e) {
                drawString(fontRendererObj, typeTag + " " + acc.username, listX + 10, y + 5, hovering ? 0xFFFFFF : 0xCCCCCC);
                drawString(fontRendererObj, "[X]", listX + listW - 25, y + 5, 0xFF5555);
            }
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (usernameField != null) usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        if (selectedType == AccountType.SSID && ssidField != null) {
            ssidField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        int centerX = this.width / 2;
        int panelY = 30;

        // Type buttons
        int btnY = panelY + 50;
        int btnW = 55;
        int btnH = 14;
        int gap = 5;

        int totalWidth = btnW * 4 + gap * 3;
        int startX = centerX - totalWidth / 2;

        AccountType[] types = {AccountType.CRACKED, AccountType.MICROSOFT, AccountType.SSID, AccountType.COOKIE};
        for (int i = 0; i < types.length; i++) {
            int btnX = startX + i * (btnW + gap);
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                selectedType = types[i];
                statusMessage = "Mode: " + types[i].name();
                initGui();
                return;
            }
        }

        // Action buttons
        int btnStartY = selectedType == AccountType.SSID ? 180 :
                selectedType == AccountType.MICROSOFT ? 140 :
                        selectedType == AccountType.COOKIE ? 165 : 165;

        if (selectedType == AccountType.CRACKED) {
            if (isButtonClicked(centerX - btnW - gap/2, btnStartY, btnW, btnH, mouseX, mouseY)) addCrackedAccount();
            else if (isButtonClicked(centerX + gap/2, btnStartY, btnW, btnH, mouseX, mouseY)) loginCracked();
            else if (isButtonClicked(centerX - 65, btnStartY + 19, 130, btnH, mouseX, mouseY)) generateRandom();
        } else if (selectedType == AccountType.SSID) {
            if (isButtonClicked(centerX - btnW - gap/2, btnStartY, btnW, btnH, mouseX, mouseY)) addSSIDAccount();
            else if (isButtonClicked(centerX + gap/2, btnStartY, btnW, btnH, mouseX, mouseY)) loginWithSSID();
            else if (isButtonClicked(centerX - 65, btnStartY + 19, 130, btnH, mouseX, mouseY)) importFromClipboard();
        } else if (selectedType == AccountType.COOKIE) {
            if (isButtonClicked(centerX - 70, btnStartY, 140, btnH, mouseX, mouseY)) selectCookieFile();
            else if (isButtonClicked(centerX - 30, btnStartY + 19, btnW, btnH, mouseX, mouseY)) loginWithCookie();
        } else if (selectedType == AccountType.MICROSOFT) {
            if (isButtonClicked(centerX - 65, btnStartY, 130, btnH, mouseX, mouseY) && !waitingForMicrosoft) {
                startMicrosoftLogin();
            }
        }

        // Back button
        int backBtnX = this.width / 2 - 30;
        int backBtnY = panelY + 350 + 15;
        if (isButtonClicked(backBtnX, backBtnY, 60, 18, mouseX, mouseY)) {
            mc.displayGuiScreen(previousScreen);
            return;
        }

        // Account list clicks
        handleAccountListClick(mouseX, mouseY);
    }

    private boolean isButtonClicked(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private void handleAccountListClick(int mouseX, int mouseY) {
        if (accounts.isEmpty()) return;

        int listX = this.width - 240;
        int accountStartY = 115;

        for (int i = scrollOffset; i < Math.min(scrollOffset + 7, accounts.size()); i++) {
            int y = accountStartY + (i - scrollOffset) * 28;
            StoredAccount acc = accounts.get(i);

            // Delete button
            if (mouseX >= listX + 195 && mouseX <= listX + 220 && mouseY >= y && mouseY <= y + 24) {
                accounts.remove(i);
                statusMessage = "Removed: " + acc.username;
                if (scrollOffset > 0 && scrollOffset >= accounts.size() - 6) scrollOffset--;
                return;
            }

            // Login by clicking account
            if (mouseX >= listX + 40 && mouseX <= listX + 195 && mouseY >= y && mouseY <= y + 24) {
                loginStoredAccount(acc);
                return;
            }
        }
    }

    private void addCrackedAccount() {
        if (validateCrackedUsername(usernameField.getText())) {
            accounts.add(new StoredAccount(usernameField.getText(), AccountType.CRACKED));
            statusMessage = "Added: " + usernameField.getText();
            usernameField.setText("");
        } else {
            statusMessage = "Invalid username (3-16 chars, alphanumeric + _)";
        }
    }

    private void loginCracked() {
        if (validateCrackedUsername(usernameField.getText())) {
            SessionChanger.setSession(new Session(usernameField.getText(), "", "", "legacy"));
            statusMessage = "Logged in as: " + usernameField.getText();
        } else {
            statusMessage = "Invalid username";
        }
    }

    private void generateRandom() {
        String[] prefixes = {"clumsy", "clpz", "zep", "pastaa", "lil_lyko", "jake", "oliver", "pago", "unc", "scale"};
        String[] suffixes = {"gaming", "poo", "mayers", "malapda", "lover", "claudeai", "hater", "gaming", "kisser","pooper"};
        String randomName = prefixes[(int)(Math.random() * prefixes.length)] +
                suffixes[(int)(Math.random() * suffixes.length)] +
                (int)(Math.random() * 1000);
        usernameField.setText(randomName);
        statusMessage = "Generated: " + randomName;
    }

    private void addSSIDAccount() {
        if (!usernameField.getText().isEmpty() && !ssidField.getText().isEmpty()) {
            accounts.add(new StoredAccount(usernameField.getText(), AccountType.SSID, ssidField.getText()));
            statusMessage = "Added SSID account: " + usernameField.getText();
            usernameField.setText("");
            ssidField.setText("");
        } else {
            statusMessage = "Please enter both username and SSID";
        }
    }

    private void importFromClipboard() {
        try {
            java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            java.awt.datatransfer.Transferable contents = clipboard.getContents(null);

            if (contents != null && contents.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor)) {
                String clipboardText = (String) contents.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);

                if (clipboardText != null && !clipboardText.trim().isEmpty()) {
                    ssidField.setText(clipboardText.trim());
                    statusMessage = "Imported token from clipboard, logging in...";

                    // Automatically login after importing
                    loginWithSSID();
                } else {
                    statusMessage = "Clipboard is empty";
                }
            } else {
                statusMessage = "No text in clipboard";
            }
        } catch (Exception e) {
            statusMessage = "Failed to read clipboard";
            e.printStackTrace();
        }
    }

    private void loginWithSSID() {
        String token = ssidField.getText();
        if (token.isEmpty()) {
            statusMessage = "Please enter a session token";
            return;
        }

        new Thread(() -> {
            try {
                statusMessage = "Validating session...";
                String[] playerInfo = APIUtils.getProfileInfo(token);
                String username = playerInfo[0];
                String uuid = playerInfo[1];

                SessionChanger.setSession(new Session(username, uuid, token, "mojang"));
                statusMessage = "Logged in as " + username;
                usernameField.setText(username);
            } catch (Exception e) {
                statusMessage = "Invalid session token";
                e.printStackTrace();
            }
        }, "SSID-Login-Thread").start();
    }

    private void loginStoredAccount(StoredAccount acc) {
        if (acc.type == AccountType.CRACKED) {
            SessionChanger.setSession(new Session(acc.username, "", "", "legacy"));
            statusMessage = "Logged in as: " + acc.username;
        } else if (acc.type == AccountType.SSID) {
            new Thread(() -> {
                try {
                    statusMessage = "Logging in...";
                    String[] playerInfo = APIUtils.getProfileInfo(acc.ssidToken);
                    String username = playerInfo[0];
                    String uuid = playerInfo[1];

                    SessionChanger.setSession(new Session(username, uuid, acc.ssidToken, "mojang"));
                    statusMessage = "Logged in as " + username;
                } catch (Exception e) {
                    statusMessage = "Login failed - Token may be expired";
                    e.printStackTrace();
                }
            }, "SSID-Login-Thread").start();
        } else if (acc.type == AccountType.MICROSOFT && acc.microsoftAccount != null) {
            try {
                acc.microsoftAccount.login();
                statusMessage = "Logged in as: " + acc.username;
            } catch (Exception e) {
                statusMessage = "Login failed: Account may need refresh";
            }
        }
    }

    private void startMicrosoftLogin() {
        new Thread(() -> {
            try {
                waitingForMicrosoft = true;
                loginTimeout = System.currentTimeMillis() + (5 * 60 * 1000);
                statusMessage = "Starting browser login...";

                System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

                microsoftAccount = MicrosoftAccount.create();

                int maxAttempts = 600;
                int attempts = 0;

                while (attempts < maxAttempts && waitingForMicrosoft) {
                    if (microsoftAccount != null && microsoftAccount.isValid()) {
                        statusMessage = "Login successful! Username: " + microsoftAccount.getName();
                        waitingForMicrosoft = false;
                        return;
                    }
                    Thread.sleep(500);
                    attempts++;
                }

                if (attempts >= maxAttempts) {
                    statusMessage = "Login timeout - Please try again";
                    microsoftAccount = null;
                }

            } catch (Exception e) {
                handleMicrosoftError(e);
            } finally {
                waitingForMicrosoft = false;
            }
        }, "Microsoft-Login").start();
    }

    private void handleMicrosoftError(Exception e) {
        String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

        if (errorMsg.contains("aadsts50058") || errorMsg.contains("device")) {
            statusMessage = "Device restricted! Try again later";
        } else if (errorMsg.contains("aadsts50053")) {
            statusMessage = "Account locked. Unlock at account.live.com";
        } else if (errorMsg.contains("aadsts50076") || errorMsg.contains("mfa")) {
            statusMessage = "Complete 2FA/MFA in browser, then retry";
        } else if (errorMsg.contains("cancelled") || errorMsg.contains("cancel")) {
            statusMessage = "Login cancelled";
        } else if (errorMsg.contains("timeout")) {
            statusMessage = "Timeout - Please try again";
        } else {
            statusMessage = "Error: " + (errorMsg.length() > 50 ? errorMsg.substring(0, 50) + "..." : errorMsg);
        }

        e.printStackTrace();
        microsoftAccount = null;
    }

    private void selectCookieFile() {
        new Thread(() -> {
            try {
                FileDialog dialog = new FileDialog((Frame) null, "Select Cookie File");
                dialog.setMode(FileDialog.LOAD);
                dialog.setVisible(true);
                String file = dialog.getFile();
                String dir = dialog.getDirectory();

                if (file != null && dir != null) {
                    String path = dir + file;
                    java.util.Scanner scanner = new java.util.Scanner(new java.io.FileReader(path));
                    StringBuilder content = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        content.append(scanner.nextLine()).append("\n");
                    }
                    scanner.close();
                    cookieData = content.toString().split("\n");
                    statusMessage = "Cookie file loaded: " + file;
                    usernameField.setText(file);
                }
            } catch (Exception e) {
                statusMessage = "Error loading cookie file: " + e.getMessage();
                e.printStackTrace();
            }
        }, "Cookie-File-Selector").start();
    }

    private void loginWithCookie() {
        if (cookieData == null || cookieData.length == 0) {
            statusMessage = "No cookie file loaded";
            return;
        }

        new Thread(() -> {
            try {
                statusMessage = "Cookie login in progress...";

                // Parse cookies from the file
                StringBuilder cookies = new StringBuilder();
                ArrayList<String> processedCookies = new ArrayList<>();

                for (String cookie : cookieData) {
                    if (cookie.trim().isEmpty()) continue;

                    String[] parts = cookie.split("\t");
                    if (parts.length >= 7) {
                        String domain = parts[0];
                        String cookieName = parts[5];
                        String cookieValue = parts[6];

                        if (domain.endsWith("login.live.com") && !processedCookies.contains(cookieName)) {
                            cookies.append(cookieName).append("=").append(cookieValue).append("; ");
                            processedCookies.add(cookieName);
                        }
                    }
                }

                if (cookies.length() == 0) {
                    statusMessage = "Invalid cookie file format";
                    return;
                }

                // Remove trailing "; "
                String cookieString = cookies.substring(0, cookies.length() - 2);

                // Step 1: Initial request to get auth flow started
                javax.net.ssl.HttpsURLConnection connection = (javax.net.ssl.HttpsURLConnection)
                        new java.net.URL("https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin&aid=1142970254").openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                String location = connection.getHeaderField("Location");
                if (location != null) {
                    location = location.replace(" ", "%20");
                }

                // Step 2: Follow redirect with cookies
                connection = (javax.net.ssl.HttpsURLConnection) new java.net.URL(location).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
                connection.setRequestProperty("Cookie", cookieString);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                String location2 = connection.getHeaderField("Location");

                // Step 3: Final redirect to get access token
                connection = (javax.net.ssl.HttpsURLConnection) new java.net.URL(location2).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
                connection.setRequestProperty("Cookie", cookieString);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                String location3 = connection.getHeaderField("Location");

                if (location3 == null || !location3.contains("accessToken=")) {
                    statusMessage = "Cookie login failed - Invalid cookies";
                    return;
                }

                // Extract access token
                String accessToken = location3.split("accessToken=")[1];

                // Decode the access token
                String decoded = new String(java.util.Base64.getDecoder().decode(accessToken), java.nio.charset.StandardCharsets.UTF_8);
                String token = decoded.split("\"Token\":\"")[1].split("\"")[0];
                String uhs = decoded.split("\"DisplayClaims\":\\{\"xui\":\\[\\{\"uhs\":\"")[1].split("\"")[0];

                String xbl = "XBL3.0 x=" + uhs + ";" + token;

                // Login to Minecraft with Xbox Live token
                String mcTokenJson = "{\"identityToken\":\"" + xbl + "\",\"ensureLegacyEnabled\":true}";

                javax.net.ssl.HttpsURLConnection mcConnection = (javax.net.ssl.HttpsURLConnection)
                        new java.net.URL("https://api.minecraftservices.com/authentication/login_with_xbox").openConnection();
                mcConnection.setRequestMethod("POST");
                mcConnection.setRequestProperty("Content-Type", "application/json");
                mcConnection.setRequestProperty("Accept", "application/json");
                mcConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
                mcConnection.setDoOutput(true);

                java.io.OutputStream os = mcConnection.getOutputStream();
                os.write(mcTokenJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                os.close();

                java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(mcConnection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                // Parse MC access token
                String mcAccessToken = response.toString().split("\"access_token\":\"")[1].split("\"")[0];

                // Get profile info
                javax.net.ssl.HttpsURLConnection profileConnection = (javax.net.ssl.HttpsURLConnection)
                        new java.net.URL("https://api.minecraftservices.com/minecraft/profile").openConnection();
                profileConnection.setRequestMethod("GET");
                profileConnection.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
                profileConnection.setRequestProperty("User-Agent", "Mozilla/5.0");

                br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(profileConnection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                response = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                String profileData = response.toString();
                String username = profileData.split("\"name\":\"")[1].split("\"")[0];
                String uuid = profileData.split("\"id\":\"")[1].split("\"")[0];

                // Login with the account
                SessionChanger.setSession(new Session(username, uuid, mcAccessToken, "mojang"));
                statusMessage = "Logged in as " + username;

                // Optionally add to saved accounts
                accounts.add(new StoredAccount(username, AccountType.COOKIE));

            } catch (Exception e) {
                statusMessage = "Cookie login failed: " + e.getMessage();
                System.out.println("[COOKIE LOGIN] Error:");
                e.printStackTrace();
            }
        }, "Cookie-Login-Thread").start();
    }

    private boolean validateCrackedUsername(String name) {
        if (name == null || name.length() < 3 || name.length() > 16) return false;
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_') return false;
        }
        return true;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (usernameField != null && usernameField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        if (selectedType == AccountType.SSID && ssidField != null && ssidField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        if (keyCode == 1) { // ESC
            mc.displayGuiScreen(previousScreen);
        }
    }

    private enum AccountType {
        CRACKED, MICROSOFT, SSID, COOKIE
    }

    private static class StoredAccount {
        String username;
        AccountType type;
        MicrosoftAccount microsoftAccount;
        String ssidToken;

        StoredAccount(String username, AccountType type) {
            this.username = username;
            this.type = type;
            this.microsoftAccount = null;
            this.ssidToken = null;
        }

        StoredAccount(String username, AccountType type, String ssidToken) {
            this.username = username;
            this.type = type;
            this.ssidToken = ssidToken;
            this.microsoftAccount = null;
        }

        StoredAccount(String username, AccountType type, MicrosoftAccount account) {
            this.username = username;
            this.type = type;
            this.microsoftAccount = account;
            this.ssidToken = null;
        }
    }
}