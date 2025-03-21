package wangbot.api;

import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import wangbot.utils.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

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

    public EmbedBuilder generateDexEmbed(String url) {
        // Extract manga ID from URL
        String mangaId = getID(url);

        // Fetch JSON response from MangaDex API
        JSONObject dexResponse;
        try {
            dexResponse = getMangaInfo(mangaId);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Start of attribute fetching for embed builder

        // Get data object
        JSONObject data = dexResponse.getJSONObject("data");

        // Get attribute object
        JSONObject attributes = data.getJSONObject("attributes");

        // Extract full manga title
        String title = attributes.getJSONObject("title")
                .getString("en");

        // Create link to manga
        String mangaUrl = "https://mangadex.org/title/" + mangaId;

        // Extract description
        String description = attributes.getJSONObject("description")
                .getString("en");
        if (description.isEmpty()) {
            description = "No description available.";
        }

        // Extract and get author(s) and artist(s)
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<String> artists = new ArrayList<>();

        JSONArray relationships = data.getJSONArray("relationships");
        for (int i = 0; i < relationships.length(); i++) {
            JSONObject relationship = relationships.getJSONObject(i);
            String type = relationship.getString("type");
            String id = relationship.getString("id");
            if (type.equals("author")) {
                try {
                    authors.add(getAuthor(id)
                            .getJSONObject("data")
                            .getJSONObject("attributes")
                            .getString("name"));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if (type.equals("artist")) {
                try {
                    artists.add(getArtist(id)
                            .getJSONObject("data")
                            .getJSONObject("attributes")
                            .getString("name"));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Extract tags
        ArrayList<String> tags = new ArrayList<>();
        JSONArray tagsArray = attributes.getJSONArray("tags");
        for (int i = 0; i < tagsArray.length(); i++) {
            JSONObject tagObject = tagsArray.getJSONObject(i);
            String tagName = tagObject.getJSONObject("attributes")
                    .getJSONObject("name")
                    .getString("en");
            tags.add(tagName);
        }

        // Extract publication year and status
        int year = 0;
        if (attributes.getJSONObject("year") != null) {
            year = attributes.getInt("year");
        }
        String status = attributes.getString("status");

        // Extract content rating
        String contentRating = attributes.getString("contentRating");

        // Get banner image
        String bannerUrl = "https://og.mangadex.org/og-image/manga/" + mangaId;

        // Get statistics
        JSONObject ratings;
        try {
            ratings = getStatistics(mangaId)
                    .getJSONObject("statistics")
                    .getJSONObject(mangaId)
                    .getJSONObject("rating");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        double mean = ratings.getDouble("average");
        double bayesian = ratings.getDouble("bayesian");
        // End of attribute fetching

        // Create embed
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder
                .setAuthor("MangaDex")
                .setTitle(title, mangaUrl)
                .setColor(new Color(255, 103, 64))
                .addField("Author", String.join(", ", authors), true)
                .addField("Artist", String.join(", ", artists), true)
                .addField("☆" + String.format("%.2f", bayesian), "Mean rating\n☆" + String.format("%.2f", mean), true)
                .addField("Tags", String.join(", ", tags), true);

        //Checks if the year is added
        if (year != 0)
            embedBuilder.addField("Publication Status", String.valueOf(year).concat(", " + StringUtils.capitalize(status)), true);
        else
            embedBuilder.addField("Publication Status", StringUtils.capitalize(status), true);

        embedBuilder
                .addField("Description", description, false)
                .setImage(bannerUrl)
                .setFooter("Content Rating: " + StringUtils.capitalize(contentRating), "https://mangadex.org/favicon.ico");

        return embedBuilder;
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

    public JSONObject getAuthor(String id) throws IOException, InterruptedException {
        String endpoint = "/author/" + id;
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

    public JSONObject getArtist(String id) throws IOException, InterruptedException {
        String endpoint = "/author/" + id;
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
            throw new IOException("Failed to fetch artist info: " + response.statusCode());
        }
    }

    public JSONObject getStatistics(String id) throws IOException, InterruptedException {
        String endpoint = "/statistics/manga/" + id;
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
            throw new IOException("Failed to fetch manga statistics: " + response.statusCode());
        }
    }
}