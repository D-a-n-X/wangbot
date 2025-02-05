package wangbot.main;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Main {
    public static void main(String[] args) {
        String token = System.getenv("TOKEN");

        JDA api = JDABuilder.createDefault(token)
                .addEventListeners(new URLHandler())
                .addEventListeners(new CommandHandler())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

        // You might need to reload your Discord client if you don't see the commands
        CommandListUpdateAction commands = api.updateCommands();

        commands.addCommands(
                Commands.slash("manga", "Generates a nice embed for MangaDex mangas")
                        .addOption(OptionType.STRING, "url", "The URL of the manga", true)
        );

        // Send the new set of commands to discord, this will override any existing global commands with the new set provided here
        commands.queue();
    }
}
