package wangbot.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class DexAPIHandler {
    private static final URI apiURL;

    static {
        try {
            apiURL = new URI("https://api.mangadex.org");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getID(String url) {
        return url.replaceAll(".*/title/([^/]+).*", "$1");
    }

    public JSONObject getMangaInfo(String mangaId) throws IOException, InterruptedException {
        String endpoint = "/manga/" + mangaId;
        URI url = apiURL.resolve(endpoint);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            throw new IOException("Failed to fetch manga info: " + response.statusCode());
        }
    }

    public JSONObject searchManga(String title) throws IOException, InterruptedException {
        String endpoint = "/manga?title=" + title;
        URI url = apiURL.resolve(endpoint);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            throw new IOException("Failed to search manga: " + response.statusCode());
        }
    }
}