package wangbot.main;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import wangbot.api.DexAPIHandler;

public class CommandHandler extends ListenerAdapter {
    private final DexAPIHandler dexAPIHandler = new DexAPIHandler();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = event.getChannel();

        //Ping command
        if (content.startsWith("!ping")) {
            channel.sendMessage("Pong!").queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "ping":
                event.reply("Pong!").queue();
            case "manga":
                event.deferReply().queue(); //Acknowledges the command has been received
                String url = event.getOption("url").getAsString();
                EmbedBuilder embedBuilder = dexAPIHandler.generateDexEmbed(url);
                event.replyEmbeds(embedBuilder.build()).queue();
        }
    }
}
