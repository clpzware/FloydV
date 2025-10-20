package fr.ambient.notification.impl;

import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.font.Fonts;
import lombok.Getter;

import java.awt.*;

import static fr.ambient.util.InstanceAccess.inGameImages;

public class Notification {

    @Getter
    private final String message, subString;
    private final NotificationType type;
    private final NotificationMode mode;
    public float width;
    public float height;
    public final Animation popIn = new Animation(Easing.EASE_IN_OUT_SINE, 250);
    private final TimeUtil time = new TimeUtil();

    public Notification(String message, String subString, NotificationType type, NotificationMode mode) {
        this.mode = mode;
        this.message = message;
        this.subString = subString;
        this.type = type;
        setSize(mode);
        time.reset();
    }

    private void setSize(NotificationMode mode){
        switch (mode){
            case MODERN:
                this.width = 125;
                this.height = 32;
        }
    }

    public final void draw(final float x, final float y){
        popIn.run(time.getTime()<250 || time.getTime()<1725 ? 1 : 0);

        switch (mode) {
            case MODERN:
                float modernTitleWidth = Fonts.getNunito(19).getWidth(message);
                float modernSubWidth = Fonts.getNunito(16).getWidth(subString);
                this.width = modernTitleWidth > modernSubWidth ? modernTitleWidth + 50 : modernSubWidth + 50;

                float newXModern = (float) (x - width * popIn.getValue() - 5);
                float newYModern = (float) (y -25 * popIn.getValue());

                RenderUtil.drawRoundedRect(newXModern, newYModern, width, height, 7, new Color(0x90121214, true));
                RenderUtil.drawRoundedRect(newXModern + 3, newYModern + 3, height - 6, height - 6, 4, new Color(0x65000000, true));
                inGameImages.get("info").drawImg(newXModern + 6, newYModern + 6, height - 12, height - 12, new Color(150, 150, 150));
                Fonts.getNunito(19).drawStringWithShadow(message, newXModern + 32, newYModern + 5, 0xFFFFFFFF);
                Fonts.getNunito(16).drawString(subString , newXModern + 32, newYModern + 18, new Color(150, 150, 150).getRGB());
                break;
        }
    }

    public boolean shouldDelete(){
        return time.finished(2000);
    }
}
