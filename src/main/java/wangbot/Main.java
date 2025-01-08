package wangbot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static void main(String[] args) throws Exception {

        String token = Dotenv.load().get("TOKEN");

        JDA api = JDABuilder.createDefault(token)
                .addEventListeners(new URLHandler())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

    }
}
