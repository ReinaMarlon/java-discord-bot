package com.marlonreina.resisas.commands.administrator;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.marlonreina.resisas.commands.Command;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BanCommand implements Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        if(Objects.requireNonNull(event.getMember()).hasPermission(Permission.BAN_MEMBERS)) {
            event.getChannel().sendMessage("No tienes permisos para realizar esta acción.").queue();
            return;
        }

        if (args.length < 1) {
            event.getChannel().sendMessage("Uso: -ban @member").queue();
            return;
        }

        var mentioned = event.getMessage().getMentions().getMembers();

        if (mentioned.isEmpty()) {
            event.getChannel().sendMessage("Menciona un usuario válido.").queue();
            return;
        }

        var target = mentioned.getFirst();

        if (!event.getGuild().getSelfMember().canInteract(target)) {
            event.getChannel().sendMessage("No puedo banear a ese usuario.").queue();
            return;
        }

        event.getGuild()
                .ban(target.getUser(), 0, TimeUnit.DAYS)
                .queue();

        event.getChannel().sendMessage("Usuario baneado.").queue();

    }
}
