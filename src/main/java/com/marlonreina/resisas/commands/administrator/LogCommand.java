package com.marlonreina.resisas.commands.administrator;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.service.LogService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class LogCommand implements Command {

    private final LogService logService;

    public LogCommand(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_SERVER)) {
            event.getChannel().sendMessage("No tienes permisos para realizar esta acción.").queue();
            return;
        }

        if (args.length < 1) {
            event.getChannel().sendMessage("Uso: !setlog #canal").queue();
            return;
        }

        var mentioned = event.getMessage().getMentions().getChannels();

        if (mentioned.isEmpty()) {
            event.getChannel().sendMessage("Menciona un canal válido. Ej: !setlog #logs").queue();
            return;
        }

        String channelId = mentioned.get(0).getId();
        String guildId = event.getGuild().getId();

        logService.setLogChannel(guildId, channelId);

        event.getChannel().sendMessage("✅ Canal de logs configurado correctamente.").queue();
    }
}