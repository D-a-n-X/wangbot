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

    private HttpRequest.Builder createRequestBuilder(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", "wangbot/1.0")
                .header("Via", ""); // Ensure Via header is not set
    }

    public String getID(String url) {
        return url.replaceAll(".*/title/([^/]+).*", "$1");
    }

    public JSONObject getMangaInfo(String mangaId) throws IOException, InterruptedException {
        String endpoint = "/manga/" + mangaId;
        URI url = apiURL.resolve(endpoint);

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = createRequestBuilder(url)
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            throw new IOException("Failed to fetch manga info: " + response.statusCode());
        }
    }

    public JSONObject searchManga(String title) throws IOException, InterruptedException {
        String endpoint = "/manga?title=" + title;
        URI url = apiURL.resolve(endpoint);

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = createRequestBuilder(url)
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            throw new IOException("Failed to search manga: " + response.statusCode());
        }
    }

    public JSONObject getAuthor (String id) throws IOException, InterruptedException {
        String endpoint = "/author/" + id;
        System.out.println(endpoint);
        URI url = apiURL.resolve(endpoint);

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = createRequestBuilder(url)
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            throw new IOException("Failed to fetch author info: " + response.statusCode());
        }
    }

    public JSONObject getArtist (String id) throws IOException, InterruptedException {
        String endpoint = "/artist/" + id;
        URI url = apiURL.resolve(endpoint);

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = createRequestBuilder(url)
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            throw new IOException("Failed to fetch author info: " + response.statusCode());
        }
    }
}