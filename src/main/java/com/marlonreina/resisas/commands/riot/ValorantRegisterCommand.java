package com.marlonreina.resisas.commands.riot;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.service.LeaderboardService;
import com.marlonreina.resisas.utils.EmbedUtil;

public class ValorantRegisterCommand implements Command {

    private final LeaderboardService leaderboardService;

    public ValorantRegisterCommand(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void execute(CommandContext context) {
        var event = context.getEvent();
        String[] args = context.getArgs();

        if (args.length < 1) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("vregisteraccount nombre#tag")).build()
            ).queue();
            return;
        }

        String fullInput = String.join(" ", args);
        String[] riotId = fullInput.split("#");

        if (riotId.length < 2) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("Formato correcto: `nombre#tag`.").build()
            ).queue();
            return;
        }

        String name = riotId[0].trim();
        String tag = riotId[1].trim();
        String guildId = event.getGuild().getId();
        String discordId = event.getAuthor().getId();

        boolean saved = leaderboardService.register(guildId, discordId, name, tag);

        if (saved) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.success("Cuenta registrada")
                            .setDescription("**" + name + "#" + tag
                                    + "** quedo en el leaderboard de este servidor.")
                            .build()
            ).queue();
        } else {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.info("Cuenta existente")
                            .setDescription("**" + name + "#" + tag
                                    + "** ya estaba registrada en este servidor.")
                            .build()
            ).queue();
        }
    }
}
