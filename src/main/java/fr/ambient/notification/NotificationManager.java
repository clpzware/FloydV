package fr.ambient.notification;

import fr.ambient.Ambient;
import fr.ambient.notification.impl.Notification;
import fr.ambient.notification.impl.NotificationMode;
import fr.ambient.notification.impl.NotificationType;
import fr.ambient.util.InstanceAccess;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;

public class NotificationManager implements InstanceAccess {
    public static final ArrayList<Notification> notifications = new ArrayList<>();
    private static String notifMode = "Dog";
    public static void update(String mode){
        notifMode = mode;
        draw();
    }

    private static void draw(){
        ScaledResolution sr = new ScaledResolution(mc);

        float offset = 0;
        for (int i = notifications.size() - 1; i >= 0; i--){
            Notification notification = notifications.get(i);
            Ambient.getInstance().getEventBus().register(notification);
            notification.draw(sr.getScaledWidth(), sr.getScaledHeight() - notification.height + offset);


            offset-= (float) ((notification.height + 5)*notification.popIn.getValue());
            if (notification.shouldDelete()){
                Ambient.getInstance().getEventBus().unregister(notification);
                notifications.remove(i);
            }
        }
    }

    public static void addNotification(String message, String subString, NotificationType type) {
        for (Notification notification : notifications) {
            if (notification.getSubString().equals(subString))
                return;
        }
        switch (notifMode){
            case "Modern":
                notifications.add(new Notification(message, subString, type, NotificationMode.MODERN));
                break;
        }
    }
}
