package fr.ambient.util.packet;

import fr.ambient.util.render.img.ImageObject;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@UtilityClass
public class RequestUtil {
//    public final Function<String, String> requestResult = endpointWithArgs -> {
//        try {
//            URL url = new URI("" + endpointWithArgs).toURL();
//            URLConnection urlConnection = url.openConnection();
//            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//
//            String inputLine;
//            StringBuilder requestResult = new StringBuilder();
//
//            while ((inputLine = bufferedReader.readLine()) != null)
//                requestResult.append(inputLine);
//
//            bufferedReader.close();
//            return requestResult.toString();
//        } catch (Throwable throwable) {
//            // Failure :(
//            return "";
//        }
//    };
//    public final Function<String, String> requestResultAll = endpointWithArgs -> {
//        try {
//            URL url = new URI(endpointWithArgs).toURL();
//            URLConnection urlConnection = url.openConnection();
//            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//
//            String inputLine;
//            StringBuilder requestResult = new StringBuilder();
//
//            while ((inputLine = bufferedReader.readLine()) != null)
//                requestResult.append(inputLine);
//
//            bufferedReader.close();
//            return requestResult.toString();
//        } catch (Throwable throwable) {
//            // Failure :(
//            return "";
//        }
//    };
//
//
//    public static CompletableFuture<ImageObject> downloadAndLoad(String url, File destination) {
//
//        if(destination.exists()){
//            ImageObject image = new ImageObject(destination);
//            return image.loadAsync().thenApply(v -> image);
//        }
//
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 9.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36")
//                .build();
//
//        return client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(destination.toPath()))
//                .thenApply(HttpResponse::body)
//                .thenApply(path -> new ImageObject(destination))
//                .thenCompose(image -> image.loadAsync().thenApply(v -> image));
//    }
}
