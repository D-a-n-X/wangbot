package wangbot.main;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.json.JSONObject;
import wangbot.api.DexAPIHandler;
import wangbot.api.PixivAPIHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles messages containing URLs and responds with fixed/transformed links
 */
public class URLHandler extends ListenerAdapter {
    private static final Logger LOGGER = Logger.getLogger(URLHandler.class.getName());

    // URL patterns
    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    private static final Pattern TWITTER_PATTERN = Pattern.compile("https?://(?:www\\.)?(twitter|x)\\.com/([^/]+)/status/\\d+");
    private static final Pattern PIXIV_PATTERN = Pattern.compile("https?://(?:www\\.)?pixiv\\.net/(?:en/)?artworks/\\d+");
    private static final Pattern DEX_PATTERN = Pattern.compile("https?://(?:www\\.)?mangadex\\.org/title/[^/]+/[^/]+");
    private static final Pattern FACEBOOK_PATTERN = Pattern.compile("https?://(?:www\\.)?facebook\\.com/([^/]+|groups/[^/]+|story\\.php\\?story_fbid|permalink\\.php\\?story_fbid|share/p/[^/]+).*");
    private static final Pattern INSTAGRAM_PATTERN = Pattern.compile("https?://(?:www\\.)?instagram\\.com/([^/]+).*");

    // Domain constants
    private static final String TWITTER_DOMAIN = "twitter.com";
    private static final String X_DOMAIN = "x.com";
    private static final String FXTWITTER_DOMAIN = "fxtwitter.com";
    private static final String FIXUPX_DOMAIN = "fixupx.com";
    private static final String VXTWITTER_DOMAIN = "vxtwitter.com";
    private static final String FIXVX_DOMAIN = "fixvx.com";
    private static final String PIXIV_DOMAIN = "pixiv.net";
    private static final String PHIXIV_DOMAIN = "phixiv.net";
    private static final String FACEBOOK_DOMAIN = "facebook.com";
    private static final String FACEBED_DOMAIN = "facebed.com";
    private static final String INSTAGRAM_DOMAIN = "instagram.com";
    private static final String INSTAGRAMEZ_DOMAIN = "instagramez.com";

    private final PixivAPIHandler pixivAPIHandler;
    private final DexAPIHandler dexAPIHandler;

    public URLHandler() {
        this.pixivAPIHandler = new PixivAPIHandler();
        this.dexAPIHandler = new DexAPIHandler();
    }

    // Constructor for testing purposes
    URLHandler(PixivAPIHandler pixivAPIHandler, DexAPIHandler dexAPIHandler) {
        this.pixivAPIHandler = pixivAPIHandler;
        this.dexAPIHandler = dexAPIHandler;
    }

    /**
     * Checks if the URL is already a fixed version
     */
    boolean containsFix(String url) {
        return  url.contains(FXTWITTER_DOMAIN) ||
                url.contains(FIXUPX_DOMAIN) ||
                url.contains(VXTWITTER_DOMAIN) ||
                url.contains(FIXVX_DOMAIN) ||
                url.contains(PHIXIV_DOMAIN) ||
                url.contains(FACEBED_DOMAIN) ||
                url.contains(INSTAGRAMEZ_DOMAIN);
    }

    /**
     * Converts Facebook URLs to Facebed URLs
     */
    public String convertToFacebed(String url) {
        return url.replace(FACEBOOK_DOMAIN, FACEBED_DOMAIN);
    }

    /**
     * Converts Instagram URLs to Instagramez URLs
     */
    public String convertToInstagramez(String url) {
        return url.replace(INSTAGRAM_DOMAIN, INSTAGRAMEZ_DOMAIN);
    }

    /**
     * Converts Twitter/X URLs to fixed versions
     */
    private String convertTwitterUrl(String url) {
        return url.replace(TWITTER_DOMAIN, FXTWITTER_DOMAIN)
                 .replace(X_DOMAIN, FIXUPX_DOMAIN);
    }
    private String reconvertTwitterUrl(String url) {
        return url.replace(TWITTER_DOMAIN, VXTWITTER_DOMAIN)
                .replace(X_DOMAIN, FIXVX_DOMAIN);
    }

    /**
     * Converts Pixiv URLs to Phixiv URLs
     */
    private String convertPixivUrl(String url) {
        return url.replace(PIXIV_DOMAIN, PHIXIV_DOMAIN);
    }

    /**
     * Sanitizes a URL by removing tracking parameters
     */
    private String sanitizeUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() + "://" + uri.getHost() + uri.getPath();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "Invalid URL: " + url, e);
            return url; // Return original if parsing fails
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Skip messages from bots
        if (event.getAuthor().isBot()) return;

        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = event.getChannel();

        // Process links
        ProcessedLinks processedLinks = processLinks(content);

        // Send processed text links if any
        if (!processedLinks.textResponse.isEmpty()) {
            channel.sendMessage(processedLinks.textResponse.toString()).queue(
                sentMessage -> message.suppressEmbeds(true).queue()
            );
        }

        // Send embed if any
        if (!processedLinks.embedBuilder.isEmpty()) {
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
            messageBuilder.setEmbeds(processedLinks.embedBuilder.build());

            channel.sendMessage(messageBuilder.build()).queue(
                sentMessage -> message.suppressEmbeds(true).queue()
            );
        }
    }

    /**
     * Container class for processed links results
     */
    private static class ProcessedLinks {
        final StringBuilder textResponse = new StringBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        final Set<String> uniqueLinks = new HashSet<>();
    }

    /**
     * Process links in the message content
     */
    ProcessedLinks processLinks(String content) {
        ProcessedLinks result = new ProcessedLinks();
        Matcher urlMatcher = URL_PATTERN.matcher(content);

        while (urlMatcher.find()) {
            String url = urlMatcher.group(0);
            url = sanitizeUrl(url);

            // Skip already fixed links
            if (containsFix(url)) continue;

            processUrl(url, result);
        }

        return result;
    }

    /**
     * Process a single URL and update the result accordingly
     */
    private void processUrl(String url, ProcessedLinks result) {
        String fixedUrl = null;
        String username = null;

        if (TWITTER_PATTERN.matcher(url).find()) {
            Matcher matcher = TWITTER_PATTERN.matcher(url);
            if (matcher.find()) {
                username = matcher.group(2);
                fixedUrl = reconvertTwitterUrl(url);
                addLinkToResponse(result, fixedUrl, "Tweet ▸ @" + username);
            }
        } else if (PIXIV_PATTERN.matcher(url).find()) {
            processPixivUrl(url, result);
        } else if (DEX_PATTERN.matcher(url).find()) {
            result.embedBuilder = dexAPIHandler.generateDexEmbed(url);
        } else if (FACEBOOK_PATTERN.matcher(url).find()) {
            fixedUrl = convertToFacebed(url);
            addLinkToResponse(result, fixedUrl, "Facebook Post");
        } else if (INSTAGRAM_PATTERN.matcher(url).find()) {
            fixedUrl = convertToInstagramez(url);
            addLinkToResponse(result, fixedUrl, "Instagram Post");
        }
    }

    /**
     * Process Pixiv URLs with API call
     */
    private void processPixivUrl(String url, ProcessedLinks result) {
        try {
            JSONObject pixivResponse = pixivAPIHandler.fetchJson(url);

            if (pixivAPIHandler.isAIGenerated(pixivResponse)) {
                String aiGeneratedLink = "[This artwork is AI generated](<" + url + ">)";
                addLinkToResponse(result, aiGeneratedLink, null);
            } else {
                String username = pixivAPIHandler.getUserName(pixivResponse);
                String fixedUrl = convertPixivUrl(url);
                addLinkToResponse(result, fixedUrl, "Artwork ▸ " + username);
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.WARNING, "Error fetching Pixiv data for URL: " + url, e);
        }
    }

    /**
     * Add a link to the response, with optional context prefix
     */
    private void addLinkToResponse(ProcessedLinks result, String link, String prefix) {
        if (result.uniqueLinks.add(link)) {
            if (!result.textResponse.isEmpty()) {
                result.textResponse.append("\n");
            }

            if (prefix != null && !prefix.isEmpty()) {
                result.textResponse.append("[").append(prefix).append("](").append(link).append(")");
            } else {
                result.textResponse.append(link);
            }
        }
    }
}
