package fr.ambient.util;

import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.protection.backend.api.HWID;
import fr.ambient.util.player.ChatUtil;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.lwjglx.Sys;

import java.net.URISyntaxException;

@UtilityClass
public class Reconnect {
    @SneakyThrows
    public void reco(){
        ChatUtil.display("Trying to reconnect to backend... You have about 60 seconds.");

        System.out.println("ERM BACKEND CONNECT INITIALIZE");

        boolean done = true;
        Ambient.getInstance().setTryingToReconnect(true);


        JsonObject sent = new JsonObject();

        sent.addProperty("id", "login");
        sent.addProperty("uid", Ambient.getInstance().getUid());
        sent.addProperty("hwid", HWID.getHWID());
        sent.addProperty("clientid", "ambient");
        sent.addProperty("clientVersion", "Release");

        while (Ambient.getInstance().getToken() == null) {
            Thread.sleep(50);
        }


        JsonObject object1 = new JsonObject();

        object1.addProperty("id", "userinfo");
        object1.addProperty("token", Ambient.getInstance().getToken());

        Ambient.getInstance().getMsSinceLast().reset();
        Ambient.getInstance().setTryingToReconnect(false);

        ChatUtil.display("Reconnection Successful");

    }
}
