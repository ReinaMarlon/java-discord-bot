package com.marlonreina.resisas.commands.administrator;

import com.marlonreina.resisas.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class KickCommand implements Command {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            event.getChannel().sendMessage("No tienes permisos.").queue();
            return;
        }

        var mentioned = event.getMessage().getMentions().getMembers();

        if (mentioned.isEmpty()) {
            event.getChannel().sendMessage("Menciona un usuario.").queue();
            return;
        }

        event.getGuild().kick(mentioned.get(0)).queue();

        event.getChannel().sendMessage("Usuario expulsado.").queue();
    }
}