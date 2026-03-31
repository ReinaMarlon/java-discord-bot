package com.marlonreina.resisas.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.service.GuildService;

import java.awt.*;

public class HelpCommand implements Command {

    private final GuildService guildService;

    public HelpCommand(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String prefix = guildService.getOrCreate(event.getGuild().getId()).getPrefix();
        sendMainMenu(event, prefix);
    }

    public static void sendMainMenu(MessageReceivedEvent event, String prefix) {
        EmbedBuilder embed = getEmbedBuilder(event, prefix);

        StringSelectMenu menu = StringSelectMenu.create("help:select")
                .setPlaceholder("📋 Selecciona un comando...")
                // General
                .addOption("ping",      "help:cmd:ping",      "Comprueba la latencia del bot",           net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("⚙️"))
                .addOption("prefix",    "help:cmd:prefix",    "Cambia el prefijo del servidor",          net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("⚙️"))
                .addOption("help",      "help:cmd:help",      "Muestra esta ayuda",                      net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("⚙️"))
                // Moderación
                .addOption("clear",     "help:cmd:clear",     "Elimina mensajes del canal",              net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🛡️"))
                .addOption("kick",      "help:cmd:kick",      "Expulsa a un miembro",                    net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🛡️"))
                .addOption("ban",       "help:cmd:ban",       "Banea a un miembro",                      net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🛡️"))
                // Valorant
                .addOption("consultar", "help:cmd:consultar", "Info de una cuenta Valorant",             net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🎮"))
                .addOption("vrank",     "help:cmd:vrank",     "Rango competitivo de un jugador",         net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🎮"))
                .addOption("vmatch",    "help:cmd:vmatch",    "Última partida de un jugador",            net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🎮"))
                .build();

        event.getChannel().sendMessageEmbeds(embed.build())
                .addActionRow(menu)
                .queue();
    }

    @NotNull
    private static EmbedBuilder getEmbedBuilder(MessageReceivedEvent event, String prefix) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(0x5865F2));
        embed.setTitle("📖  Centro de Ayuda");
        embed.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        embed.setDescription(
                "Selecciona un comando del menú para ver sus detalles.\n" +
                        "Prefijo actual: **`" + prefix + "`**\n\u200B"
        );
        embed.addField("⚙️  General",      "`ping` · `prefix` · `help`",            false);
        embed.addField("🛡️  Moderación",   "`clear` · `kick` · `ban`",              false);
        embed.addField("🎮  Valorant",      "`consultar` · `vrank` · `vmatch`",      false);
        embed.setFooter("Resisas Bot  •  " + event.getGuild().getName());
        return embed;
    }
}