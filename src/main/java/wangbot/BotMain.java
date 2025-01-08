package wangbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class BotMain {
    public static void main(String[] args) throws Exception {

        String token = System.getenv("TOKEN");

        JDA api = JDABuilder.createDefault(token).build();


    }
}
