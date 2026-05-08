package com.marlonreina.resisas.commands.administrator;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.model.WelcomeConfig;
import com.marlonreina.resisas.service.WelcomeConfigService;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;
import java.util.Locale;
import java.util.Objects;

public class WelcomeCommand implements Command {

    private final WelcomeConfigService welcomeConfigService;
    private final String configUrl;

    public WelcomeCommand(WelcomeConfigService welcomeConfigService,
                          String configUrl) {
        this.welcomeConfigService = welcomeConfigService;
        this.configUrl = configUrl;
    }

    @Override
    public void execute(CommandContext context) {
        MessageReceivedEvent event = context.getEvent();
        String[] args = context.getArgs();

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No tienes permisos para ejecutar este comando.").build()
            ).queue();
            return;
        }

        if (args.length == 0) {
            sendMenu(event, context.getPrefix());
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "setchannel" -> setChannel(context);
            case "enable" -> enableWelcome(context);
            case "configmessage" -> configMessage(context);
            default -> sendMenu(event, context.getPrefix());
        }
    }

    private void sendMenu(MessageReceivedEvent event, String prefix) {
        WelcomeConfig config = welcomeConfigService.getOrCreate(event.getGuild().getId());
        String channel = config.getChannelId() == null
                ? "No configurado"
                : "<#" + config.getChannelId() + ">";
        String mode = config.getMessageMode();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(0x57F287));
        embed.setTitle("Welcome");
        embed.setDescription("Configura los mensajes de bienvenida para los nuevos miembros del servidor.");
        embed.addField("Canal", channel, true);
        embed.addField("Estado", config.isEnabled() ? "Activado" : "Desactivado", true);
        embed.addField("Mensaje", mode, true);
        embed.addField("Uso", "`" + prefix + "welcome setchannel {id del canal}`\n"
                + "`" + prefix + "welcome enable {true o false}`\n"
                + "`" + prefix + "welcome configmessage {simply o complex}`", false);
        embed.addField("Descripcion", "Usa `simply` para un mensaje prearmado sencillo o `complex` para recibir "
                + "la URL de configuracion web.", false);
        embed.setFooter("Hexa - " + event.getGuild().getName());

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private void setChannel(CommandContext context) {
        MessageReceivedEvent event = context.getEvent();
        String[] args = context.getArgs();

        if (args.length < 2) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("welcome setchannel {id del canal}")).build()
            ).queue();
            return;
        }

        String channelId = normalizeChannelId(args[1]);
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        if (channel == null) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No encontre un canal de texto con ese id.").build()
            ).queue();
            return;
        }

        welcomeConfigService.changeChannel(event.getGuild().getId(), channelId);
        event.getChannel().sendMessageEmbeds(
                EmbedUtil.success("Canal de bienvenida actualizado: #" + channel.getName()).build()
        ).queue();
    }

    private void enableWelcome(CommandContext context) {
        MessageReceivedEvent event = context.getEvent();
        String[] args = context.getArgs();

        if (args.length < 2 || !isBooleanValue(args[1])) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("welcome enable {true o false}")).build()
            ).queue();
            return;
        }

        boolean enabled = Boolean.parseBoolean(args[1]);
        welcomeConfigService.changeEnabled(event.getGuild().getId(), enabled);
        event.getChannel().sendMessageEmbeds(
                EmbedUtil.success("Welcome " + (enabled ? "activado." : "desactivado.")).build()
        ).queue();
    }

    private void configMessage(CommandContext context) {
        MessageReceivedEvent event = context.getEvent();
        String[] args = context.getArgs();

        if (args.length < 2) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("welcome configmessage {simply o complex}")).build()
            ).queue();
            return;
        }

        String mode = args[1].toLowerCase(Locale.ROOT);
        if ("simply".equals(mode)) {
            welcomeConfigService.changeMessageMode(event.getGuild().getId(), mode);
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.success("Mensaje simple de bienvenida configurado.").build()
            ).queue();
            return;
        }

        if ("complex".equals(mode)) {
            welcomeConfigService.changeMessageMode(event.getGuild().getId(), mode);
            String guildUrl = configUrl + "?guildId=" + event.getGuild().getId();
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.info("Configura tu welcome en la web").setDescription(guildUrl).build()
            ).addActionRow(Button.link(guildUrl, "Abrir configuracion")).queue();
            return;
        }

        event.getChannel().sendMessageEmbeds(
                EmbedUtil.error("Modo invalido. Usa simply o complex.").build()
        ).queue();
    }

    private String normalizeChannelId(String rawChannelId) {
        return rawChannelId.replace("<#", "").replace(">", "");
    }

    private boolean isBooleanValue(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }
}
