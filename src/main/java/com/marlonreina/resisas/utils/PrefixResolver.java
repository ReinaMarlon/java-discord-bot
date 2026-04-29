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

    public String getPrefix(String guildId) {
        return guildService.getOrCreate(guildId).getPrefix();
    }

    public String resolvePrefix(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        String guildId = event.getGuild().getId();
        String botId = event.getJDA().getSelfUser().getId();

        String mentionPrefix = "<@" + botId + ">";
        if (startsWithMentionPrefix(content, mentionPrefix)) {
            return mentionPrefix;
        }

        String legacyMentionPrefix = "<@!" + botId + ">";
        if (startsWithMentionPrefix(content, legacyMentionPrefix)) {
            return legacyMentionPrefix;
        }

        String prefix = getPrefix(guildId);
        if (content.startsWith(prefix)) {
            return prefix;
        }

        return null;
    }

    private boolean startsWithMentionPrefix(String content, String mentionPrefix) {
        if (!content.startsWith(mentionPrefix)) {
            return false;
        }

        return content.length() == mentionPrefix.length()
                || Character.isWhitespace(content.charAt(mentionPrefix.length()));
    }
}
