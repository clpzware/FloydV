package fr.ambient.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CookieAuth {
    private static final String CLIENT_ID = "00000000402b5328";
    private static final String REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf";

    public static String postRequest(String url, String jsonBody) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(jsonBody));
            post.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(post)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("Request failed: " + response.getStatusLine().getStatusCode());
                }
                return new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                        .lines()
                        .reduce("", (acc, line) -> acc + line);
            }
        }
    }

    public static String getRequest(String url, String authorizationHeader) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            if (authorizationHeader != null) {
                get.setHeader("Authorization", authorizationHeader);
            }

            try (CloseableHttpResponse response = client.execute(get)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("Request failed: " + response.getStatusLine().getStatusCode());
                }
                return new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                        .lines()
                        .reduce("", (acc, line) -> acc + line);
            }
        }
    }

    public static JsonObject getPlayerFromCode(String code) throws IOException {
        String tokenResponse = postRequest("https://login.live.com/oauth20_token.srf",
                String.format("""
                        {
                            "client_id": "%s",
                            "code": "%s",
                            "grant_type": "authorization_code",
                            "redirect_uri": "%s",
                            "scope": "service::user.auth.xboxlive.com::MBI_SSL"
                        }
                        """, CLIENT_ID, code, REDIRECT_URI));

        JsonObject tokenJson = JsonParser.parseString(tokenResponse).getAsJsonObject();
        String accessToken = tokenJson.get("access_token").getAsString();

        String xblResponse = postRequest("https://user.auth.xboxlive.com/user/authenticate",
                String.format("""
                        {
                            "Properties": {
                                "AuthMethod": "RPS",
                                "SiteName": "user.auth.xboxlive.com",
                                "RpsTicket": "%s"
                            },
                            "RelyingParty": "http://auth.xboxlive.com",
                            "TokenType": "JWT"
                        }
                        """, accessToken));

        JsonObject xblJson = JsonParser.parseString(xblResponse).getAsJsonObject();
        String xblToken = xblJson.get("Token").getAsString();
        String uhs = xblJson.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();

        String xstsResponse = postRequest("https://xsts.auth.xboxlive.com/xsts/authorize",
                String.format("""
                        {
                            "Properties": {
                                "SandboxId": "RETAIL",
                                "UserTokens": ["%s"]
                            },
                            "RelyingParty": "rp://api.minecraftservices.com/",
                            "TokenType": "JWT"
                        }
                        """, xblToken));

        JsonObject xstsJson = JsonParser.parseString(xstsResponse).getAsJsonObject();
        String xstsToken = xstsJson.get("Token").getAsString();

        String mcAuthResponse = postRequest("https://api.minecraftservices.com/authentication/login_with_xbox",
                String.format("""
                        {
                            "identityToken": "XBL3.0 x=%s;%s"
                        }
                        """, uhs, xstsToken));

        JsonObject mcAuthJson = JsonParser.parseString(mcAuthResponse).getAsJsonObject();
        String mcAccessToken = mcAuthJson.get("access_token").getAsString();

        String mcProfileResponse = getRequest("https://api.minecraftservices.com/minecraft/profile",
                "Bearer " + mcAccessToken);

        return JsonParser.parseString(mcProfileResponse).getAsJsonObject();
    }

    public CookieAuth(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String cookie = null;

            for (String line; (line = br.readLine()) != null; ) {
                if (line.contains("__Host-MSAAUTHP")) {
                    cookie = line.split("__Host-MSAAUTHP")[1].trim();
                } else if (line.contains("__Host-MSAAUTH")) {
                    cookie = line.split("__Host-MSAAUTH")[1].trim();
                }
            }

            if (cookie == null) {
                System.out.println("Invalid cookie file.");
                return;
            }

            String url = "https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code" +
                    "&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf";

            String sessionResponse = getRequest(url, "Cookie: __Host-MSAAUTHP=" + cookie);

            String code = null;


            System.out.println(sessionResponse);
            if (sessionResponse.contains("code=")) {
                code = sessionResponse.split("code=")[1].split("&")[0];
            }

            if (code == null) {
                System.out.println("Invalid session or code not found.");
                return;
            }

            JsonObject playerProfile = getPlayerFromCode(code);
            System.out.println("Player ID: " + playerProfile.get("id").getAsString());
            System.out.println("Player Name: " + playerProfile.get("name").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
