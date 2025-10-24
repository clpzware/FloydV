// File: IntroSequence.java

package femcum.modernfloyd.clients.ui.menu.impl.intro;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.ui.menu.impl.main.MainMenu;
import femcum.modernfloyd.clients.util.animation.Animation;
import femcum.modernfloyd.clients.util.animation.Easing;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.util.shader.RiseShaders;
import femcum.modernfloyd.clients.util.shader.base.ShaderRenderType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.lwjgl.opengl.GL11;

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

    private static final String HWID_FILE = "HWID.json";
    private static final String CLIENT_DATA_DIR = "floyd_client";
    private static final String AUTH_URL = "http://87.106.208.203:13487/";
    private static final String CLIENT_ID = "1417894615457202286";
    private static final String CLIENT_SECRET = "EugjCOAnjVK-5kMmQUIO24PgZZD0YODq";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final long SHADER_FADE_DURATION = 2000; // Time for shader to fade in
    private static final long SHADER_SOLO_DURATION = 3000; // Shader plays solo before splash appears
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
    private static volatile String authState = "shader_intro";
    private static CompletableFuture<AuthResult> authFuture;
    private static long stateStartTime = System.currentTimeMillis();
    private static boolean authenticationCompleted = false;
    private static long authStartTime = 0;
    private static int count = 0;
    private static long shaderStartTime = 0;

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
            logoAnimation.setValue(255); // Logo visible from start
            logoAnimation.reset();
            authStartTime = System.currentTimeMillis();
            shaderStartTime = System.currentTimeMillis();
            RiseShaders.INTRO_SHADER.update();
            System.out.println("[Floyd Auth] Starting intro sequence...");
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

        // Draw black background first
        RenderUtil.rectangle(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), Color.BLACK);

        // Calculate shader fade-in over 2 seconds
        long timeSinceStart = System.currentTimeMillis() - shaderStartTime;
        float shaderAlpha = Math.min(1.0f, timeSinceStart / (float)SHADER_FADE_DURATION);

        // Render shader with fade-in alpha - plays continuously throughout intro
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, shaderAlpha);

        RiseShaders.INTRO_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, null);

        // Reset GL state after shader
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        // Apply blur effect
        renderBlurEffect(sr);

        // Show splash and text AFTER shader has played solo for SHADER_SOLO_DURATION
        if (timeSinceStart >= SHADER_SOLO_DURATION) {
            // Start fading in the logo after shader solo period
            long fadeTime = timeSinceStart - SHADER_SOLO_DURATION;
            int alpha = (int) Math.min(255, (fadeTime / 1500.0) * 255); // Fade in over 1.5 seconds

            if (alpha > 0) {
                // Reset GL state before rendering image
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

                // Render the logo image with fade animation
                RenderUtil.color(Color.WHITE);
                RenderUtil.image(new net.minecraft.util.ResourceLocation("floyd/images/splash.png"),
                        sr.getScaledWidth() / 2D - 75, sr.getScaledHeight() / 2D - 25,
                        150, 50, new Color(255, 255, 255, alpha));

                // Show auth text only after logo is fully visible
                if (alpha >= 255) {
                    String displayText = getDisplayText();
                    int color = getDisplayColor();
                    float textX = (sr.getScaledWidth() - mc.fontRendererObj.width(displayText)) / 2f;
                    float textY = sr.getScaledHeight() / 2f + 50;

                    drawTextWithStroke(displayText, textX, textY, color);
                }
            }
        }

        handleAuthenticationFlow();

        if (shouldContinueAuthLoop()) {
            return;
        }

        if (Objects.equals(authState, "loading") && Floyd.DEVELOPMENT_SWITCH) {
            System.out.println("[Floyd Auth] Authentication and loading complete, transitioning to MainMenu");
            mc.displayGuiScreen(new MainMenu());
            Floyd.INSTANCE.getConfigManager().setupLatestConfig();
        }
    }

    private void drawTextWithStroke(String text, float x, float y, int color) {
        Color shadow = new Color(0, 0, 0, 180);

        // Draw shadow/stroke using RenderUtil if available, or just draw the main text
        mc.fontRendererObj.draw(text, x + 1, y + 1, shadow.getRGB());
        mc.fontRendererObj.draw(text, x - 1, y - 1, shadow.getRGB());
        mc.fontRendererObj.draw(text, x + 1, y - 1, shadow.getRGB());
        mc.fontRendererObj.draw(text, x - 1, y + 1, shadow.getRGB());

        // Draw main text
        mc.fontRendererObj.draw(text, x, y, color);
    }

    private void renderBlurEffect(ScaledResolution sr) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.3f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(sr.getScaledWidth(), 0);
        GL11.glVertex2f(sr.getScaledWidth(), sr.getScaledHeight());
        GL11.glVertex2f(0, sr.getScaledHeight());
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
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
            case "shader_intro" -> timeInState < SHADER_SOLO_DURATION;
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
            case "shader_intro" -> {
                if (timeInState >= SHADER_SOLO_DURATION) {
                    authState = "presenting";
                    stateStartTime = currentTime;
                    System.out.println("[Floyd Auth] Starting authentication sequence...");
                }
            }
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
            case "shader_intro" -> ""; // No text during shader intro
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

            String jsonContent = Files.readString(hwidFile, StandardCharsets.UTF_8);
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
            Files.writeString(hwidFile, gson.toJson(json), StandardCharsets.UTF_8);
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
            out.println("<meta charset='utf-8'><meta name='viewport' content='width=device-width, initial-scale=1'>");
            out.println("<style>");
            out.println("* { margin: 0; padding: 0; box-sizing: border-box; }");
            out.println("body { font-family: 'Segoe UI', system-ui, -apple-system, sans-serif; background: #000; color: #fff; height: 100vh; display: flex; align-items: center; justify-content: center; overflow: hidden; }");
            out.println("canvas { position: fixed; top: 0; left: 0; width: 100%; height: 100%; z-index: 0; }");
            out.println(".container { position: relative; z-index: 1; background: rgba(0,0,0,0.85); backdrop-filter: blur(20px); border: 1px solid rgba(255,0,0,0.2); border-radius: 16px; padding: 48px 40px; max-width: 480px; text-align: center; box-shadow: 0 8px 32px rgba(255,0,0,0.15), inset 0 1px 0 rgba(255,255,255,0.05); animation: slideUp 0.6s ease-out; }");
            out.println("@keyframes slideUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }");
            out.println(".logo { font-size: 42px; font-weight: 900; background: linear-gradient(135deg, #ff0000 0%, #cc0000 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; margin-bottom: 8px; letter-spacing: 2px; animation: glow 2s ease-in-out infinite; }");
            out.println("@keyframes glow { 0%, 100% { filter: drop-shadow(0 0 8px rgba(255,0,0,0.5)); } 50% { filter: drop-shadow(0 0 16px rgba(255,0,0,0.8)); } }");
            out.println(".subtitle { font-size: 13px; color: rgba(255,255,255,0.5); margin-bottom: 32px; letter-spacing: 3px; text-transform: uppercase; font-weight: 600; }");
            out.println(".status { font-size: 18px; margin: 32px 0; padding: 24px; border-radius: 12px; font-weight: 600; position: relative; overflow: hidden; }");
            out.println(".status::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent); animation: shimmer 2s infinite; }");
            out.println("@keyframes shimmer { 0% { left: -100%; } 100% { left: 100%; } }");
            out.println(".success { background: linear-gradient(135deg, rgba(0,255,100,0.15), rgba(0,200,80,0.15)); color: #00ff88; border: 1px solid rgba(0,255,100,0.3); }");
            out.println(".error { background: linear-gradient(135deg, rgba(255,0,0,0.15), rgba(200,0,0,0.15)); color: #ff4444; border: 1px solid rgba(255,0,0,0.3); }");
            out.println(".icon { font-size: 48px; margin-bottom: 16px; display: block; }");
            out.println(".username { color: #ff3333; font-weight: 700; }");
            out.println(".uid { color: rgba(255,255,255,0.6); font-size: 14px; margin-top: 8px; }");
            out.println(".instruction { font-size: 14px; color: rgba(255,255,255,0.5); margin-top: 24px; line-height: 1.6; }");
            out.println(".link { color: #ff3333; text-decoration: none; font-weight: 600; transition: all 0.3s; }");
            out.println(".link:hover { color: #ff6666; text-shadow: 0 0 8px rgba(255,0,0,0.5); }");
            out.println("</style></head><body>");
            out.println("<canvas id='bg'></canvas>");
            out.println("<div class='container'>");
            out.println("<div class='logo'>FLOYD</div>");
            out.println("<div class='subtitle'>CLIENT AUTHENTICATION</div>");

            if (authResult.success) {
                String welcomeUsername = authResult.username != null ? authResult.username : "User";
                String welcomeUid = authResult.uid != null ? authResult.uid : "N/A";
                out.println("<div class='status success'>");
                out.println("<span class='icon'>✓</span>");
                out.println("<div><strong>AUTHENTICATION SUCCESSFUL</strong></div>");
                out.println("<div style='margin-top: 16px;'>Welcome <span class='username'>" + welcomeUsername + "</span></div>");
                out.println("<div class='uid'>UID: " + welcomeUid + "</div>");
                out.println("</div>");
                out.println("<div class='instruction'>You can now return to your client</div>");
            } else {
                out.println("<div class='status error'>");
                out.println("<span class='icon'>✕</span>");
                out.println("<div><strong>AUTHENTICATION FAILED</strong></div>");
                out.println("<div style='margin-top: 12px; font-size: 15px;'>" + (authResult.errorMessage != null ? authResult.errorMessage : "Access denied") + "</div>");
                out.println("</div>");
                out.println("<div class='instruction'>Purchase access at <a href='https://floyd.sellhub.cx/' class='link'>floyd.sellhub.cx</a></div>");
                out.println("<div class='instruction' style='margin-top: 16px;'>You can now close this window</div>");
            }

            out.println("</div>");
            out.println("<script>");
            out.println("const canvas = document.getElementById('bg');");
            out.println("const gl = canvas.getContext('webgl');");
            out.println("if (gl) {");
            out.println("  canvas.width = window.innerWidth; canvas.height = window.innerHeight;");
            out.println("  const vs = gl.createShader(gl.VERTEX_SHADER);");
            out.println("  gl.shaderSource(vs, 'attribute vec2 p; void main() { gl_Position = vec4(p, 0, 1); }');");
            out.println("  gl.compileShader(vs);");
            out.println("  const fs = gl.createShader(gl.FRAGMENT_SHADER);");
            out.println("  gl.shaderSource(fs, 'precision mediump float; uniform float t; uniform vec2 r; void main() { vec2 u = (gl_FragCoord.xy * 2.0 - r) / r.y; float d = length(u); float a = atan(u.y, u.x); float wave = sin(d * 10.0 - t * 2.0) * 0.5 + 0.5; float rings = smoothstep(0.4, 0.6, wave); vec3 col = vec3(0.8, 0.0, 0.0) * rings * (1.0 - d * 0.5); gl_FragColor = vec4(col * 0.3, 1.0); }');");
            out.println("  gl.compileShader(fs);");
            out.println("  const prog = gl.createProgram();");
            out.println("  gl.attachShader(prog, vs); gl.attachShader(prog, fs); gl.linkProgram(prog); gl.useProgram(prog);");
            out.println("  const buf = gl.createBuffer(); gl.bindBuffer(gl.ARRAY_BUFFER, buf);");
            out.println("  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1,-1,1,-1,-1,1,1,1]), gl.STATIC_DRAW);");
            out.println("  const pos = gl.getAttribLocation(prog, 'p'); gl.enableVertexAttribArray(pos);");
            out.println("  gl.vertexAttribPointer(pos, 2, gl.FLOAT, false, 0, 0);");
            out.println("  const tLoc = gl.getUniformLocation(prog, 't');");
            out.println("  const rLoc = gl.getUniformLocation(prog, 'r');");
            out.println("  function draw() { gl.uniform1f(tLoc, Date.now() * 0.001); gl.uniform2f(rLoc, canvas.width, canvas.height); gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4); requestAnimationFrame(draw); }");
            out.println("  draw();");
            out.println("}");
            out.println("window.addEventListener('resize', () => { canvas.width = window.innerWidth; canvas.height = window.innerHeight; });");
            out.println("</script>");
            out.println("</body></html>");
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