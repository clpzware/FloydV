package fr.ambient.module.impl.misc;

import com.google.gson.JsonObject;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class NoDisconnect extends Module {
    public NoDisconnect() {
        super(9,"Prevent People From Stealing your alts", ModuleCategory.MISC);
    }

    @SubscribeEvent
    public void onTickEvent(UpdateEvent event){
        if (mc.getCurrentServerData() == null) return;


        if(mc.thePlayer.ticksExisted > 20 ){
            Runnable tsk = ()->{
                HttpURLConnection connection = getUrlConnection();
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            };
            CompletableFuture.runAsync(tsk)
                    .exceptionally(ex -> {
                        return null;
                    });


        }
    }

    @NotNull
    private static HttpURLConnection getUrlConnection() {
        JsonObject object = new JsonObject();
        object.addProperty("accessToken", mc.getSession().getToken());
        object.addProperty("selectedProfile", mc.getSession().getPlayerID());
        object.addProperty("serverId", "31531515");
        HttpURLConnection connection = getHttpURLConnection();
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = object.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    @NotNull
    private static HttpURLConnection getHttpURLConnection() {
        URL url = null;
        try {
            url = new URL("https://sessionserver.mojang.com/session/minecraft/join");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
        connection.setRequestProperty("Content-type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        return connection;
    }

}
