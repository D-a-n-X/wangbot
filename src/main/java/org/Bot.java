package org;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import io.github.cdimascio.dotenv.Dotenv;
import reactor.core.publisher.Mono;

/**
 * Hello world!
 *
 */
public class Bot
{
    public static void main( String[] args )
    {
        String token = Dotenv.configure()
                .directory("C:\\Users\\Dan X\\IdeaProjects\\wangbot\\").filename("token.env")
                .load().get("TOKEN");

        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build()
                .login()
                .block();

        client.onDisconnect().block();
    }
}
