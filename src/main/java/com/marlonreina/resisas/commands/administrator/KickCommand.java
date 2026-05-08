package com.marlonreina.resisas.commands.administrator;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Objects;

public class KickCommand implements Command {

    @Override
    public void execute(CommandContext context) {
        var event = context.getEvent();

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.KICK_MEMBERS)) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No tienes permisos para expulsar miembros.").build()
            ).queue();
            return;
        }

        var mentioned = event.getMessage().getMentions().getMembers();
        if (mentioned.isEmpty()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("kick @usuario [razon]")).build()
            ).queue();
            return;
        }

        Member target = mentioned.get(0);
        if (!event.getGuild().getSelfMember().canInteract(target)) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No puedo expulsar a ese usuario por jerarquia de roles.").build()
            ).queue();
            return;
        }

        event.getGuild().kick(target).queue(
                success -> event.getChannel().sendMessageEmbeds(
                        EmbedUtil.success("Usuario expulsado")
                                .setDescription(target.getAsMention() + " fue expulsado del servidor.")
                                .build()
                ).queue(),
                error -> event.getChannel().sendMessageEmbeds(
                        EmbedUtil.error("No pude expulsar a ese usuario.").build()
                ).queue()
        );
    }
}
