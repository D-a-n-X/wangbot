package wangbot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PixivAPIHandler {

    public JSONObject fetchJson(String url)
            throws IOException, URISyntaxException, JSONException {
        // Extract artwork ID from URL
        String id = url.replaceAll(".*/artworks/(\\d+).*", "$1");

        // Construct API URL
        URI apiUri = new URI("https", "www.pixiv.net", "/ajax/illust/" + id, null);

        // Fetch JSON response from Pixiv API
        HttpURLConnection connection = (HttpURLConnection) apiUri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        Scanner scanner = new Scanner(connection.getInputStream());
        String response = scanner.useDelimiter("\\A").next();
        scanner.close();

        // Parse JSON response
        return new JSONObject(response);
    }

    public boolean isAIGenerated(String url) {
        try {
            // Parse JSON response
            JSONObject jsonResponse = fetchJson(url);
            JSONObject body = jsonResponse.getJSONObject("body");
            JSONObject tags = body.getJSONObject("tags");
            JSONArray tagsList = tags.getJSONArray("tags");

            // Check for "AI-Generated" tag
            for (int i = 0; i < Math.min(2,tagsList.length()); i++) {
                String tag = tagsList.getJSONObject(i).getString("tag");
                if (tag.equals("AI生成")) {
                    return true;
                }
            }
        } catch (Exception e) {
            if (e instanceof IOException ||
                    e instanceof JSONException ||
                    e instanceof URISyntaxException) {
                e.printStackTrace();
            } else {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public String getUserName(String url) {
        try {
            JSONObject jsonResponse = fetchJson(url);
            JSONObject body = jsonResponse.getJSONObject("body");
            return body.getString("userName");
        } catch (Exception e) {
            if (e instanceof IOException ||
                    e instanceof JSONException ||
                    e instanceof URISyntaxException) {
                e.printStackTrace();
            } else {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}