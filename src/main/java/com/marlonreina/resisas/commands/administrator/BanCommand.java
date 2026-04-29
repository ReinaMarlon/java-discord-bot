package com.marlonreina.resisas.commands.administrator;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BanCommand implements Command {

    @Override
    public void execute(CommandContext context) {
        var event = context.getEvent();

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.BAN_MEMBERS)) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No tienes permisos para banear miembros.").build()
            ).queue();
            return;
        }

        if (context.getArgs().length < 1) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("ban @usuario [razon]")).build()
            ).queue();
            return;
        }

        var mentioned = event.getMessage().getMentions().getMembers();
        if (mentioned.isEmpty()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("Menciona un usuario valido.").build()
            ).queue();
            return;
        }

        Member target = mentioned.get(0);
        if (!event.getGuild().getSelfMember().canInteract(target)) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No puedo banear a ese usuario por jerarquia de roles.").build()
            ).queue();
            return;
        }

        event.getGuild().ban(target.getUser(), 0, TimeUnit.DAYS).queue(
                success -> event.getChannel().sendMessageEmbeds(
                        EmbedUtil.success("Usuario baneado")
                                .setDescription(target.getAsMention() + " fue baneado del servidor.")
                                .build()
                ).queue(),
                error -> event.getChannel().sendMessageEmbeds(
                        EmbedUtil.error("No pude banear a ese usuario.").build()
                ).queue()
        );
    }
}
