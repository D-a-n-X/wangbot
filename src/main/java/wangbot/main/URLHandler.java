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

//Handles messages containing URLs and responses to those messages
public class URLHandler extends ListenerAdapter {

    //Hyperlink regex pattern
    private static final Pattern urlPattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    private static final Pattern twitterPattern = Pattern.compile("https?://(?:www\\.)?(twitter|x)\\.com/([^/]+)/status/\\d+");
    private static final Pattern pixivPattern = Pattern.compile("https?://(?:www\\.)?pixiv\\.net/(?:en/)?artworks/\\d+");
    private static final Pattern dexPattern = Pattern.compile("https?://(?:www\\.)?mangadex\\.org/title/[^/]+/[^/]+");

    private final PixivAPIHandler pixivAPIHandler = new PixivAPIHandler();
    private final DexAPIHandler dexAPIHandler = new DexAPIHandler();

    boolean containsFix(String url) {
        return url.contains("fxtwitter.com") || url.contains("fixupx.com") || url.contains("phixiv.net");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // We don't want to respond to other bot accounts, including ourselves
        if (event.getAuthor().isBot())
            return;

        //Read the message
        Message message = event.getMessage();
        String content = message.getContentRaw();
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)

        MessageChannel channel = event.getChannel();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        StringBuilder response = new StringBuilder();
        Set<String> uniqueLinks = new HashSet<>();

        //Regex pattern matching for hyperlinks
        Matcher urlMatcher = urlPattern.matcher(content);

        while (urlMatcher.find()) {
            String url = urlMatcher.group(0);
            String fix = "";
            String twitterUsername = "";
            String pixivUsername = "";

            // Validate URL
            try {
                URI uri = new URI(url);
                // Remove tracking parameters
                url = uri.getScheme() + "://" + uri.getHost() + uri.getPath();
            } catch (URISyntaxException e) {
                continue; // Skip invalid URLs
            }

            //Check if the link is already fixed
            if (containsFix(url)) {
                continue;
            }

            Matcher twitterMatcher = twitterPattern.matcher(url);
            Matcher pixivMatcher = pixivPattern.matcher(url);
            Matcher dexMatcher = dexPattern.matcher(url);

            //Check if the link is from twitter/X
            if (twitterMatcher.find()) {

                // Extract username from URL
                twitterUsername = twitterMatcher.group(2);

                //Replace with fxtwitter/fixupx
                fix = url.replace("twitter.com", "fxtwitter.com")
                        .replace("x.com", "fixupx.com");
            }

            //Check if the link is from pixiv
            else if (pixivMatcher.find()) {

                // Fetch JSON response from Pixiv API
                JSONObject pixivResponse;
                try {
                    pixivResponse = pixivAPIHandler.fetchJson(url);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }

                //Check for AI generated artwork
                if (pixivAPIHandler.isAIGenerated(pixivResponse)) {
                    fix = "[This artwork is AI generated](<" + url + ">)";
                } else {

                    //Replace with phixiv
                    pixivUsername = pixivAPIHandler.getUserName(pixivResponse);
                    fix = url.replace("pixiv.net", "phixiv.net");
                }
            }

            //Check if the link is from MangaDex
            else if (dexMatcher.find()) {
                embedBuilder = dexAPIHandler.generateDexEmbed(url);
            }

            // Add link to message if it's unique
            if (fix != null && uniqueLinks.add(fix)) {
                if (!response.isEmpty()) {
                    response.append("\n");
                }
                if (!twitterUsername.isEmpty()) {
                    response.append("[Tweet ▸ @").append(twitterUsername).append("](").append(fix).append(")");
                } else if (!pixivUsername.isEmpty()) {
                    response.append("[Artwork ▸ ").append(pixivUsername).append("](").append(fix).append(")");
                } else {
                    response.append(fix);
                }
            }
        }

        if (!response.isEmpty()) {
            channel.sendMessage(response.toString()).queue((sentMessage) -> {
                message.suppressEmbeds(true).queue();
            });
        }

        if (!embedBuilder.isEmpty()) {
            MessageEmbed embed = embedBuilder.build();
            // Create a MessageCreateBuilder and add the embed
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
            messageBuilder.setEmbeds(embed);

            // Send the message
            channel.sendMessage(messageBuilder.build()).queue((sentMessage) -> {
                message.suppressEmbeds(true).queue();
            });
        }
    }
}
