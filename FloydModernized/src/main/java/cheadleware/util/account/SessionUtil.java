package cheadleware.util.account;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.lang.reflect.Field;

public class SessionUtil {
    private static Field sessionField;

    static {
        try {
            sessionField = Minecraft.class.getDeclaredField("session");
            sessionField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean setSession(Session session) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            sessionField.set(mc, session);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Session getSession() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            return (Session) sessionField.get(mc);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}