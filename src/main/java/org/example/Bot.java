package org.example;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

/**
 * Hello world!
 *
 */
public class Bot
{
    public static void main( String[] args )
    {
        DiscordClient client = DiscordClient.create(Token.getToken());

        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
            Mono<Void> handlePingCommand = gateway.on(MessageCreateEvent.class, event -> {
            Message message = event.getMessage();

            if (message.getContent().equalsIgnoreCase("!ping")) {
                return message.getChannel()
                        .flatMap(channel -> channel.createMessage("pong!"));
            }

            return Mono.empty();
        }).then();

        return handlePingCommand;});

        login.block();
    }
}
