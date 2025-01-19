package wangbot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLHandler extends ListenerAdapter {

    //Hyperlink regex pattern
    Pattern urlPattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

    boolean containsFix(String url) {
        return url.contains("fxtwitter.com") || url.contains("fixupx.com") || url.contains("phixiv.net");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        // We don't want to respond to other bot accounts, including ourselves
        if (event.getAuthor().isBot())
            return;

        //Read the message
        Message message = event.getMessage();
        String content = message.getContentRaw();
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)

        MessageChannel channel = event.getChannel();
        StringBuilder response = new StringBuilder();
        Set<String> uniqueLinks = new HashSet<>();

        //Regex pattern matching for hyperlinks
        Matcher urlMatcher = urlPattern.matcher(content);
        
        while (urlMatcher.find())
        {
            String url = urlMatcher.group(0);
            String fix;
            String username = "";

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
                return;
            }
            
            //Check if the link is from twitter/X
            if (url.contains("twitter.com") || url.contains("x.com")) {
                // Extract username from URL
                Pattern twitterPattern = Pattern.compile(
                        "https?://(?:www\\.)?twitter\\.com/([^/]+)/status/\\d+");
                Matcher twitterMatcher = twitterPattern.matcher(url);
                if (twitterMatcher.find()) {
                    username = twitterMatcher.group(1);
                }
                //Replace with FxTwitter embed
                fix = url.replace("twitter.com", "fxtwitter.com")
                            .replace("x.com", "fixupx.com");
            }
            //Check if the link is from pixiv
            else if (url.contains("pixiv.net")) {
                //Replace with phixiv
                fix = url.replace("pixiv.net", "phixiv.net");
            } else {
                //Ignore other websites
                continue;
            }

            // Add link to message if it's unique
            if (uniqueLinks.add(fix)) {
                if (!response.isEmpty()) {
                    response.append("\n");
                }
                if (!username.isEmpty()) {
                    response.append("[Tweet from @").append(username).append("](")
                            .append(fix).append(")");
                } else {
                    response.append(fix);
                }
            }
        }
        
        if (!response.isEmpty() && containsFix(response.toString()))
        {
            channel.sendMessage(response.toString()).queue(sentMessage ->
                    message.suppressEmbeds(true).queue());
        }
    }
}
