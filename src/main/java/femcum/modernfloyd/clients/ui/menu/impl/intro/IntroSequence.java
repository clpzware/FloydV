// File: IntroSequence.java

package femcum.modernfloyd.clients.ui.menu.impl.intro;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.ui.menu.impl.main.MainMenu;
import femcum.modernfloyd.clients.util.animation.Animation;
import femcum.modernfloyd.clients.util.animation.Easing;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IntroSequence extends GuiScreen {
    private final Animation logoAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, 3000);
    private boolean started = false;

    // Authentication fields
    private static final String HWID_FILE = "HWID.json";
    private static final String CLIENT_DATA_DIR = "floyd_client";
    private static final String AUTH_URL = "http://87.106.208.203:13487/";
    private static final String CLIENT_ID = "1417894615457202286";
    private static final String CLIENT_SECRET = "EugjCOAnjVK-5kMmQUIO24PgZZD0YODq";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final long PRESENTING_DURATION = 2000;
    private static final long HWID_CHECK_DURATION = 1000;
    private static final long CACHE_VERIFY_DURATION = 3000;
    private static final long AUTHENTICATING_DURATION = 45000;
    private static final long SUCCESS_DISPLAY_DURATION = 2000;
    private static final long LOADING_DURATION = 3000;
    private static final long MAX_AUTH_TIME = 60000;
    private static final long DENIED_SHUTDOWN_TIMEOUT = 1000;

    private static final ExecutorService authExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Floyd-Auth-Thread");
        t.setDaemon(true);
        return t;
    });
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static String cachedHWID = null;
    private static UserData cachedUserData = null;
    private static volatile String authState = "presenting";
    private static CompletableFuture<AuthResult> authFuture;
    private static long stateStartTime = System.currentTimeMillis();
    private static boolean authenticationCompleted = false;
    private static long authStartTime = 0;
    private static int count = 0;

    private static class UserData {
        String hwid, discordId, username, displayName, uid;
        long lastAuth;
        boolean isValid;

        UserData(String hwid, String discordId, String username, String displayName, String uid) {
            this.hwid = hwid;
            this.discordId = discordId;
            this.username = username;
            this.displayName = displayName;
            this.uid = uid;
            this.lastAuth = System.currentTimeMillis();
            this.isValid = true;
        }
    }

    private static class AuthResult {
        boolean success;
        String username, displayName, userId, uid, errorMessage, fullJsonResponse;

        AuthResult(boolean success, String username, String displayName, String userId, String uid, String fullJsonResponse) {
            this.success = success;
            this.username = username;
            this.displayName = displayName;
            this.userId = userId;
            this.uid = uid;
            this.fullJsonResponse = fullJsonResponse;
        }

        AuthResult(boolean success, String username, String displayName, String userId, String uid, String errorMessage, String fullJsonResponse) {
            this.success = success;
            this.username = username;
            this.displayName = displayName;
            this.userId = userId;
            this.uid = uid;
            this.errorMessage = errorMessage;
            this.fullJsonResponse = fullJsonResponse;
        }
    }

    @Override
    public void initGui() {
        if (!started) {
            started = true;
            logoAnimation.setValue(255);
            logoAnimation.reset();
            authStartTime = System.currentTimeMillis();
            System.out.println("[Floyd Auth] Starting authentication sequence...");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Objects.equals(authState, "denied")) {
            System.err.println("[Floyd Auth] SECURITY: Attempted to draw screen in denied/shutdown state");
            System.exit(0);
            return;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        RenderUtil.color(Color.WHITE);
        RenderUtil.rectangle(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), Color.BLACK);
        RenderUtil.image(new net.minecraft.util.ResourceLocation("floyd/images/splash.png"),
                sr.getScaledWidth() / 2D - 75, sr.getScaledHeight() / 2D - 25,
                150, 50, new Color(255, 255, 255, (int) logoAnimation.getValue()));

        logoAnimation.run(0);
        handleAuthenticationFlow();

        // Draw authentication status text
       // String displayText = getDisplayText();
       // int color = getDisplayColor();
       // mc.fontRendererObj.drawString(displayText,
       //         (int)((sr.getScaledWidth() - mc.fontRendererObj.getStringWidth(displayText)) / 2f),
         //       (int)(sr.getScaledHeight() / 2f + 40), color, true);

        if (shouldContinueAuthLoop()) {
            return;
        }

        if (Objects.equals(authState, "loading") && Floyd.DEVELOPMENT_SWITCH) {
            System.out.println("[Floyd Auth] Authentication and loading complete, transitioning to MainMenu");
            mc.displayGuiScreen(new MainMenu());
            Floyd.INSTANCE.getConfigManager().setupLatestConfig();
        }
    }

    private boolean shouldContinueAuthLoop() {
        if (Objects.equals(authState, "denied")) {
            if ((System.currentTimeMillis() - stateStartTime) > DENIED_SHUTDOWN_TIMEOUT) {
                System.err.println("[Floyd Auth] Denied state timeout exceeded - shutting down");
                System.exit(0);
            }
            return false;
        }

        if (authStartTime > 0 && (System.currentTimeMillis() - authStartTime) > MAX_AUTH_TIME) {
            System.err.println("[Floyd Auth] Maximum authentication time exceeded");
            authState = "denied";
            System.exit(0);
            return false;
        }

        long timeInState = System.currentTimeMillis() - stateStartTime;
        return switch (authState) {
            case "presenting" -> timeInState < PRESENTING_DURATION;
            case "hwid_check" -> timeInState < HWID_CHECK_DURATION;
            case "verifying_cache" -> timeInState < CACHE_VERIFY_DURATION && (authFuture == null || !authFuture.isDone());
            case "authenticating" -> timeInState < AUTHENTICATING_DURATION && (authFuture == null || !authFuture.isDone());
            case "success" -> timeInState < SUCCESS_DISPLAY_DURATION;
            case "loading" -> timeInState < LOADING_DURATION;
            default -> {
                System.err.println("[Floyd Auth] Unknown auth state: " + authState);
                authState = "denied";
                System.exit(0);
                yield false;
            }
        };
    }

    private void handleAuthenticationFlow() {
        long currentTime = System.currentTimeMillis();
        long timeInState = currentTime - stateStartTime;

        switch (authState) {
            case "presenting" -> {
                if (timeInState >= PRESENTING_DURATION) {
                    authState = "hwid_check";
                    stateStartTime = currentTime;
                    count = 0;
                    checkCachedAuth();
                }
            }
            case "hwid_check" -> {
                if (timeInState >= HWID_CHECK_DURATION) {
                    if (cachedUserData != null) {
                        authState = "verifying_cache";
                        stateStartTime = currentTime;
                        verifyCachedAuth();
                    } else {
                        authState = "authenticating";
                        stateStartTime = currentTime;
                        startAuthentication();
                    }
                }
            }
            case "verifying_cache" -> {
                if (authFuture != null && authFuture.isDone()) {
                    handleAuthResult(currentTime);
                } else if (timeInState >= CACHE_VERIFY_DURATION) {
                    System.err.println("[Floyd Auth] Cache verification timeout");
                    authState = "denied";
                    System.exit(0);
                }
            }
            case "authenticating" -> {
                if (authFuture != null && authFuture.isDone()) {
                    handleAuthResult(currentTime);
                } else if (timeInState >= AUTHENTICATING_DURATION) {
                    System.err.println("[Floyd Auth] Authentication timeout");
                    cancelAuth();
                    authState = "denied";
                    System.exit(0);
                }
            }
            case "success" -> {
                if (timeInState >= SUCCESS_DISPLAY_DURATION) {
                    authState = "loading";
                    stateStartTime = currentTime;
                }
            }
            case "loading" -> {}
            case "denied" -> System.exit(0);
        }
    }

    private void checkCachedAuth() {
        cachedUserData = loadCachedUserData();
        if (cachedUserData != null) {
            Floyd.DISCUSER = cachedUserData.username;
            Floyd.UID = cachedUserData.uid;
        } else {
            Floyd.DISCUSER = "Unknown";
            Floyd.UID = "0";
        }
    }

    private void verifyCachedAuth() {
        if (cachedUserData == null) return;
        authFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return checkWhitelist(cachedUserData.discordId, cachedUserData.username, cachedUserData.displayName);
            } catch (Exception e) {
                return new AuthResult(false, null, null, null, null, "Cache verification failed", null);
            }
        }, authExecutor);
    }

    private void startAuthentication() {
        String state = java.util.UUID.randomUUID().toString().replace("-", "");
        String oauthUrl = "https://discord.com/api/oauth2/authorize?client_id=" + CLIENT_ID +
                "&redirect_uri=" + encodeURL(REDIRECT_URI) +
                "&response_type=code&scope=identify&state=" + state;

        authFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return handleOAuthFlow(oauthUrl, state);
            } catch (Exception e) {
                return new AuthResult(false, null, null, null, null, "OAuth failed: " + e.getMessage(), null);
            }
        }, authExecutor);
    }

    private void handleAuthResult(long currentTime) {
        try {
            AuthResult result = authFuture.get();
            if (result.success) {
                if (cachedUserData == null) {
                    cachedUserData = new UserData(generateHWID(), result.userId, result.username, result.displayName, result.uid);
                } else {
                    cachedUserData.lastAuth = System.currentTimeMillis();
                    cachedUserData.uid = result.uid;
                }
                saveCachedUserData(cachedUserData);
                authState = "success";
                Floyd.DISCUSER = result.username;
                Floyd.UID = result.uid;
                authenticationCompleted = true;
                stateStartTime = currentTime;
            } else {
                authState = "denied";
                System.exit(0);
            }
            authFuture = null;
        } catch (Exception e) {
            authState = "denied";
            System.exit(0);
        }
    }

    private String getDisplayText() {
        return switch (authState) {
            case "presenting" -> "FloydCEO Presents" + ".".repeat(count % 4);
            case "hwid_check" -> "Checking authentication...";
            case "verifying_cache" -> "Verifying cached login...";
            case "authenticating" -> "Authorize via browser...";
            case "success" -> {
                long timeLeft = (SUCCESS_DISPLAY_DURATION - (System.currentTimeMillis() - stateStartTime)) / 1000;
                String baseText = "Welcome " + (Floyd.DISCUSER.isEmpty() ? "User" : Floyd.DISCUSER);
                if (!Objects.equals(Floyd.UID, "0")) baseText += " | UID: " + Floyd.UID;
                yield baseText + (timeLeft > 0 ? " (" + timeLeft + "s)" : "");
            }
            case "denied" -> "ACCESS DENIED - SHUTTING DOWN";
            case "loading" -> "Loading Floyd...";
            default -> "Unknown State";
        };
    }

    private int getDisplayColor() {
        return switch (authState) {
            case "presenting" -> Color.WHITE.getRGB();
            case "hwid_check", "verifying_cache", "authenticating" -> new Color(150, 150, 150).getRGB();
            case "success" -> new Color(100, 255, 100).getRGB();
            case "denied" -> new Color(255, 50, 50).getRGB();
            case "loading" -> new Color(100, 150, 255).getRGB();
            default -> Color.WHITE.getRGB();
        };
    }

    private String generateHWID() {
        if (cachedHWID != null) return cachedHWID;
        try {
            String hwid = System.getProperty("os.name", "unknown") +
                    System.getProperty("os.arch", "unknown") +
                    System.getProperty("user.name", "unknown") +
                    Runtime.getRuntime().availableProcessors() +
                    Runtime.getRuntime().maxMemory() +
                    System.getProperty("java.version", "unknown");

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(hwid.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            cachedHWID = hexString.substring(0, 32);
            return cachedHWID;
        } catch (Exception e) {
            System.err.println("[Floyd Auth] HWID generation failed");
            System.exit(0);
            return null;
        }
    }

    private Path getClientDataPath() {
        try {
            Path dataDir = Paths.get(System.getProperty("user.home"), CLIENT_DATA_DIR);
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            return dataDir;
        } catch (Exception e) {
            System.err.println("[Floyd Auth] Failed to create client data directory");
            System.exit(0);
            return null;
        }
    }

    private UserData loadCachedUserData() {
        try {
            Path hwidFile = getClientDataPath().resolve(HWID_FILE);
            if (!Files.exists(hwidFile)) return null;

            String jsonContent = new String(Files.readAllBytes(hwidFile), StandardCharsets.UTF_8);
            JsonObject json = gson.fromJson(jsonContent, JsonObject.class);

            String cachedHwid = json.get("hwid").getAsString();
            if (!Objects.equals(cachedHwid, generateHWID())) {
                Files.deleteIfExists(hwidFile);
                return null;
            }

            UserData userData = new UserData(
                    json.get("hwid").getAsString(),
                    json.get("discordId").getAsString(),
                    json.get("username").getAsString(),
                    json.has("displayName") ? json.get("displayName").getAsString() : null,
                    json.has("uid") ? json.get("uid").getAsString() : "0"
            );
            userData.lastAuth = json.get("lastAuth").getAsLong();
            userData.isValid = json.get("isValid").getAsBoolean();

            if ((System.currentTimeMillis() - userData.lastAuth) > 7 * 24 * 60 * 60 * 1000L) {
                return null;
            }
            return userData;
        } catch (Exception e) {
            System.err.println("[Floyd Auth] Failed to load cached authentication data");
            System.exit(0);
            return null;
        }
    }

    private void saveCachedUserData(UserData userData) {
        try {
            Path hwidFile = getClientDataPath().resolve(HWID_FILE);
            JsonObject json = new JsonObject();
            json.addProperty("hwid", userData.hwid);
            json.addProperty("discordId", userData.discordId);
            json.addProperty("username", userData.username);
            if (userData.displayName != null) json.addProperty("displayName", userData.displayName);
            json.addProperty("uid", userData.uid);
            json.addProperty("lastAuth", userData.lastAuth);
            json.addProperty("isValid", userData.isValid);
            Files.write(hwidFile, gson.toJson(json).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("[Floyd Auth] Failed to save authentication data");
        }
    }

    private AuthResult checkWhitelist(String userId, String username, String displayName) {
        try {
            String apiUrl = AUTH_URL + "auth/" + userId;
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "Floyd-Client/1.0");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            String responseBody = readResponse(conn, responseCode == 200);
            if (responseCode == 200 && !responseBody.isEmpty()) {
                String successValue = extractJsonValue(responseBody, "success");
                if ("true".equalsIgnoreCase(successValue)) {
                    String uid = extractJsonValue(responseBody, "uid");
                    return new AuthResult(true, username, displayName, userId, uid, responseBody);
                }
            }
            authState = "denied";
            return new AuthResult(false, null, null, null, null, "User not whitelisted", responseBody);
        } catch (Exception e) {
            authState = "denied";
            return new AuthResult(false, null, null, null, null, "Authorization check failed", null);
        }
    }

    private AuthResult handleOAuthFlow(String oauthUrl, String expectedState) {
        java.net.ServerSocket server = null;
        try {
            server = new java.net.ServerSocket(8080);
            server.setSoTimeout(45000);
            openBrowser(oauthUrl);
            java.net.Socket client = server.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String requestLine = in.readLine();

            String code = null, state = null, error = null;
            if (requestLine != null) {
                if (requestLine.contains("code=")) {
                    code = extractFromURL(requestLine, "code");
                    state = extractFromURL(requestLine, "state");
                } else if (requestLine.contains("error=")) {
                    error = extractFromURL(requestLine, "error");
                }
            }

            AuthResult authResult;
            if (error != null) {
                authResult = new AuthResult(false, null, null, null, null, "User denied: " + error, null);
            } else if (code != null && Objects.equals(expectedState, state)) {
                authResult = exchangeCodeForUser(code);
            } else {
                authResult = new AuthResult(false, null, null, null, null, "Invalid callback", null);
            }

            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html; charset=utf-8");
            out.println("Connection: close");
            out.println();
            out.println("<!DOCTYPE html><html><head><title>Floyd Client Authentication</title>");
            out.println("<style>body { font-family: Arial, sans-serif; background: #000; color: #fff; text-align: center; padding: 50px; }");
            out.println(".container { background: rgba(255,0,0,0.2); padding: 20px; border-radius: 10px; }");
            out.println("</style></head><body><div class='container'>");
            out.println(authResult.success ? "<h2>Authentication Successful</h2><p>Welcome " + Objects.requireNonNullElse(authResult.username, "User") + "!</p>" :
                    "<h2>Authentication Failed</h2><p>" + Objects.requireNonNullElse(authResult.errorMessage, "Access denied") + "</p><p>Visit <a href='https://floyd.sellhub.cx/'>https://floyd.sellhub.cx/</a> to purchase access.</p>");
            out.println("</div></body></html>");
            client.close();
            server.close();
            return authResult;
        } catch (Exception e) {
            System.err.println("[Floyd Auth] OAuth callback server error");
            System.exit(0);
            return new AuthResult(false, null, null, null, null, "Server error", null);
        } finally {
            if (server != null && !server.isClosed()) {
                try { server.close(); } catch (Exception ignored) {}
            }
        }
    }

    private AuthResult exchangeCodeForUser(String code) {
        try {
            String tokenUrl = "https://discord.com/api/oauth2/token";
            String postData = "client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET +
                    "&grant_type=authorization_code&code=" + code + "&redirect_uri=" + encodeURL(REDIRECT_URI);
            String tokenResponse = httpPost(tokenUrl, postData);
            String accessToken = extractJsonValue(tokenResponse, "access_token");
            if (accessToken == null) return new AuthResult(false, null, null, null, null, "Failed to get access token", null);

            String userResponse = httpGetWithAuth("https://discord.com/api/users/@me", accessToken);
            String userId = extractJsonValue(userResponse, "id");
            String username = extractJsonValue(userResponse, "username");
            String displayName = extractJsonValue(userResponse, "display_name");
            if (userId == null) return new AuthResult(false, null, null, null, null, "Failed to get user info", null);

            return checkWhitelist(userId, username, displayName);
        } catch (Exception e) {
            return new AuthResult(false, null, null, null, null, "Token exchange failed", null);
        }
    }

    private String httpPost(String url, String postData) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", "Floyd-Client/1.0");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes());
        }
        return readResponse(conn, conn.getResponseCode() < 400);
    }

    private String httpGetWithAuth(String url, String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("User-Agent", "Floyd-Client/1.0");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        return readResponse(conn, true);
    }

    private String readResponse(HttpURLConnection conn, boolean useInputStream) throws Exception {
        InputStream inputStream = useInputStream ? conn.getInputStream() :
                (conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        return response.toString();
    }

    private String extractFromURL(String requestLine, String param) {
        try {
            String query = requestLine.split("\\?")[1].split(" ")[0];
            for (String pair : query.split("&")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2 && Objects.equals(keyValue[0], param)) {
                    return java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            System.err.println("[Floyd Auth] URL parameter extraction error");
        }
        return null;
    }

    private String encodeURL(String s) {
        try {
            return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
        } catch (Exception e) {
            System.out.println("[Floyd Auth] Please manually visit: " + url);
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String searchFor = "\"" + key + "\":";
            int startIndex = json.indexOf(searchFor);
            if (startIndex == -1) return null;
            startIndex += searchFor.length();
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
            if (endIndex == -1) return null;
            String value = json.substring(startIndex, endIndex).trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value.isEmpty() ? null : value;
        } catch (Exception e) {
            return null;
        }
    }

    private void cancelAuth() {
        if (authFuture != null) {
            authFuture.cancel(true);
            authFuture = null;
        }
    }

    @Override
    public void onGuiClosed() {
        if (!authenticationCompleted && !Objects.equals(authState, "denied")) {
            System.err.println("[Floyd Auth] GUI closed before authentication completion");
            System.exit(0);
        }
        if (!authExecutor.isShutdown()) {
            authExecutor.shutdown();
            try {
                if (!authExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    authExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                authExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}