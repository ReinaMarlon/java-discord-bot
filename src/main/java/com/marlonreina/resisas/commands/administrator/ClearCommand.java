package com.marlonreina.resisas.commands.administrator;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.marlonreina.resisas.commands.Command;

import java.util.Objects;

public class ClearCommand implements Command {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getChannel().sendMessage("No tienes permisos.").queue();
            return;
        }

        int amount = 10;

        if (args.length >= 1) {
            amount = Integer.parseInt(args[0]);
        }

        event.getChannel().getHistory().retrievePast(amount)
                .queue(messages -> {
                    event.getChannel().purgeMessages(messages);
                });
    }
}