package com.marlonreina.resisas.commands.misc;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class HelpCommand implements Command {

    public static void sendMainMenu(MessageReceivedEvent event, String prefix) {
        event.getChannel().sendMessageEmbeds(getEmbedBuilder(event, prefix).build())
                .addActionRow(buildMenu())
                .queue();
    }

    private static StringSelectMenu buildMenu() {
        return StringSelectMenu.create("help:select")
                .setPlaceholder("Selecciona un comando...")
                .addOption("ping", "help:cmd:ping", "Comprueba la latencia del bot")
                .addOption("prefix", "help:cmd:prefix", "Cambia el prefijo del servidor")
                .addOption("help", "help:cmd:help", "Muestra esta ayuda")
                .addOption("welcome", "help:cmd:welcome", "Configura mensajes de bienvenida")
                .addOption("clear", "help:cmd:clear", "Elimina mensajes del canal")
                .addOption("kick", "help:cmd:kick", "Expulsa a un miembro")
                .addOption("ban", "help:cmd:ban", "Banea a un miembro")
                .addOption("economy", "help:cmd:economy", "Menu de economia")
                .addOption("balance", "help:cmd:balance", "Consulta balances")
                .addOption("daily", "help:cmd:daily", "Recompensa diaria")
                .addOption("pay", "help:cmd:pay", "Transfiere monedas")
                .addOption("music", "help:cmd:music", "Menu de musica")
                .addOption("play", "help:cmd:play", "Reproduce musica")
                .addOption("queue", "help:cmd:queue", "Cola de musica")
                .addOption("skip", "help:cmd:skip", "Salta pistas")
                .addOption("consultar", "help:cmd:consultar", "Info de una cuenta Valorant")
                .addOption("vplayer", "help:cmd:vplayer", "Perfil avanzado de Valorant")
                .addOption("vrank", "help:cmd:vrank", "Rango competitivo de un jugador")
                .addOption("vmatch", "help:cmd:vmatch", "Ultima partida de un jugador")
                .addOption("vregisteraccount", "help:cmd:vregisteraccount", "Registra tu cuenta")
                .addOption("vleaderboard", "help:cmd:vleaderboard", "Ranking del servidor")
                .build();
    }

    @NotNull
    private static EmbedBuilder getEmbedBuilder(MessageReceivedEvent event, String prefix) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(EmbedUtil.HEXA_COLOR));
        embed.setTitle("Centro de Ayuda");
        embed.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        embed.setDescription("Selecciona un comando del menu para ver sus detalles.\n"
                + "Prefijo actual: **`" + prefix + "`**\n\u200B");
        embed.addField("General", "`ping` - `prefix` - `help` - `welcome`", false);
        embed.addField("Moderacion", "`clear` - `kick` - `ban`", false);
        embed.addField("Economia", "`economy` - `balance` - `daily` - `pay`", false);
        embed.addField("Musica", "`music` - `play` - `pause` - `resume` - `queue` - `skip` - "
                + "`prev` - `stop` - `now` - `volume`", false);
        embed.addField("Valorant", "`consultar` - `vplayer` - `vrank` - `vmatch` - "
                + "`vregisteraccount` - `vleaderboard`", false);
        embed.setFooter("Hexa - " + event.getGuild().getName());
        return embed;
    }

    @Override
    public void execute(CommandContext context) {
        sendMainMenu(context.getEvent(), context.getPrefix());
    }
}
