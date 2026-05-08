package com.marlonreina.resisas.listener;

import com.marlonreina.resisas.service.GuildService;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
public class HelpInteractionListener extends ListenerAdapter {

    private final GuildService guildService;

    public HelpInteractionListener(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("help:select")) {
            return;
        }

        String selected = event.getValues().get(0);
        if (!selected.startsWith("help:cmd:")) {
            return;
        }

        String command = selected.replace("help:cmd:", "");
        String prefix = guildService.getOrCreate(event.getGuild().getId()).getPrefix();
        EmbedBuilder embed = buildCommandEmbed(command, prefix, event.getGuild().getName(),
                event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        event.editMessageEmbeds(embed.build())
                .setActionRow(Button.secondary("help:back", "Volver al menu"))
                .queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("help:back")) {
            return;
        }

        String prefix = guildService.getOrCreate(event.getGuild().getId()).getPrefix();
        event.editMessageEmbeds(buildMainEmbed(prefix, event.getGuild().getName(),
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl()).build())
                .setActionRow(buildMenu())
                .queue();
    }

    private EmbedBuilder buildMainEmbed(String prefix, String guildName, String botAvatar) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(EmbedUtil.HEXA_COLOR));
        embed.setTitle("Centro de Ayuda");
        embed.setThumbnail(botAvatar);
        embed.setDescription("Selecciona un comando del menu para ver sus detalles.\n"
                + "Prefijo actual: **`" + prefix + "`**\n\u200B");
        embed.addField("General", "`ping` - `prefix` - `help` - `welcome`", false);
        embed.addField("Moderacion", "`clear` - `kick` - `ban`", false);
        embed.addField("Economia", "`economy` - `balance` - `daily` - `pay`", false);
        embed.addField("Musica", "`music` - `play` - `pause` - `resume` - `queue` - `skip` - "
                + "`prev` - `stop` - `now` - `volume`", false);
        embed.addField("Valorant", "`consultar` - `vplayer` - `vrank` - `vmatch` - "
                + "`vregisteraccount` - `vleaderboard`", false);
        embed.setFooter("Hexa - " + guildName);
        return embed;
    }

    private StringSelectMenu buildMenu() {
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

    private EmbedBuilder buildCommandEmbed(String command, String prefix,
                                           String guildName, String botAvatar) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(EmbedUtil.HEXA_COLOR));
        embed.setThumbnail(botAvatar);

        switch (command) {
            case "ping" -> commandInfo(embed, "ping", "Comprueba si Hexa esta activo.",
                    prefix + "ping", prefix + "ping", "Ninguno");
            case "prefix" -> commandInfo(embed, "prefix", "Cambia el prefijo del bot en este servidor.",
                    prefix + "prefix <nuevo>", prefix + "prefix !", "Administrador");
            case "help" -> commandInfo(embed, "help", "Muestra el centro de ayuda interactivo.",
                    prefix + "help", prefix + "help", "Ninguno");
            case "welcome" -> welcomeInfo(embed, prefix);
            case "clear" -> commandInfo(embed, "clear", "Elimina mensajes recientes del canal.",
                    prefix + "clear <cantidad>", prefix + "clear 10", "Gestionar mensajes");
            case "kick" -> commandInfo(embed, "kick", "Expulsa a un miembro del servidor.",
                    prefix + "kick @usuario [razon]", prefix + "kick @usuario spam", "Expulsar miembros");
            case "ban" -> commandInfo(embed, "ban", "Banea a un miembro del servidor.",
                    prefix + "ban @usuario [razon]", prefix + "ban @usuario spam", "Banear miembros");
            case "economy" -> economyInfo(embed, prefix);
            case "balance" -> commandInfo(embed, "balance", "Muestra tu balance o el de otro miembro.",
                    prefix + "balance [@usuario]", prefix + "balance @usuario", "Ninguno");
            case "daily" -> commandInfo(embed, "daily", "Reclama tu recompensa diaria.",
                    prefix + "daily", prefix + "daily", "Ninguno");
            case "pay" -> commandInfo(embed, "pay", "Transfiere hexacoins a otro miembro.",
                    prefix + "pay @usuario <cantidad>", prefix + "pay @usuario 100", "Ninguno");
            case "music" -> musicInfo(embed, prefix);
            case "play" -> commandInfo(embed, "play", "Reproduce una URL o busqueda en tu canal de voz.",
                    prefix + "play <url o busqueda>", prefix + "play never gonna give you up", "Ninguno");
            case "queue" -> commandInfo(embed, "queue", "Muestra la cola de musica.",
                    prefix + "queue", prefix + "queue", "Ninguno");
            case "skip" -> commandInfo(embed, "skip", "Salta a la siguiente pista.",
                    prefix + "skip", prefix + "skip", "Ninguno");
            case "consultar" -> commandInfo(embed, "consultar", "Muestra informacion general de Valorant.",
                    prefix + "consultar <nombre#tag>", prefix + "consultar Neon#RS", "Ninguno");
            case "vplayer" -> commandInfo(embed, "vplayer", "Muestra un perfil avanzado de Valorant.",
                    prefix + "vplayer <nombre#tag>", prefix + "vplayer Neon#RS", "Ninguno");
            case "vrank" -> commandInfo(embed, "vrank", "Muestra el rango competitivo actual.",
                    prefix + "vrank <nombre#tag>", prefix + "vrank Neon#RS", "Ninguno");
            case "vmatch" -> commandInfo(embed, "vmatch", "Resume la ultima partida competitiva.",
                    prefix + "vmatch <nombre#tag>", prefix + "vmatch Neon#RS", "Ninguno");
            case "vregisteraccount" -> commandInfo(embed, "vregisteraccount",
                    "Registra tu cuenta en el leaderboard del servidor.",
                    prefix + "vregisteraccount <nombre#tag>", prefix + "vregisteraccount Neon#RS", "Ninguno");
            case "vleaderboard" -> commandInfo(embed, "vleaderboard",
                    "Muestra el ranking Valorant del servidor.",
                    prefix + "vleaderboard", prefix + "vleaderboard", "Ninguno");
            default -> {
                embed.setColor(new Color(0xE74C3C));
                embed.setTitle("Comando no encontrado");
                embed.setDescription("Usa el menu para seleccionar un comando valido.");
            }
        }

        embed.setFooter("Hexa - " + guildName);
        return embed;
    }

    private void commandInfo(EmbedBuilder embed, String name, String description,
                             String usage, String example, String permissions) {
        embed.setTitle(name);
        embed.setDescription(description);
        embed.addField("Uso", "`" + usage + "`", false);
        embed.addField("Ejemplo", "`" + example + "`", false);
        embed.addField("Permisos", permissions, false);
    }

    private void economyInfo(EmbedBuilder embed, String prefix) {
        embed.setTitle("economy");
        embed.setDescription("Menu principal del sistema de economia.");
        embed.addField("Uso", "`" + prefix + "economy`", false);
        embed.addField("Subcomandos", "`" + prefix + "economy balance [@usuario]`\n`"
                + prefix + "economy daily`\n`"
                + prefix + "economy pay @usuario <cantidad>`\n`"
                + prefix + "economy leaderboard`", false);
        embed.addField("Permisos", "Ninguno", false);
    }

    private void musicInfo(EmbedBuilder embed, String prefix) {
        embed.setTitle("music");
        embed.setDescription("Menu principal del reproductor de musica.");
        embed.addField("Uso", "`" + prefix + "music`", false);
        embed.addField("Subcomandos", "`" + prefix + "music play <url o busqueda>`\n`"
                + prefix + "music pause`\n`"
                + prefix + "music resume`\n`"
                + prefix + "music queue`\n`"
                + prefix + "music skip`\n`"
                + prefix + "music prev`\n`"
                + prefix + "music stop`\n`"
                + prefix + "music volume <0-100>`", false);
        embed.addField("Permisos", "Ninguno", false);
    }

    private void welcomeInfo(EmbedBuilder embed, String prefix) {
        embed.setTitle("welcome");
        embed.setDescription("Configura el canal, estado y tipo de mensaje de bienvenida.");
        embed.addField("Uso", "`" + prefix + "welcome`", false);
        embed.addField("Subcomandos", "`" + prefix + "welcome setchannel {id del canal}`\n`"
                + prefix + "welcome enable {true o false}`\n`"
                + prefix + "welcome configmessage {simply o complex}`", false);
        embed.addField("Permisos", "Administrador", false);
    }
}
