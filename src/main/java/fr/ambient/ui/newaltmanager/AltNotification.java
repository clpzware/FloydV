package fr.ambient.ui.newaltmanager;

import fr.ambient.util.render.GlowUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

import static fr.ambient.util.InstanceAccess.inGameImages;
import static fr.ambient.util.InstanceAccess.mc;

public class AltNotification {

    @Getter
    private final String message, title;
    @Getter
    private final NotificationStatus status;
    private final long duration, initialTime;

    public final Animation popIn = new Animation(Easing.EASE_OUT_BACK, 200);

    public AltNotification(String title, String message, long duration, NotificationStatus status) {
        this.title = title;
        this.message = message;
        this.duration = duration;
        this.initialTime = System.currentTimeMillis();
        this.status = status;
    }

    public boolean hasExpired() {
        return duration >= 0 && duration < System.currentTimeMillis() - initialTime;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        float modernTitleWidth = Fonts.getNunito(19).getWidth(message);
        float modernSubWidth = Fonts.getNunito(16).getWidth(status.name());
        float width = modernTitleWidth > modernSubWidth ? modernTitleWidth + 50 : modernSubWidth + 50;
        float height = 32;

        ScaledResolution sr = new ScaledResolution(mc);

        float newXModern = sr.getScaledWidth() - width - 5;
        float newYModern = sr.getScaledHeight() - height - 25;

        RenderUtil.scale(() -> {
            RenderUtil.drawRoundedRect(newXModern, newYModern, width, height, 7, new Color(0x90121214, true));

            RenderUtil.drawRoundedRect(newXModern + 3, newYModern + 3, height - 6, height - 6, 4, new Color(0x65000000, true));
            inGameImages.get("info").drawImg(newXModern + 6, newYModern + 6, height - 12, height - 12, new Color(150, 150, 150));
            Fonts.getNunito(19).drawStringWithShadow(title, newXModern + 32, newYModern + 5, 0xFFFFFFFF);
            Fonts.getNunito(16).drawString(message, newXModern + 32, newYModern + 18, new Color(150, 150, 150).getRGB());
        }, newXModern + width / 2f, newYModern + height / 2f, popIn.getValue());
    }
}
