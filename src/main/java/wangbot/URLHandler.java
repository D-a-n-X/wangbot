package wangbot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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

        //Regex pattern matching for hyperlinks
        Matcher urlMatcher = urlPattern.matcher(content);
        if (urlMatcher.find())
        {
            String url = urlMatcher.group(0);
            //Check if the link is from twitter/X
            if (url.contains("twitter.com") || url.contains("x.com")) {
                //Replace with FxTwitter embed
                String fix = url.replace("twitter.com", "fxtwitter.com")
                                .replace("x.com", "fixupx.com");
                channel.sendMessage(fix).queue(sentMessage -> {
                    //Remove embed of previous message
                    message.suppressEmbeds(true).queue();
                });
            }
            //Check if the link is from pixiv
            if (url.contains("pixiv.com")) {
                //Replace with phixiv
                String fix = url.replace("pixiv.com", "phixiv.com");
                channel.sendMessage(fix).queue(sentMessage -> {
                    message.suppressEmbeds(true).queue();
                });
            }
        }
    }
}
