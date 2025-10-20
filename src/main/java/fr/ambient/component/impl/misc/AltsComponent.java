package fr.ambient.component.impl.misc;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import fr.ambient.component.Component;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.DisconnectEvent;
import fr.ambient.event.impl.world.WorldLoadEvent;
import fr.ambient.ui.newaltmanager.AccountStatus;
import fr.ambient.ui.newaltmanager.AltType;
import fr.ambient.ui.newaltmanager.GuiAltManager;
import fr.ambient.ui.newaltmanager.NewAlt;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Session;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AltsComponent extends Component {

    private final File file = new File(Minecraft.getMinecraft().mcDataDir, "ambient/newalts.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private ArrayList<NewAlt> alts = new ArrayList<>();

    @SubscribeEvent
    public void onWorldLoad(WorldLoadEvent e) {
        ServerData serverData = mc.getCurrentServerData();
        if (serverData != null) {
            String ip = serverData.serverIP;
            if (ip.endsWith("hypixel.net") || ip.endsWith("hypixel.io") || ip.endsWith("technoblade.club") || ip.endsWith("ilovecatgirls.xyz")) {
                alts.forEach(a -> {
                    if (mc.getSession().getPlayerID().equals(a.getSession().getPlayerID())) {
                        a.setStatus(AccountStatus.UNBANNED);
                        a.setUnbanTime(0L);
                        saveAlts();
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onInitGUI(DisconnectEvent e) {
        if (e.getScreen() instanceof GuiDisconnected gui) {
            IChatComponent message = gui.message;
            String text = message.getFormattedText().split("\n\n")[0];

            System.out.println(text);

            if (text.equals("§r§cYou are permanently banned from this server!") || text.equals("§r§cYour account has been blocked.")) {
                alts.forEach(account -> {
                    if (mc.getSession().getPlayerID().equals(account.getSession().getPlayerID())) {
                        account.setUnbanTime(-1L);
                        account.setStatus(AccountStatus.BANNED);
                    }
                });
                saveAlts();
            }

            if (
                    text.matches("§r§cYou are temporarily banned for §r§f.*§r§c from this server!") ||
                            text.matches("§r§cYour account is temporarily blocked for §r§f.*§r§c from this server!")
            ) {
                String unban = StringUtils.substringBetween(text, "§r§f", "§r§c");
                if (unban != null) {
                    long time = System.currentTimeMillis();
                    for (String duration : unban.split(" ")) {
                        String type = duration.substring(duration.length() - 1);
                        long value = Long.parseLong(duration.substring(0, duration.length() - 1));
                        switch (type) {
                            case "d": {
                                time += value * 86400000L;
                            }
                            break;
                            case "h": {
                                time += value * 3600000L;
                            }
                            break;
                            case "m": {
                                time += value * 60000L;
                            }
                            break;
                            case "s": {
                                time += value * 1000L;
                            }
                            break;
                        }
                    }

                    long finalTime = time;
                    alts.forEach(account -> {
                        if (mc.getSession().getPlayerID().equals(account.getSession().getPlayerID())) {
                            account.setUnbanTime(finalTime);
                            account.setStatus(AccountStatus.BANNED);
                        }
                    });

                    saveAlts();
                }
            }
        }
    }

    public void saveAlts() {
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        JsonArray jsonArray = new JsonArray();

        for (NewAlt alt : alts) {
            Session session = alt.getSession();

            JsonObject sessionObject = new JsonObject();
            sessionObject.addProperty("username", session.getUsername());
            sessionObject.addProperty("playerID", session.getPlayerID());
            sessionObject.addProperty("token", session.getToken());
            sessionObject.addProperty("sessionType", session.getSessionType().toString());

            JsonObject altObject = new JsonObject();
            altObject.add("session", sessionObject);
            altObject.addProperty("type", alt.getType().toString());
            altObject.addProperty("status", alt.getStatus().toString());
            altObject.addProperty("unbanTime", alt.getUnbanTime());
            altObject.addProperty("refreshToken", alt.getRefreshToken() != null ? alt.getRefreshToken() : "");

            jsonArray.add(altObject);
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(jsonArray));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAlts(GuiAltManager guiAltManager) {
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonParser parser = new JsonParser();
            JsonArray jsonArray = parser.parse(reader).getAsJsonArray();

            alts = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                JsonObject altObject = element.getAsJsonObject();

                JsonObject sessionObject = altObject.getAsJsonObject("session");

                String username = sessionObject.get("username").getAsString();
                String playerID = sessionObject.get("playerID").getAsString();
                String accessToken = sessionObject.get("token").getAsString();
                String sessionType = sessionObject.get("sessionType").getAsString();

                Session session = new Session(username, playerID, accessToken, sessionType);

                AltType altType = AltType.valueOf(altObject.get("type").getAsString());
                String refreshToken = altObject.get("refreshToken").getAsString();

                NewAlt alt = new NewAlt(guiAltManager, session, altType, refreshToken);

                alt.setStatus(AccountStatus.valueOf(altObject.get("status").getAsString()));
                alt.setUnbanTime(altObject.get("unbanTime").getAsLong());

                alts.add(alt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAlt(NewAlt alt) {
        alts.removeIf(a -> a.getType() != AltType.CRACKED && a.getSession().getPlayerID().equals(alt.getSession().getPlayerID()));
        alts.add(alt);
        saveAlts();
    }

    public void removeAlt(NewAlt alt) {
        alts.remove(alt);
        saveAlts();
    }

    public void removeBannedAlts() {
        boolean removed = this.alts.removeIf(alt -> alt.getStatus().equals(AccountStatus.BANNED));

        if (removed) {
            saveAlts();
        }
    }
}