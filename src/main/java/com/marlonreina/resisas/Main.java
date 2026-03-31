package com.marlonreina.resisas;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import com.marlonreina.resisas.database.DatabaseInitializer;
import com.marlonreina.resisas.listener.HelpInteractionListener;
import com.marlonreina.resisas.listener.MessageListener;

public class Main {
    public static void main(String[] args) throws Exception {

        String token = System.getenv("BOT_TOKEN");

        DatabaseInitializer.init();

        JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(new MessageListener())
            .addEventListeners(new HelpInteractionListener())
            .build();

        System.out.println("Bot encendido.");
    }
}