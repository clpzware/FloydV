package fr.ambient.util.mcauth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import fr.ambient.ui.newaltmanager.AltType;
import fr.ambient.ui.newaltmanager.GuiAltManager;
import fr.ambient.ui.newaltmanager.NewAlt;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MicrosoftAuthenticator {
    private final String clientId;
    private final String clientSecret;

    private String username;
    private String uuid;
    private String accessToken;

    private final String redirectUri = "http://localhost/api";
    private final String loginUri;

    private String authCode;
    private String xboxAccessToken;
    private String xblToken;
    private String xboxUserhash;
    private String xstsToken;

    private String refreshToken;
    private boolean shouldRefreshLogin;

    private GuiAltManager parent;


    public MicrosoftAuthenticator(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        shouldRefreshLogin = true;

        loginUri = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?client_id=" + clientId + "&response_type=code&redirect_uri=" + redirectUri + "&scope=XboxLive.signin%20offline_access";
    }

    public MicrosoftAuthenticator(GuiAltManager parent, String clientId, String clientSecret) {
        this.parent = parent;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        shouldRefreshLogin = true;

        loginUri = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?client_id=" + clientId + "&response_type=code&redirect_uri=" + redirectUri + "&scope=XboxLive.signin%20offline_access";
    }

    public void login() {
        if (shouldRefreshLogin) {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
                server.createContext("/api", new MyHandler(server, this));
                server.setExecutor(null);
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (Desktop.getDesktop() != null)
                    Desktop.getDesktop().browse(new URI(loginUri));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            getToken(false);
        }
    }

    public void loginWithoutBrowseAndCopyIntoiClip(){
        if (shouldRefreshLogin) {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
                server.createContext("/api", new MyHandler(server, this));
                server.setExecutor(null);
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            copy(loginUri);

        } else {
            getToken(false);

        }
    }

    public void getToken(boolean freshLogin) {
        HttpPost post = new HttpPost("https://login.microsoftonline.com/consumers/oauth2/v2.0/token");
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();
        if (freshLogin) {
            urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
            urlParameters.add(new BasicNameValuePair("code", authCode));
            urlParameters.add(new BasicNameValuePair("client_id", clientId));
            urlParameters.add(new BasicNameValuePair("redirect_uri", redirectUri));
            urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));

            try {
                post.setEntity(new UrlEncodedFormEntity(urlParameters));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.addHeader("Content-type", "application/x-www-form-urlencoded");

            try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
                String json = EntityUtils.toString(response.getEntity());
                JsonObject jobj = new Gson().fromJson(json, JsonObject.class);
                xboxAccessToken = jobj.get("access_token").getAsString();

                refreshToken = jobj.get("refresh_token").getAsString();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
            urlParameters.add(new BasicNameValuePair("refresh_token", refreshToken));
            urlParameters.add(new BasicNameValuePair("client_id", clientId));
            urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));
            urlParameters.add(new BasicNameValuePair("scope", "XboxLive.signin"));

            try {
                post.setEntity(new UrlEncodedFormEntity(urlParameters));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.addHeader("Content-type", "application/x-www-form-urlencoded");

            try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
                String json = EntityUtils.toString(response.getEntity());
                JsonObject jobj = new Gson().fromJson(json, JsonObject.class);
                xboxAccessToken = jobj.get("access_token").getAsString();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        authXBL();
    }

    public void getToken(String token) {
        HttpPost post = new HttpPost("https://login.microsoftonline.com/consumers/oauth2/v2.0/token");
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();

        urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
        urlParameters.add(new BasicNameValuePair("refresh_token", token));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));
        urlParameters.add(new BasicNameValuePair("scope", "XboxLive.signin"));

        try {
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        post.addHeader("Content-type", "application/x-www-form-urlencoded");

        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
            String json = EntityUtils.toString(response.getEntity());
            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);
            xboxAccessToken = jobj.get("access_token").getAsString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        authXBL();
    }

    public void authXBL() {
        HttpPost post = new HttpPost("https://user.auth.xboxlive.com/user/authenticate");

        post.setHeader("Content-type", "application/json");
        post.setHeader("Accept", "application/json");

        String payload = "{\"Properties\": {\"AuthMethod\": \"RPS\", \"SiteName\": \"user.auth.xboxlive.com\", \"RpsTicket\": \"d=" + xboxAccessToken + "\"},\"RelyingParty\": \"http://auth.xboxlive.com\", \"TokenType\": \"JWT\"}";
        StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
            String json = EntityUtils.toString(response.getEntity());
            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);
            xblToken = jobj.get("Token").getAsString();
            xboxUserhash = jobj.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        authXSTS();
    }

    private void authXSTS() {
        HttpPost post = new HttpPost("https://xsts.auth.xboxlive.com/xsts/authorize");

        post.setHeader("Content-type", "application/json");
        post.setHeader("Accept", "application/json");

        String payload = "{\"Properties\": {\"SandboxId\": \"RETAIL\", \"UserTokens\": [\"" + xblToken + "\"]}, \"RelyingParty\": \"rp://api.minecraftservices.com/\", \"TokenType\": \"JWT\"}";
        StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
            String json = EntityUtils.toString(response.getEntity());
            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);
            xstsToken = jobj.get("Token").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        authMinecraft();
    }

    private void authMinecraft() {
        HttpPost post = new HttpPost("https://api.minecraftservices.com/authentication/login_with_xbox");

        String payload = "{\"identityToken\": \"XBL3.0 x=" + xboxUserhash + ";" + xstsToken + "\"}";
        StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
            String json = EntityUtils.toString(response.getEntity());
            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);
            accessToken = jobj.get("access_token").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkOwnership();
    }

    private void checkOwnership() {
        getProfile();
    }

    private void getProfile() {
        HttpGet get = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
        get.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(get)) {

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                System.err.println("Error: Received HTTP " + statusCode + " from Minecraft API.");
                return;
            }

            String json = EntityUtils.toString(response.getEntity());
            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);

            if (jobj == null || !jobj.has("id") || !jobj.has("name")) {
                System.err.println("Error: Invalid profile data received. JSON: " + json);
                return;
            }

            uuid = jobj.get("id").isJsonNull() ? null : jobj.get("id").getAsString();
            username = jobj.get("name").isJsonNull() ? null : jobj.get("name").getAsString();

            if (uuid == null || username == null) {
                System.err.println("Error: Profile data contains null values. JSON: " + json);
                return;
            }

            setSession();
        } catch (IOException e) {
            System.err.println("Error fetching Minecraft profile: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void setSession() {
        if (username == null || uuid == null || accessToken == null) {
            System.err.println("Error: Cannot set session. Missing required fields: " +
                    "username=" + username + ", uuid=" + uuid + ", accessToken=" + accessToken);
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            System.err.println("Error: Minecraft instance is null. Cannot set session.");
            return;
        }

        Session session = new Session(username, uuid, accessToken, "mojang");

        if (this.parent == null) {
            mc.setSession(session);
        } else {
            mc.setSession(session);

            if (parent != null) {
                parent.addAlt(new NewAlt(parent, session, AltType.MICROSOFT, refreshToken));
            } else {
                System.err.println("Warning: Parent is null. Skipping alt addition.");
            }
        }
    }


    static class MyHandler implements HttpHandler {
        private final HttpServer server;
        private final MicrosoftAuthenticator changer;

        public MyHandler(HttpServer server, MicrosoftAuthenticator changer) {
            this.server = server;
            this.changer = changer;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String responseURI = t.getRequestURI().toString();
            String code = responseURI.split("=")[1];

            String response = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Logged In</title><style>body{margin:0;display:flex;justify-content:center;align-items:center;height:100vh;background-color:#121212;color:#ffffff;font-family:Arial,sans-serif}.message{font-size:1.5rem;text-align:center;}</style></head><body><div class=\"message\">Logged in! You can now close this window!</div></body></html>";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            server.stop(1);

            changer.authCode = code;
            changer.getToken(true);
        }

    }
    public static void copy(String data) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), null);
    }
}
