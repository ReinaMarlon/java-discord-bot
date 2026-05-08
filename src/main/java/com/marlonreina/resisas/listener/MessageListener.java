package com.marlonreina.resisas.listener;

import com.marlonreina.resisas.commands.CommandHandler;
import com.marlonreina.resisas.utils.PrefixResolver;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class MessageListener extends ListenerAdapter {

    private final CommandHandler commandHandler;
    private final PrefixResolver prefixResolver;

    public MessageListener(CommandHandler commandHandler, PrefixResolver prefixResolver) {
        this.commandHandler = commandHandler;
        this.prefixResolver = prefixResolver;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }
        if (!event.isFromGuild()) {
            return;
        }

        String triggerPrefix = prefixResolver.resolvePrefix(event);

        if (triggerPrefix == null) {
            return;
        }

        String displayPrefix = prefixResolver.getPrefix(event.getGuild().getId());
        commandHandler.handle(event, triggerPrefix, displayPrefix);
    }
}
