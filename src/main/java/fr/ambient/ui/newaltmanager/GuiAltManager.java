package fr.ambient.ui.newaltmanager;

import fr.ambient.Ambient;
import fr.ambient.util.mcauth.MicrosoftAuthenticator;
import fr.ambient.util.mcauth.cookie.CookieLogin;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Session;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;
import org.lwjglx.util.vector.Vector4f;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static fr.ambient.util.render.img.ImageManager.menuImages;

public class GuiAltManager extends GuiScreen {

    private final GuiScreen parent;

    @Getter @Setter
    private NewAlt selectedAlt;
    private ArrayList<AltManagerButton> buttons = new ArrayList<>();

    public final MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator(this, "526b3e37-6aa9-45ef-989f-ed84bfb47f18", "aY78Q~1zman1vukdI.ZzirYvGsWkxY0pjBOLFcEB");

    @Getter
    @Setter
    private AltNotification notification = null;
    private AltNotification lastNotification = null;

    private GLFWDropCallback dropCallback;
    private long windowHandle;

    private GuiAltPopup altPopup;

    private float scrollOffset = 0;
    private float targetScrollOffset = 0;

    @SneakyThrows
    public GuiAltManager(GuiScreen parent) {
        this.parent = parent;

        loadAlts();

        setupFileDrop();
        this.altPopup = new GuiAltPopup(this, GuiAltPopup.ActionType.ADD);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        buttons.add(new AltManagerButton(this, "Add Account", new Vector4f(sr.getScaledWidth() / 2f - 205, sr.getScaledHeight() - 76, 200, 28), () -> {
            altPopup = new GuiAltPopup(this, GuiAltPopup.ActionType.ADD);
            altPopup.open();
        }));

        buttons.add(new AltManagerButton(this, "Direct Login", new Vector4f(sr.getScaledWidth() / 2f + 5, sr.getScaledHeight() - 76, 200, 28), () -> {
            altPopup = new GuiAltPopup(this, GuiAltPopup.ActionType.DIRECT_LOGIN);
            altPopup.open();
        }));

        buttons.add(new AltManagerButton(this, "Remove Banned", new Vector4f(sr.getScaledWidth() / 2f - 205, sr.getScaledHeight() - 38, 130, 28), () -> {
            Ambient.getInstance().getAltsComponent().removeBannedAlts();
        }));

        buttons.add(new AltManagerButton(this, "Login", new Vector4f(sr.getScaledWidth() / 2f - 65, sr.getScaledHeight() - 38, 130, 28), () -> {
            if (this.selectedAlt != null)
                selectedAlt.attemptLogin();
        }));

        buttons.add(new AltManagerButton(this, "Remove", new Vector4f(sr.getScaledWidth() / 2f + 75, sr.getScaledHeight() - 38, 130, 28), () -> {
            if (this.selectedAlt != null) {
                List<NewAlt> alts = Ambient.getInstance().getAltsComponent().getAlts();
                NewAlt selected = selectedAlt;
                if (selected != alts.getLast()) selectedAlt = alts.get(alts.indexOf(selected) + 1);
                else selectedAlt = null;
                Ambient.getInstance().getAltsComponent().removeAlt(selected);
            }
        }));
    }

    @Override
    public void initGui() {
        setupFileDrop();
        super.initGui();
    }

    @Override
    public void onGuiClosed() {
        removeFileDropCallback();
        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if (sr.getScaledWidth() <= 0 || sr.getScaledHeight() <= 0 || mc.displayHeight <= 0 || mc.displayWidth <= 0) return;

        List<NewAlt> alts = Ambient.getInstance().getAltsComponent().getAlts();

        float lerpFactor = 0.05f;
        scrollOffset += (targetScrollOffset - scrollOffset) * lerpFactor;

        float available = sr.getScaledHeight() - 136;
        float altSize = alts.size() * 50;
        float needed = altSize - available;
        scrollOffset =  Math.max(Math.min(scrollOffset, needed), 0);

        if (scrollOffset <= 0) {
            scrollOffset = 0;
            targetScrollOffset = 0;
        } else if (scrollOffset >= needed) {
            scrollOffset = needed;
            targetScrollOffset = needed;
        } else {
            scrollOffset = Math.max(Math.min(scrollOffset, needed), 0);
        }

        buttons.get(0).setBounds(new Vector4f(sr.getScaledWidth() / 2f - 205, sr.getScaledHeight() - 76, 200, 28));
        buttons.get(1).setBounds(new Vector4f(sr.getScaledWidth() / 2f + 5, sr.getScaledHeight() - 76, 200, 28));
        buttons.get(2).setBounds(new Vector4f(sr.getScaledWidth() / 2f - 205, sr.getScaledHeight() - 38, 130, 28));
        buttons.get(3).setBounds(new Vector4f(sr.getScaledWidth() / 2f - 65, sr.getScaledHeight() - 38, 130, 28));
        buttons.get(4).setBounds(new Vector4f(sr.getScaledWidth() / 2f + 75, sr.getScaledHeight() - 38, 130, 28));

        UUID playerId = mc.getSession().getPlayerID() == null || mc.getSession().getPlayerID().isEmpty() || mc.getSession().getPlayerID().contains("Ambient") ?
                UUID.fromString("ec561538-f3fd-461d-aff5-086b22154bce") :
                mc.getSession().getProfile().getId();

        menuImages.get("background").drawImg(0, 0, sr.getScaledWidth(), sr.getScaledHeight());

        // top bar
        RenderUtil.drawRect(0, 0, sr.getScaledWidth(), 40, new Color(0x66121212, true));
        RenderUtil.drawLine(0, 40, sr.getScaledWidth(), 0, 1, new Color(0x26FFFFFF, true));

        RenderUtil.drawRoundHead(playerId, 7, 7, 26, 3);
        Fonts.getNunito(21).drawString(mc.getSession().getUsername(), 37, 10, -1);
        Fonts.getNunito(15).drawString(mc.getSession().getPlayerID().isEmpty() ? "Cracked Account" : mc.getSession().getPlayerID(), 37, 23, 0xFF737373);

        // bottom bar
        RenderUtil.drawRect(0, sr.getScaledHeight() - 86, sr.getScaledWidth(), 86, new Color(0x66121212, true));
        RenderUtil.drawLine(0, sr.getScaledHeight() - 86, sr.getScaledWidth(), 0, 1, new Color(0x26FFFFFF, true));

        Fonts.getNunito(28).drawCenteredString("Alt Manager", sr.getScaledWidth() / 2f, 6, -1);
        Fonts.getNunito(15).drawCenteredString(alts.size() + " Account(s)", sr.getScaledWidth() / 2f, 25, 0xFF737373);

        buttons.forEach(button -> {
            button.render(mouseX, mouseY);
        });

        RenderUtil.renderScissor(() -> {
            float y = 50 - scrollOffset;
            List<NewAlt> altsCopy = new ArrayList<>(alts);
            for (NewAlt alt : altsCopy) {
                alt.setBounds(new Vector4f(sr.getScaledWidth() / 2f - alt.getBounds().z / 2f, y, alt.getBounds().z, alt.getBounds().w));
                alt.render(mouseX, mouseY);
                y += alt.getBounds().w + 10;
            }
        }, 0, 40, sr.getScaledWidth(), sr.getScaledHeight() - 86);

        altPopup.draw(mouseX, mouseY);

        if (notification != null) {
            if (notification.hasExpired()) {
                lastNotification = notification;
                notification = null;
            } else {
                notification.popIn.run(1);
                notification.render(mouseX, mouseY, partialTicks);
            }
        }
        if (lastNotification != null) {
            lastNotification.popIn.run(0);
            lastNotification.render(mouseX, mouseY, partialTicks);

            if (lastNotification.popIn.getValue() == 0) {
                lastNotification = null;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if (sr.getScaledWidth() <= 0 || sr.getScaledHeight() <= 0 || mc.displayHeight <= 0 || mc.displayWidth <= 0) return;

        int dWheel = Mouse.getEventDWheel();

        if (dWheel != 0) {
            float scrollSpeed = 25;
            targetScrollOffset -= (float) ((dWheel / 120.0) * scrollSpeed);

            if (targetScrollOffset < 0) targetScrollOffset = 0;
        }

        super.handleMouseInput();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if (sr.getScaledWidth() <= 0 || sr.getScaledHeight() <= 0 || mc.displayHeight <= 0 || mc.displayWidth <= 0) return;

        if (altPopup.isOpen()) {
            altPopup.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        buttons.forEach(b -> b.mouseClicked(mouseX, mouseY, mouseButton));
        List<NewAlt> alts = Ambient.getInstance().getAltsComponent().getAlts();
        alts.forEach(a -> a.mouseClicked(mouseX, mouseY, mouseButton));
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void setupFileDrop() {
        windowHandle = Display.getHandle();

        dropCallback = new GLFWDropCallback() {
            @Override
            public void invoke(long window, int count, long names) {
                List<String> filePaths = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    long namePointer = MemoryUtil.memGetAddress(names + (long) i * Pointer.POINTER_SIZE);
                    String filePath = MemoryUtil.memUTF8(namePointer);
                    filePaths.add(filePath);
                }

                for (String path : filePaths) {
                    File file = new File(path);

                    if (file.exists() && file.isFile() && path.toLowerCase().endsWith(".txt")) {
                        loginWithCookie(file,filePaths.getFirst().equals(path));
                    }
                }
            }
        };

        GLFW.glfwSetDropCallback(windowHandle, dropCallback);
    }

    private void loginWithCookie(File file, boolean login) {
        if (file.isFile() && !file.isDirectory()) {
            try {
                final CookieLogin.LoginData loginData = CookieLogin.loginWithCookie(file);

                if (loginData != null) {
                    Session session = new Session(loginData.username(), loginData.uuid(), loginData.mcToken(), "legacy");
                    if (login)
                        mc.setSession(session);

                    addAlt(new NewAlt(this, session, AltType.COOKIE));
                    setNotification(new AltNotification("Logged in!", "Logged into " + session.getUsername(), 1800, NotificationStatus.SUCCESS));
                } else {
                    setNotification(new AltNotification("Failed to login", "Your cookie may be invalid", 1800, NotificationStatus.FAILURE));
                    System.err.println("Failed to login: Could not retrieve login data.");
                }
            } catch (Exception exception) {
                System.err.println("Failed to login: Exception during login.");
                exception.printStackTrace(System.err);
            }
        }
    }

    private void removeFileDropCallback() {
        if (dropCallback != null) {
            dropCallback.free();
            GLFW.glfwSetDropCallback(windowHandle, null);
            dropCallback = null;
        }
    }

    public void addAlt(NewAlt alt) {
        Ambient.getInstance().getAltsComponent().addAlt(alt);
    }

    public void saveAlts() {
        Ambient.getInstance().getAltsComponent().saveAlts();
    }

    public void loadAlts() {
        Ambient.getInstance().getAltsComponent().loadAlts(this);
    }
}