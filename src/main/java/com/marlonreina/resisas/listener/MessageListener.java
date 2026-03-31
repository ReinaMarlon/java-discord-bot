package com.marlonreina.resisas.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.marlonreina.resisas.commands.CommandHandler;
import com.marlonreina.resisas.utils.PrefixResolver;

public class MessageListener extends ListenerAdapter {

//    private final GuildService guildService = new GuildService();
    private final CommandHandler commandHandler = new CommandHandler();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        // Ignorar bots
        if (event.getAuthor().isBot()) return;
        // Solo en servidores
        if (!event.isFromGuild()) return;

        String detectedPrefix = PrefixResolver.resolvePrefix(event);

        // No es un comando
        if (detectedPrefix == null) return;

        commandHandler.handle(event, detectedPrefix);
    }
}