package com.marlonreina.resisas.config;

import com.marlonreina.resisas.listener.HelpInteractionListener;
import com.marlonreina.resisas.listener.MessageListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Bean
    public net.dv8tion.jda.api.JDA jda(HelpInteractionListener helpListener,
                                       MessageListener messageListener) throws Exception {

        String token = System.getenv("BOT_TOKEN");

        net.dv8tion.jda.api.JDA jda = JDABuilder.createDefault(token,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(helpListener)
                .addEventListeners(messageListener)
                .build()
                .awaitReady();

        System.out.println("Bot encendido correctamente.");

        return jda;
    }
}