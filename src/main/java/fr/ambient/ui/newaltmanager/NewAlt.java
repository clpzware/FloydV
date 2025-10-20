package fr.ambient.ui.newaltmanager;

import com.google.gson.annotations.Expose;
import fr.ambient.util.mcauth.MicrosoftAuthenticator;
import fr.ambient.util.render.ColorUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.lwjglx.util.vector.Vector4f;

import java.awt.*;
import java.util.UUID;

import static fr.ambient.util.render.img.ImageManager.menuImages;

@Getter
@Setter
public class NewAlt {

    private GuiAltManager parent;
    private Session session;
    private final AltType type;
    private AccountStatus status = AccountStatus.UNKNOWN;
    private long unbanTime;
    private String refreshToken;

    private long lastClick = 0;

    private Animation animation = new Animation(Easing.EASE_IN_OUT_QUAD, 200);
    private Vector4f bounds = new Vector4f(0, 0, 410, 40);

    public NewAlt(GuiAltManager parent, Session session, AltType type, String refreshToken) {
        this.parent = parent;
        this.session = session;
        this.type = type;
        this.refreshToken = refreshToken;
    }

    public NewAlt(GuiAltManager parent, Session session, AltType type) {
        this.parent = parent;
        this.session = session;
        this.type = type;
        this.refreshToken = "";
    }

    public void render(int mouseX, int mouseY) {
        animation.run(isHovered(mouseX, mouseY) || parent.getSelectedAlt() == this ? 1 : 0);

        UUID playerId = session.getPlayerID() == null || session.getPlayerID().isEmpty() || session.getPlayerID().contains("Ambient") ?
                UUID.fromString("ec561538-f3fd-461d-aff5-086b22154bce") :
                session.getProfile().getId();

        RenderUtil.drawRoundedRect(bounds.x + 0.5f, bounds.y + 0.5f, bounds.z - 1f, bounds.w - 1f, 9.5f, new Color(0xA6121212, true));
        RenderUtil.drawRoundedOutline(bounds.x, bounds.y, bounds.z, bounds.w, 10, 1, ColorUtil.interpolateColorC(new Color(0xFF404047, true), new Color(0xFF484F9C, true), (float) animation.getValue()));

        RenderUtil.drawRoundHead(playerId, bounds.x + 7.5f, bounds.y + 7.5f, bounds.w - 15f, 3);
        menuImages.get(type.getIcon()).drawImg(bounds.x + 36, bounds.y + 9, 9, 9, new Color(0xFF5A5B6D));

        Fonts.getNunito(19).drawString(session.getUsername(), bounds.x + 49f, bounds.y + 9.5f, -1);

        long timeLeft = unbanTime - System.currentTimeMillis();

        long days = timeLeft / (1000 * 60 * 60 * 24);
        long hours = (timeLeft / (1000 * 60 * 60)) % 24;
        long minutes = (timeLeft / (1000 * 60)) % 60;
        String banTime = days + "d " + hours + "h " + minutes + "m";
        if (unbanTime == -1L) banTime = "Permanent";
        Fonts.getNunito(13).drawString(status.getMessage(banTime), bounds.x + 37, bounds.y + 24f, 0xFF55565C);
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            parent.setSelectedAlt(this);

            if (System.currentTimeMillis() - lastClick <= 200) {
                attemptLogin();
                parent.setSelectedAlt(null);
            }

            lastClick = System.currentTimeMillis();
        }
    }

    public void attemptLogin() {
        if (refreshToken.isEmpty()) {
            Minecraft.getMinecraft().setSession(this.session);
        } else {
            parent.setNotification(new AltNotification("Attempting Login", "Logging into ms account...", 1800, NotificationStatus.INFO));
            Thread thread = new Thread(() -> {
                refreshAndLogin();
                System.out.println("-> Succesfully logged into Microsoft Account.");
            });
            thread.start();
        }
    }

    public void refreshAndLogin() {
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator("526b3e37-6aa9-45ef-989f-ed84bfb47f18", "aY78Q~1zman1vukdI.ZzirYvGsWkxY0pjBOLFcEB");
        authenticator.getToken(refreshToken);
    }

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX > bounds.x && mouseX < bounds.x + bounds.z && mouseY > bounds.y && mouseY <= bounds.y + bounds.w;
    }
}