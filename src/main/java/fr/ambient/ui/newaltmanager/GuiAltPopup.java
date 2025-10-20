package fr.ambient.ui.newaltmanager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ambient.component.impl.misc.AltsComponent;
import fr.ambient.util.Names;
import fr.ambient.util.mcauth.cookie.CookieLogin;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import lombok.SneakyThrows;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Session;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.lwjglx.openal.AL;
import org.lwjglx.util.vector.Vector4f;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

import static fr.ambient.util.InstanceAccess.mc;

public class GuiAltPopup {
    private final GuiAltManager parent;
    private boolean closing = false;
    private boolean isOpen = false;
    private final ActionType actionType;
    private final ArrayList<AltManagerButton> buttons = new ArrayList<>();

    public enum ActionType {
        ADD, DIRECT_LOGIN
    }

    public GuiAltPopup(GuiAltManager parent, ActionType actionType) {
        this.parent = parent;
        this.actionType = actionType;
    }

    public void open() {
        isOpen = true;
        closing = false;
        initGui();
    }

    public void close() {
        closing = true;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void initGui() {
        ScaledResolution sr = new ScaledResolution(mc);
        float centerX = sr.getScaledWidth() / 2f;
        float centerY = sr.getScaledHeight() / 2f - 25;

        buttons.clear();
        buttons.add(new AltManagerButton(parent, "Cracked", new Vector4f(centerX - 90, centerY - 50, 180, 30), () -> {
            try {
                Random random = new Random();

                String baseName = Names.names.get(random.nextInt(Names.names.size()));

                int randomNumber = 1000 + random.nextInt(9000);

                String generatedString = baseName + randomNumber;

                Session cracked = new Session(generatedString, "", "", "legacy");
                mc.setSession(cracked);

                if (actionType.equals(ActionType.ADD)) {
                    parent.addAlt(new NewAlt(parent, cracked, AltType.CRACKED));
                }

                parent.setNotification(new AltNotification("Successful Login", "Added new cracked alt!", 1800, NotificationStatus.SUCCESS));
            } catch (Exception e) {

            }
            close();
        }));

        buttons.add(new AltManagerButton(parent, "Microsoft", new Vector4f(centerX - 90, centerY - 15, 180, 30), () -> {
            parent.setNotification(new AltNotification("Opened browser", "Please login to your xbox account", 1800, NotificationStatus.INFO));
            Thread thread = new Thread(parent.authenticator::login);
            thread.start();
            close();
        }, () -> {
            parent.setNotification(new AltNotification("Copied link", "MS login link has been copied to clipboard!", 1800, NotificationStatus.INFO));
            Thread thread = new Thread(parent.authenticator::loginWithoutBrowseAndCopyIntoiClip);
            thread.start();
            close();
        }));

        buttons.add(new AltManagerButton(parent, "Cookie", new Vector4f(centerX - 90, centerY + 20, 180, 30), () -> {
            try {
                new Thread(() -> {
                    parent.setNotification(new AltNotification("Choose Cookie", "Opened an explorer window", 1800, NotificationStatus.INFO));

                    final String directory = Paths.get(System.getProperty("user.home"), "Downloads").toString();

                    final FileDialog fileChooser = new FileDialog((Frame) null, "Select Cookie File", FileDialog.LOAD);
                    fileChooser.setFilenameFilter((ignored, name) -> name.endsWith(".txt"));
                    fileChooser.setDirectory(directory);

                    fileChooser.setVisible(true);
                    fileChooser.setAlwaysOnTop(true);

                    final String fileDirectory = fileChooser.getDirectory();
                    final String fileName = fileChooser.getFile();

                    if (fileDirectory != null && fileName != null) {
                        final File selectedFile = new File(fileDirectory, fileName);

                        if (selectedFile.isFile() && !selectedFile.isDirectory()) {
                            try {
                                final CookieLogin.LoginData loginData = CookieLogin.loginWithCookie(selectedFile);

                                if (loginData != null) {
                                    Session session = new Session(loginData.username(), loginData.uuid(), loginData.mcToken(), "legacy");
                                    mc.setSession(session);
                                    if (actionType.equals(ActionType.ADD))
                                        parent.addAlt(new NewAlt(parent, session, AltType.COOKIE));

                                    parent.setNotification(new AltNotification("Logged in!", "Logged into " + session.getUsername(), 1800, NotificationStatus.SUCCESS));
                                } else {
                                    parent.setNotification(new AltNotification("Failed to login", "Your cookie may be invalid", 1800, NotificationStatus.FAILURE));
                                    System.err.println("Failed to login: Could not retrieve login data.");
                                }
                            } catch (Exception exception) {
                                System.err.println("Failed to login: Exception during login.");
                                exception.printStackTrace(System.err);
                            }
                        }
                    } else {
                        System.out.println("No file selected!");
                    }
                }).start();
            } catch (Exception exception) {
                System.err.println("Failed to login: Thread death.");
                exception.printStackTrace(System.err);
            }
            close();
        }));

        buttons.add(new AltManagerButton(parent, "Dog", new Vector4f(centerX - 90, centerY + 55, 180, 30), () -> {
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Clipboard clipboard = toolkit.getSystemClipboard();
                String result = null;
                try {
                    result = (String) clipboard.getData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException | IOException e) {
                    throw new RuntimeException(e);
                }
                String dc;

                try {
                    dc = getDecrypt(result.replace(" ", "").split("\\|")[0], false);
                } catch (Exception e) {
                    dc = getDecrypt(result.replace(" ", "").split("\\|")[0]);
                }

                String name = getName(dc.split(":")[0]);
                Session dogAlt = new Session(name, dc.split(":")[0], dc.split(":")[1], "legacy");
                mc.setSession(dogAlt);
                if (actionType.equals(ActionType.ADD)) {
                    parent.addAlt(new NewAlt(parent, dogAlt, AltType.DOG));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            close();
        }));


        buttons.add(new AltManagerButton(parent, "Session", new Vector4f(centerX - 90, centerY + 55 + 35, 180, 30), () -> {
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Clipboard clipboard = toolkit.getSystemClipboard();
                String result = null;
                try {
                    result = (String) clipboard.getData(DataFlavor.stringFlavor);

                } catch (UnsupportedFlavorException | IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    String[] t = getProfileInfo(result);
                    Session dogAlt = new Session(t[0], t[1], result, "legacy");
                    mc.setSession(dogAlt);
                    if (actionType.equals(ActionType.ADD)) {
                        parent.addAlt(new NewAlt(parent, dogAlt, AltType.SESSION));
                    }
                }catch (Exception e){
                    parent.setNotification(new AltNotification("Failed to login", "Invalid Session", 1800, NotificationStatus.FAILURE));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            close();
        }));

        buttons.add(new AltManagerButton(parent, "Cancel", new Vector4f(centerX - 90, centerY + 95 + 35, 180, 30), this::close));
    }

    @SneakyThrows
    public static String getName(String uuid) {
        String name;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).openStream()));
            name = (((JsonObject) new JsonParser().parse(in)).get("name")).toString().replaceAll("\"", "");
            in.close();
        } catch (Exception e) {
            System.out.println("Unable to get Name of: " + uuid + "!");
            name = "None - Issue";
        }
        return name;
    }

    @SneakyThrows
    public static String getDecrypt(String bas) {
        SecretKeySpec key = new SecretKeySpec("LiticaneFurryFemboy69420".getBytes(StandardCharsets.UTF_8), "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(bas));
        return new String(decrypted).replaceAll("[^a-zA-Z0-9-._:]", "");
    }

    @SneakyThrows
    public static String getDecrypt(String bas, boolean a) {
        SecretKeySpec key = new SecretKeySpec("HassAltsTheBest486124".getBytes(StandardCharsets.UTF_8), "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(bas));
        return new String(decrypted).replaceAll("[^a-zA-Z0-9-._:]", "");
    }

    public void draw(int mouseX, int mouseY) {
        if (!isOpen) return;

        ScaledResolution sr = new ScaledResolution(mc);

        RenderUtil.drawBlur(() -> {
            RenderUtil.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), Color.WHITE);
        });

        RenderUtil.drawRoundedRect(sr.getScaledWidth() / 2f - 95, sr.getScaledHeight() / 2f - 105, 190, 210 + 35, 7, new Color(0x66121212, true));
        RenderUtil.drawRoundedOutline(sr.getScaledWidth() / 2f - 96, sr.getScaledHeight() / 2f - 106, 192, 212 + 35, 8, 1, new Color(0x1AFFFFFF, true));

        Fonts.getNunito(20).drawCenteredString("Choose Alt Type", sr.getScaledWidth() / 2f, sr.getScaledHeight() / 2f - 95, -1);

        buttons.forEach(button -> button.render(mouseX, mouseY));

        if (closing) {
            isOpen = false;
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!isOpen) return;
        for (AltManagerButton button : buttons) {
            if (button.isHovered(mouseX, mouseY)) {
                button.mouseClicked(mouseX, mouseY, mouseButton);
                return;
            }
        }
        close();
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (!isOpen) return;
        if (keyCode == 1) close();
    }
    public static String[] getProfileInfo(String token) throws IOException { // taken straight from meow client <3
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
        request.setHeader("Authorization", "Bearer " + token);
        CloseableHttpResponse response = client.execute(request);
        String jsonString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        String IGN = jsonObject.get("name").getAsString();
        String UUID = jsonObject.get("id").getAsString();
        return new String[]{IGN, UUID};
    }
}
