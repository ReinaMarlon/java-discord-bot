package com.marlonreina.resisas.commands.administrator;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.service.LogService;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;

import java.awt.Color;
import java.util.Objects;

public class LogCommand implements Command {

    private final LogService logService;

    public LogCommand(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void execute(CommandContext context) {

        var event = context.getEvent();
        var args = context.getArgs();

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_SERVER)) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No tienes permisos para ejecutar este comando.").build()
            ).queue();
            return;
        }

        String guildId = event.getGuild().getId();
        var config = logService.getConfig(guildId);

        String canalActual = config
                .map(c -> c.getChannelId() != null ? "<#" + c.getChannelId() + ">" : "No configurado")
                .orElse("No configurado");

        String estadoActual = config
                .map(c -> Boolean.TRUE.equals(c.getEnabled()) ? "Activado" : "Desactivado")
                .orElse("Desactivado");

        if (args.length < 1) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.simplyBuildMessage(
                            "Logs",
                            "Configura el canal de logs del servidor.\n\n" +
                                    "**Canal:** " + canalActual + "\n" +
                                    "**Estado:** " + estadoActual + "\n\n" +
                                    "**Uso**\n" +
                                    "`" + context.usage("logs setchannel #canal") + "`\n" +
                                    "`" + context.usage("logs enable true/false") + "`\n\n" +
                                    "**Descripción**\n" +
                                    "El canal de logs registra automáticamente eventos relevantes del servidor.",
                            Color.CYAN
                    ).build()
            ).queue();
            return;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("setchannel")) {
            var mentioned = event.getMessage().getMentions().getChannels();

            if (mentioned.isEmpty()) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtil.error("Menciona un canal válido. Ej: `" + context.usage("logs setchannel #logs") + "`").build()
                ).queue();
                return;
            }

            String channelId = mentioned.get(0).getId();
            logService.setLogChannel(guildId, channelId);

            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.success("Canal de logs actualizado: <#" + channelId + ">").build()
            ).queue();
            return;
        }

        if (subCommand.equals("enable")) {
            if (args.length < 2) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtil.error("Uso: `" + context.usage("logs enable true/false") + "`").build()
                ).queue();
                return;
            }

            String value = args[1].toLowerCase();

            if (!value.equals("true") && !value.equals("false")) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtil.error("El valor debe ser `true` o `false`.").build()
                ).queue();
                return;
            }

            boolean enabled = Boolean.parseBoolean(value);
            logService.setEnabled(guildId, enabled);

            String estado = enabled ? "activado ✅" : "desactivado ❌";
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.success("Sistema de logs " + estado).build()
            ).queue();
            return;
        }

        event.getChannel().sendMessageEmbeds(
                EmbedUtil.error("Subcomando no reconocido. Usa `" + context.usage("logs") + "` para ver las opciones.").build()
        ).queue();
    }
}