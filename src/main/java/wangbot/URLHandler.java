package wangbot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLHandler extends ListenerAdapter {

    //Hyperlink regex pattern
    Pattern urlPattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        // We don't want to respond to other bot accounts, including ourselves
        if (event.getAuthor().isBot())
            return;

        Message message = event.getMessage();
        String content = message.getContentRaw();
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)

        MessageChannel channel = event.getChannel();
        StringBuilder response = new StringBuilder();

        //Regex pattern matching for hyperlinks
        Matcher urlMatcher = urlPattern.matcher(content);
        
        while (urlMatcher.find())
        {
            String url = urlMatcher.group(0);
            String fix = url;

            // Validate URL
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                continue; // Skip invalid URLs
            }

            //Check if the link is already fixed
            if (url.contains("fxtwitter.com") || url.contains("fixupx.com") || url.contains("phixiv.net")) {
                continue;
            }
            
            //Check if the link is from twitter/X
            if (url.contains("twitter.com") || url.contains("x.com")) {
                //Replace with FxTwitter embed
                fix = url.replace("twitter.com", "fxtwitter.com")
                            .replace("x.com", "fixupx.com");
            }
            //Check if the link is from pixiv
            if (url.contains("pixiv.net")) {
                //Replace with phixiv
                fix = url.replace("pixiv.net", "phixiv.net");
            }
            
            urlMatcher.appendReplacement(response, fix);
        }
        urlMatcher.appendTail(response);
        
        if (!response.isEmpty())
        {
            channel.sendMessage(response.toString()).queue(sentMessage -> {
                message.suppressEmbeds(true).queue();
            });
        }
    }
}
