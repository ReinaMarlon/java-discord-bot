package com.marlonreina.resisas.utils;

import com.marlonreina.resisas.service.GuildService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

@Component
public class PrefixResolver {

    private final GuildService guildService;

    public PrefixResolver(GuildService guildService) {
        this.guildService = guildService;
    }

    /**
     * Devuelve el prefijo actual del servidor.
     */
    public String getPrefix(String guildId) {
        return guildService.getOrCreate(guildId).getPrefix();
    }

    /**
     * Verifica si el mensaje activa un comando, ya sea por prefijo o por @mención al bot.
     * Devuelve el prefijo/mención detectada, o null si no aplica.
     */
    public String resolvePrefix(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        String guildId = event.getGuild().getId();
        String botId = event.getJDA().getSelfUser().getId();

        // ── Mención directa: @Bot comando ─────────────────────────────
        String mentionPrefix = "<@" + botId + ">";
        String mentionPrefix2 = "<@!" + botId + ">"; // legacy mention format

        if (content.startsWith(mentionPrefix)) {
            return mentionPrefix;
        }
        if (content.startsWith(mentionPrefix2)) {
            return mentionPrefix2;
        }

        // ── Prefijo normal del servidor ────────────────────────────────
        String prefix = getPrefix(guildId);
        if (content.startsWith(prefix)) {
            return prefix;
        }

        return null; // No es un comando
    }
}