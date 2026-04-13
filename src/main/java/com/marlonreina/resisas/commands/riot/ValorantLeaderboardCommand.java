package com.marlonreina.resisas.commands.riot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.model.LeaderboardAccount;
import com.marlonreina.resisas.service.LeaderboardService;
import com.marlonreina.resisas.service.RiotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ValorantLeaderboardCommand implements Command {

    private final LeaderboardService leaderboardService;
    private final RiotService riotService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ValorantLeaderboardCommand(LeaderboardService leaderboardService, RiotService riotService) {
        this.leaderboardService = leaderboardService;
        this.riotService = riotService;
    }

    private static class PlayerEntry {
        String riotId;
        int elo;
        int tier;
        String rankPatched;
        int rr;
        int mmrChange;
        String peakRank;
        int peakElo;
        double winRate;
        double avgAcs;
        double avgKda;
        int gamesPlayed;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        String guildId = event.getGuild().getId();
        List<LeaderboardAccount> accounts = leaderboardService.getAccounts(guildId);

        if (accounts.isEmpty()) {
            event.getChannel().sendMessage(
                    "❌ No hay cuentas registradas. Usa `!vregisteraccount nombre#tag` para agregar una."
            ).queue();
            return;
        }

        event.getChannel().sendMessage(
                "⏳ Consultando " + accounts.size() + " cuenta(s), espera un momento..."
        ).queue(loadingMsg -> {
            try {

                List<PlayerEntry> entries = new java.util.concurrent.CopyOnWriteArrayList<>();


                java.util.concurrent.ExecutorService executor =
                        java.util.concurrent.Executors.newFixedThreadPool(Math.min(accounts.size(), 10));

                List<java.util.concurrent.Future<?>> futures = new ArrayList<>();

                for (LeaderboardAccount acc : accounts) {
                    futures.add(executor.submit(() -> {
                        try {
                            Thread.sleep((long) (Math.random() * 1000));
                            PlayerEntry entry = buildEntry(acc);
                            if (entry != null) entries.add(entry);
                        } catch (Exception e) {
                            System.err.println("Error consultando " + acc.getRiotName() + ": " + e.getMessage());
                        }
                    }));
                }

                for (java.util.concurrent.Future<?> f : futures) {
                    try {
                        f.get(60, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (java.util.concurrent.ExecutionException e) {
                        System.err.println("Error en hilo: " + e.getCause());
                        e.getCause().printStackTrace();
                    } catch (java.util.concurrent.TimeoutException e) {
                        System.err.println("Timeout esperando respuesta de la API");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                executor.shutdown();

                if (entries.isEmpty()) {
                    loadingMsg.editMessage("❌ No se pudo obtener información de ninguna cuenta.").queue();
                    return;
                }


                entries.sort(Comparator.comparingInt((PlayerEntry e) -> e.elo).reversed());


                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(new Color(0xFF4655));
                embed.setTitle("🏆  Leaderboard Valorant  —  " + event.getGuild().getName());
                embed.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                StringBuilder board = new StringBuilder();
                String[] medals = {"🥇", "🥈", "🥉"};

                for (int i = 0; i < entries.size(); i++) {
                    PlayerEntry e = entries.get(i);
                    String pos = i < 3 ? medals[i] : "`#" + (i + 1) + "`";
                    String rankEmoji = getTierEmoji(e.tier);
                    String mmrStr = e.mmrChange >= 0 ? "▲+" + e.mmrChange : "▼" + e.mmrChange;

                    board.append(String.format(
                            "%s  %s %s **%s**  ·  `%d RR`  %s\n",
                            pos, rankEmoji, e.rankPatched, e.riotId, e.rr, mmrStr
                    ));
                }

                embed.setDescription(board.toString());

                for (int i = 0; i < entries.size(); i++) {
                    PlayerEntry e = entries.get(i);
                    String pos = i < 3 ? medals[i] : "#" + (i + 1);
                    String emoji = getTierEmoji(e.tier);

                    embed.addField(
                            pos + "  " + emoji + "  " + e.riotId,
                            String.format(
                                    "```\n" +
                                            "ELO        %d\n" +
                                            "Peak       %s (%d ELO)\n" +
                                            "Win Rate   %.1f%%  (%d partidas)\n" +
                                            "KDA        %.2f\n" +
                                            "ACS        %.0f\n" +
                                            "```",
                                    e.elo, e.peakRank, e.peakElo,
                                    e.winRate, e.gamesPlayed,
                                    e.avgKda, e.avgAcs
                            ),
                            true
                    );
                }

                embed.setFooter("Ordenado por ELO  •  Última actualización ahora");

                loadingMsg.delete().queue();
                event.getChannel().sendMessageEmbeds(embed.build()).queue();

            } catch (Exception e) {
                loadingMsg.editMessage("❌ Error generando el leaderboard.").queue();
                e.printStackTrace();
            }
        });
    }

    private PlayerEntry buildEntry(LeaderboardAccount acc) throws Exception {
        String name = acc.getRiotName();
        String tag = acc.getRiotTag();

        String mmrJson = riotService.getMMRhistory("latam", name, tag);
        JsonNode mmrRoot = mapper.readTree(mmrJson);
        JsonNode mmrData = mmrRoot.get("data");

        if (mmrData == null || !mmrData.isArray() || mmrData.size() == 0) return null;

        JsonNode latest = mmrData.get(0);

        PlayerEntry entry = new PlayerEntry();
        entry.riotId = name + "#" + tag;
        entry.elo = latest.get("elo").asInt(0);
        entry.tier = latest.get("currenttier").asInt(0);
        entry.rankPatched = latest.get("currenttierpatched").asText("Unranked");
        entry.rr = latest.get("ranking_in_tier").asInt(0);
        entry.mmrChange = latest.get("mmr_change_to_last_game").asInt(0);

        entry.peakElo = 0;
        entry.peakRank = entry.rankPatched;
        for (JsonNode e : mmrData) {
            int elo = e.get("elo").asInt(0);
            if (elo > entry.peakElo) {
                entry.peakElo = elo;
                entry.peakRank = e.get("currenttierpatched").asText("N/A");
            }
        }

        String matchJson = riotService.getMatchHistory("latam", name, tag, 10);
        JsonNode matchRoot = mapper.readTree(matchJson);
        JsonNode matches = matchRoot.get("data");

        int totalGames = 0, wins = 0;
        int totalKills = 0, totalDeaths = 0, totalAssists = 0;
        int totalAcs = 0;

        if (matches != null && matches.isArray()) {
            for (JsonNode match : matches) {
                if (!match.get("metadata").get("mode_id").asText().equals("competitive")) continue;

                int roundsPlayed = match.get("metadata").get("rounds_played").asInt(1);
                JsonNode players = match.get("players").get("all_players");
                JsonNode teams = match.get("teams");

                JsonNode mainPlayer = null;
                for (JsonNode p : players) {
                    if (p.get("name").asText().equalsIgnoreCase(name)) {
                        mainPlayer = p;
                        break;
                    }
                }
                if (mainPlayer == null) continue;

                totalGames++;
                JsonNode stats = mainPlayer.get("stats");
                int k = stats.get("kills").asInt();
                int d = stats.get("deaths").asInt();
                int a = stats.get("assists").asInt();
                int s = stats.get("score").asInt();

                totalKills += k;
                totalDeaths += d;
                totalAssists += a;
                totalAcs += (s / Math.max(roundsPlayed, 1));

                String playerTeam = mainPlayer.get("team").asText();
                boolean redWon = teams.get("red").get("has_won").asBoolean();
                boolean won = (playerTeam.equalsIgnoreCase("Red") && redWon)
                        || (playerTeam.equalsIgnoreCase("Blue") && !redWon);
                if (won) wins++;
            }
        }

        entry.gamesPlayed = totalGames;
        entry.winRate = totalGames > 0 ? (wins * 100.0 / totalGames) : 0;
        entry.avgKda = totalDeaths > 0
                ? (totalKills + totalAssists) / (double) totalDeaths
                : totalKills + totalAssists;
        entry.avgAcs = totalGames > 0 ? (double) totalAcs / totalGames : 0;

        return entry;
    }

    private String getTierEmoji(int tier) {
        if (tier == 0) return "❓";
        if (tier <= 5) return "🔘";
        if (tier <= 8) return "🟤";
        if (tier <= 11) return "⚪";
        if (tier <= 14) return "🟡";
        if (tier <= 17) return "🔵";
        if (tier <= 20) return "️💎️";
        if (tier <= 23) return "🟢";
        if (tier <= 26) return "🔴";
        return "🏆";
    }
}