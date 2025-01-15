package wangbot;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static void main(String[] args) {
        String token = System.getenv("TOKEN");

        JDA api = JDABuilder.createDefault(token)
                .addEventListeners(new URLHandler())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
    }
}
