package com.marlonreina.resisas.commands;

import com.marlonreina.resisas.commands.riot.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.marlonreina.resisas.commands.administrator.BanCommand;
import com.marlonreina.resisas.commands.administrator.ClearCommand;
import com.marlonreina.resisas.commands.administrator.KickCommand;
import com.marlonreina.resisas.commands.administrator.PrefixCommand;
import com.marlonreina.resisas.commands.misc.HelpCommand;
import com.marlonreina.resisas.commands.riot.*;
import com.marlonreina.resisas.commands.test.PingCommand;
import com.marlonreina.resisas.service.GuildService;
import com.marlonreina.resisas.service.LeaderboardService;
import com.marlonreina.resisas.service.RiotService;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    private final Map<String, Command> commands = new HashMap<>();
    private RiotService riotService = new RiotService();
    private LeaderboardService leaderboardService = new LeaderboardService();
    private final GuildService guildService = new GuildService();

    public CommandHandler() {

        commands.put("ping", new PingCommand());
        commands.put("prefix", new PrefixCommand());
        commands.put("clear", new ClearCommand());
        commands.put("kick", new KickCommand());
        commands.put("ban", new BanCommand());
        commands.put("consultar", new ValorantPlayerCommand(riotService));
        commands.put("vrank", new ValorantRankCommand(riotService));
        commands.put("vmatch", new ValorantMatchCommand(riotService));
        commands.put("vplayer", new ValorantPlayerCommand(riotService));
        commands.put("vregisteraccount", new ValorantRegisterCommand(leaderboardService));
        commands.put("vleaderboard",     new ValorantLeaderboardCommand(leaderboardService, riotService));
        commands.put("help", new HelpCommand(guildService));

    }

    public void handle(MessageReceivedEvent event, String prefix) {

        String msg   = event.getMessage().getContentRaw().trim();
        String body  = msg.substring(prefix.length()).trim(); // trim por si @mención deja espacio
        String[] parts = body.split("\\s+");

        if (parts.length == 0 || parts[0].isBlank()) return;

        String commandName = parts[0].toLowerCase();
        Command command    = commands.get(commandName);

        if (command != null) {
            String[] args = java.util.Arrays.copyOfRange(parts, 1, parts.length);
            command.execute(event, args);
        }
    }
}