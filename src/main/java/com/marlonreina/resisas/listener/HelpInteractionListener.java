package com.marlonreina.resisas.listener;

import com.marlonreina.resisas.service.GuildService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.Color;

public class HelpInteractionListener extends ListenerAdapter {

    private final GuildService guildService;

    public HelpInteractionListener(GuildService guildService) {
        this.guildService = guildService;
    }

    // ── SelectMenu: usuario eligió un comando ──────────────────────────
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("help:select")) {
            return;
        }

        String selected = event.getValues().get(0); // ej: "help:cmd:vmatch"
        if (!selected.startsWith("help:cmd:")) {
            return;
        }

        String cmd = selected.replace("help:cmd:", "");
        String prefix = guildService.getOrCreate(event.getGuild().getId()).getPrefix();

        EmbedBuilder embed = buildCommandEmbed(cmd, prefix, event.getGuild().getName(),
                event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        Button backButton = Button.secondary("help:back", "← Volver al menú");

        event.editMessageEmbeds(embed.build())
                .setActionRow(backButton)
                .queue();
    }

    // ── Button: usuario presionó "← Volver" ───────────────────────────
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("help:back")) {
            return;
        }

        String prefix = guildService.getOrCreate(event.getGuild().getId()).getPrefix();
        String guildName = event.getGuild().getName();
        String botAvatar = event.getJDA().getSelfUser().getEffectiveAvatarUrl();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(0x5865F2));
        embed.setTitle("📖  Centro de Ayuda");
        embed.setThumbnail(botAvatar);
        embed.setDescription(
                "Selecciona un comando del menú para ver sus detalles.\n"
                        + "Prefijo actual: **`"
                        + prefix
                        + "`**\n\u200B"
        );
        embed.addField("⚙️  General", "`ping` · `prefix` · `help`", false);
        embed.addField("🛡️  Moderación", "`clear` · `kick` · `ban`", false);
        embed.addField("🎮  Valorant", "`consultar` · `vrank` · `vmatch`", false);
        embed.setFooter("Resisas Bot  •  "
                + guildName);

        StringSelectMenu menu = StringSelectMenu.create("help:select")
                .setPlaceholder("📋 Selecciona un comando...")
                .addOption("ping", "help:cmd:ping", "Comprueba la latencia del bot",
                        net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("⚙️"))
                .addOption("prefix", "help:cmd:prefix", "Cambia el prefijo del servidor",
                        net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("⚙️"))
                .addOption("help", "help:cmd:help", "Muestra esta ayuda",
                        net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("⚙️"))
                .addOption("clear", "help:cmd:clear", "Elimina mensajes del canal",
                        net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🛡️"))
                .addOption("kick", "help:cmd:kick", "Expulsa a un miembro",
                        net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🛡️"))
                .addOption("ban", "help:cmd:ban", "Banea a un miembro",
                        net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🛡️"))
                .addOption("consultar", "help:cmd:consultar", "Info de una cuenta Valorant",
                        net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🎮"))
                .addOption("vrank", "help:cmd:vrank", "Rango competitivo de un jugador",
                        net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🎮"))
                .addOption("vmatch", "help:cmd:vmatch", "Última partida de un jugador",
                        net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("🎮"))
                .build();

        event.editMessageEmbeds(embed.build())
                .setActionRow(menu)
                .queue();
    }

    // ── Builder de embed por comando ───────────────────────────────────
    private EmbedBuilder buildCommandEmbed(String cmd, String prefix,
                                           String guildName, String botAvatar) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(0x5865F2));
        embed.setThumbnail(botAvatar);

        switch (cmd) {
            case "ping" -> {
                embed.setTitle("⚙️  ping");
                embed.setDescription("Comprueba si el bot está activo y mide la latencia.");
                embed.addField("📌 Uso", "`"
                        + prefix
                        + "ping`", false);
                embed.addField("📋 Ejemplo", "`"
                        + prefix
                        + "ping`", false);
                embed.addField("🔒 Permisos", "Ninguno", false);
            }
            case "prefix" -> {
                embed.setTitle("⚙️  prefix");
                embed.setDescription("Cambia el prefijo del bot en este servidor.");
                embed.addField("📌 Uso", "`"
                        + prefix
                        + "prefix <nuevo>`", false);
                embed.addField("📋 Ejemplo", "`"
                        + prefix
                        + "prefix !`", false);
                embed.addField("🔒 Permisos", "Administrador", false);
            }
            case "help" -> {
                embed.setTitle("⚙️  help");
                embed.setDescription("Muestra el centro de ayuda interactivo.");
                embed.addField("📌 Uso", "`"
                        + prefix
                        + "help`", false);
                embed.addField("🔒 Permisos", "Ninguno", false);
            }
            case "clear" -> {
                embed.setTitle("🛡️  clear");
                embed.setDescription("Elimina una cantidad de mensajes del canal.");
                embed.addField("📌 Uso", "`"
                        + prefix
                        + "clear <cantidad>`", false);
                embed.addField("📋 Ejemplo", "`"
                        + prefix
                        + "clear 10`", false);
                embed.addField("⚠️ Límite", "Máximo 100 mensajes por ejecución.", false);
                embed.addField("🔒 Permisos", "Gestionar mensajes", false);
            }
            case "kick" -> {
                embed.setTitle("🛡️  kick");
                embed.setDescription("Expulsa a un miembro del servidor.");
                embed.addField("📌 Uso", "`"
                        + prefix
                        + "kick <@usuario> [razón]`", false);
                embed.addField("📋 Ejemplo", "`"
                        + prefix
                        + "kick @user spam`", false);
                embed.addField("🔒 Permisos", "Expulsar miembros", false);
            }
            case "ban" -> {
                embed.setTitle("🛡️  ban");
                embed.setDescription("Banea permanentemente a un miembro.");
                embed.addField("📌 Uso", "`"
                        + prefix
                        + "ban <@usuario> [razón]`", false);
                embed.addField("📋 Ejemplo", "`"
                        + prefix
                        + "ban @user conducta inapropiada`", false);
                embed.addField("🔒 Permisos", "Banear miembros", false);
            }
            case "consultar" -> {
                embed.setTitle("🎮  consultar");
                embed.setDescription("Muestra información general de una cuenta de Valorant.");
                embed.addField("📌 Uso", "`"
                        + prefix
                        + "consultar <nombre#tag>`", false);
                embed.addField("📋 Ejemplo", "`"
                        + prefix
                        + "consultar Neon#RS シ`", false);
                embed.addField("🌎 Región", "Latinoamérica (latam)", false);
                embed.addField("🔒 Permisos", "Ninguno", false);
            }
            case "vrank" -> {
                embed.setTitle("🎮  vrank");
                embed.setDescription("Muestra el rango competitivo actual de un jugador.");
                embed.addField("📌 Uso", "`"
                        + prefix
                        + "vrank <nombre#tag>`", false);
                embed.addField("📋 Ejemplo", "`"
                        + prefix
                        + "vrank Neon#RS シ`", false);
                embed.addField("📊 Info", "Tier, RR actuales y pico histórico.", false);
                embed.addField("🌎 Región", "Latinoamérica (latam)", false);
                embed.addField("🔒 Permisos", "Ninguno", false);
            }
            case "vmatch" -> {
                embed.setTitle("🎮  vmatch");
                embed.setDescription("Resumen detallado de la última partida competitiva.");
                embed.addField("📌 Uso", "`"
                        + prefix
                        + "vmatch <nombre#tag>`", false);
                embed.addField("📋 Ejemplo", "`"
                        + prefix
                        + "vmatch Neon#RS シ`", false);
                embed.addField("📊 Incluye",
                        "• Resultado · Mapa · Modo · Hora\n"
                                + "• K/D/A · KDA ratio · ACS · Score\n"
                                + "• Headshot % · Daño hecho/recibido\n"
                                + "• Tabla completa de ambos equipos",
                        false
                );
                embed.addField("🌎 Región", "Latinoamérica (latam)", false);
                embed.addField("🔒 Permisos", "Ninguno", false);
            }
            default -> {
                embed.setColor(new Color(0xE74C3C));
                embed.setTitle("❌  Comando no encontrado");
                embed.setDescription("Usa el menú para seleccionar un comando válido.");
            }
        }

        embed.setFooter("Resisas Bot  •  "
                + guildName);
        return embed;
    }
}