package com.marlonreina.resisas.commands.administrator;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.service.GuildService;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;

import java.awt.Color;
import java.util.Objects;

public class PrefixCommand implements Command {

    private final GuildService guildService;

    public PrefixCommand(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void execute(CommandContext context) {
        var event = context.getEvent();

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No tienes permisos para cambiar el prefijo.").build()
            ).queue();
            return;
        }

        if (context.getArgs().length < 1) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.simplyBuildMessage(
                            "Prefijo actual",
                            "Prefijo: `" + context.getPrefix() + "`\nUso: `"
                                    + context.usage("prefix <nuevo>") + "`",
                            Color.CYAN
                    ).build()
            ).queue();
            return;
        }

        String newPrefix = context.getArgs()[0];
        guildService.changePrefix(event.getGuild().getId(), newPrefix);

        event.getChannel().sendMessageEmbeds(
                EmbedUtil.success("Prefijo actualizado")
                        .setDescription("Nuevo prefijo: `" + newPrefix + "`")
                        .build()
        ).queue();
    }
}
