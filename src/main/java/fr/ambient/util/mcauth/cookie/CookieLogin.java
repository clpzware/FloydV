package fr.ambient.util.mcauth.cookie;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

public class CookieLogin {

    private final static Gson gson = new Gson();

    private final static String useragent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";

    public static LoginData loginWithCookie(File cookieFile) {
        try {
            final String[] cookiesText = FileUtils.readFileToString(cookieFile).split("\n");

            final StringBuilder sb = new StringBuilder();

            for (final String cookie : cookiesText) {
                final String name = cookie.split("\t")[5].trim();
                final String value = cookie.split("\t")[6].trim();
                sb.append(name).append("=").append(value).append("; ");
            }

            final String cookie = sb.toString();

            final String loc1 = getNextLocation(new URI("https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin&aid=1142970254").toURL(), "PHPSESSID=0");
            final String loc2 = getNextLocation(new URI(loc1.replaceAll(" ", "%20")).toURL(), cookie);
            final String loc3 = getNextLocation(new URI(loc2.replaceAll(" ", "%20")).toURL(), cookie);

            final String accessToken = loc3.split("accessToken=")[1];

            final String decoded = new String(Base64.getDecoder().decode(accessToken), StandardCharsets.UTF_8)
                    .split("\"rp://api.minecraftservices.com/\",")[1];

            final String token = decoded.split("\"Token\":\"")[1].split("\"")[0];

            final String uhs = decoded.split(Pattern.quote("{\"DisplayClaims\":{\"xui\":[{\"uhs\":\""))[1].split("\"")[0];

            final String xbl = "XBL3.0 x=" + uhs + ";" + token;

            final String output = postExternal("https://api.minecraftservices.com/authentication/login_with_xbox", "{\"identityToken\":\"" + xbl + "\",\"ensureLegacyEnabled\":true}", true);
            final String mcToken = gson.fromJson(output, JsonObject.class).get("access_token").getAsString();

            final HttpsURLConnection connection = (HttpsURLConnection) new URI("https://api.minecraftservices.com/minecraft/profile").toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + mcToken);

            final JsonObject profileResponse = gson.fromJson(IOUtils.toString(connection.getInputStream()), JsonObject.class);
            return new LoginData(mcToken, "", profileResponse.get("id").getAsString(), profileResponse.get("name").getAsString());
        } catch (Exception e) {
            return null;
        }
    }

    public static String getNextLocation(URL url, String cookie) throws IOException {
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        connection.setRequestProperty("Accept-Encoding", "");
        connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
        connection.setRequestProperty("Cookie", cookie);
        connection.setRequestProperty("User-Agent", useragent);
        connection.setInstanceFollowRedirects(false);
        connection.connect();

        return connection.getHeaderField("Location");
    }

    public static String postExternal(final String url, final String post, final boolean json) {
        try {
            final HttpsURLConnection connection = connect(url, post, json);
            final int responseCode = connection.getResponseCode();

            final InputStream stream = responseCode / 100 == 2 || responseCode / 100 == 3 ? connection.getInputStream() : connection.getErrorStream();

            if (stream == null) {
                System.err.println(responseCode + ": " + url);
                System.out.println(IOUtils.toString(connection.getInputStream()));
                return null;
            }

            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String lineBuffer;
            final StringBuilder response = new StringBuilder();
            while ((lineBuffer = reader.readLine()) != null) {
                response.append(lineBuffer);
            }

            reader.close();

            return response.toString();
        } catch (final Exception exception) {
            exception.printStackTrace(System.err);
            return null;
        }
    }

    @NotNull
    private static HttpsURLConnection connect(String url, String post, boolean json) throws IOException, URISyntaxException {
        final HttpsURLConnection connection = (HttpsURLConnection) new URI(url).toURL().openConnection();
        connection.addRequestProperty("User-Agent", useragent);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        final byte[] out = post.getBytes(StandardCharsets.UTF_8);
        final int length = out.length;
        connection.setFixedLengthStreamingMode(length);
        connection.addRequestProperty("Content-Type", json ? "application/json" : "application/x-www-form-urlencoded; charset=UTF-8");
        connection.addRequestProperty("Accept", "application/json");
        connection.connect();

        try (final OutputStream os = connection.getOutputStream()) {
            os.write(out);
        }

        return connection;
    }

    public record LoginData(String mcToken, String newRefreshToken, String uuid, String username) {
        /* */
    }
}

