package com.marlonreina.resisas.commands.administrator;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.service.GuildService;
import com.marlonreina.resisas.utils.EmbedUtil;

import java.awt.*;
import java.util.Objects;

public class PrefixCommand implements Command {

    private final GuildService guildService;

    public PrefixCommand(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No tienes permisos para ejecutar este comando.").build()
            ).queue();
            return;
        }

        var currentPrefix = guildService
                .getOrCreate(event.getGuild().getId())
                .getPrefix();

        if (args.length < 1) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.simplyBuildMessage(
                            "Prefijo actual " + currentPrefix,
                            "Uso: " + currentPrefix + "prefix !",
                            Color.CYAN
                    ).build()
            ).queue();
            return;
        }

        String newPrefix = args[0];
        String guildId = event.getGuild().getId();

        guildService.changePrefix(guildId, newPrefix);

        event.getChannel().sendMessageEmbeds(
                EmbedUtil.success("Prefijo actualizado a: " + newPrefix).build()
        ).queue();
    }
}