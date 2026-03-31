package com.marlonreina.resisas.commands.riot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.service.LeaderboardService;

public class ValorantRegisterCommand implements Command {

    private final LeaderboardService leaderboardService;

    public ValorantRegisterCommand(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        if (args.length < 1) {
            event.getChannel().sendMessage("Uso: `!vregisteraccount nombre#tag`").queue();
            return;
        }

        String fullInput = String.join(" ", args);
        String[] riotId  = fullInput.split("#");

        if (riotId.length < 2) {
            event.getChannel().sendMessage("❌ Formato correcto: `nombre#tag`").queue();
            return;
        }

        String name      = riotId[0].trim();
        String tag       = riotId[1].trim();
        String guildId   = event.getGuild().getId();
        String discordId = event.getAuthor().getId();

        boolean saved = leaderboardService.register(guildId, discordId, name, tag);

        if (saved) {
            event.getChannel().sendMessage(
                    "✅ **" + name + "#" + tag + "** registrado en el leaderboard de este servidor."
            ).queue();
        } else {
            event.getChannel().sendMessage(
                    "⚠️ **" + name + "#" + tag + "** ya estaba registrado en este servidor."
            ).queue();
        }
    }
}