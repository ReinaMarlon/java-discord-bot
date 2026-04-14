package com.marlonreina.resisas.commands.riot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.service.RiotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class ValorantPlayerCommand implements Command {

    private final RiotService riotService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ValorantPlayerCommand(RiotService riotService) {
        this.riotService = riotService;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        if (args.length < 1) {
            event.getChannel().sendMessage("Uso: `!vplayer nombre#tag`").queue();
            return;
        }

        event.getChannel().sendMessage("🔍 Analizando perfil, espera un momento...").queue(loadingMsg -> {
            try {
                String fullInput = String.join(" ", args);
                String[] riotId = fullInput.split("#");

                if (riotId.length < 2) {
                    loadingMsg.editMessage("❌ Formato correcto: `nombre#tag`").queue();
                    return;
                }

                String name = riotId[0].trim();
                String tag = riotId[1].trim();

                String mmrHistoryJson = riotService.getMmRhistory("latam", name, tag);
                String matchesJson = riotService.getMatchHistory("latam", name, tag, 10);

                JsonNode mmrRoot = mapper.readTree(mmrHistoryJson);
                JsonNode matchesRoot = mapper.readTree(matchesJson);

                JsonNode mmrData = mmrRoot.get("data");
                if (mmrData == null || mmrData.isNull() || !mmrData.isArray() || mmrData.size() == 0) {
                    loadingMsg.editMessage("❌ No se encontró el jugador o no tiene partidas ranked.").queue();
                    return;
                }

                JsonNode latest = mmrData.get(0);
                String currentRank = latest.get("currenttierpatched").asText("Unranked");
                int currentTier = latest.get("currenttier").asInt(0);
                int rankingInTier = latest.get("ranking_in_tier").asInt(0);
                int mmrChange = latest.get("mmr_change_to_last_game").asInt(0);
                int currentElo = latest.get("elo").asInt(0);
                String rankIconUrl = latest.get("images").get("small").asText();

                int peakElo = 0;
                String peakRank = currentRank;
                String peakDate = "";

                for (JsonNode entry : mmrData) {
                    int elo = entry.get("elo").asInt(0);
                    if (elo > peakElo) {
                        peakElo = elo;
                        peakRank = entry.get("currenttierpatched").asText("N/A");
                        peakDate = entry.get("date").asText("");
                        if (peakDate.contains(" ")) {
                            String[] parts2 = peakDate.split(",");
                            peakDate = parts2.length > 1
                                    ? parts2[0] + "," + parts2[1].trim().split(" ")[0]
                                    + " " + parts2[1].trim().split(" ")[1]
                                    : peakDate;
                        }
                    }
                }

                int oldestElo = mmrData.get(mmrData.size() - 1).get("elo").asInt(currentElo);
                int eloChange = currentElo - oldestElo;
                String eloChangeStr = eloChange >= 0 ? "▲ +" + eloChange : "▼ " + eloChange;

                JsonNode matches = matchesRoot.get("data");
                int totalGames = 0;
                int wins = 0;
                int losses = 0;
                int totalKills = 0;
                int totalDeaths = 0;
                int totalAssists = 0;
                int totalAcs = 0;
                int totalDmg = 0;
                int aces = 0;
                int clutches = 0;
                String agentIconUrl = null;
                java.util.Map<String, Integer> agentCount = new java.util.HashMap<>();

                if (matches != null && matches.isArray()) {
                    for (JsonNode match : matches) {

                        String mode = match.get("metadata").get("mode_id").asText();
                        if (!mode.equals("competitive")) {
                            continue;
                        }

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
                        if (mainPlayer == null) {
                            continue;
                        }

                        totalGames++;

                        String agent = mainPlayer.get("character").asText();
                        agentCount.merge(agent, 1, Integer::sum);
                        if (agentIconUrl == null) {
                            agentIconUrl = mainPlayer.get("assets").get("agent").get("small").asText();
                        }

                        JsonNode stats = mainPlayer.get("stats");
                        int k = stats.get("kills").asInt();
                        int d = stats.get("deaths").asInt();
                        int a = stats.get("assists").asInt();
                        int s = stats.get("score").asInt();
                        int dmg = mainPlayer.get("damage_made").asInt();

                        totalKills += k;
                        totalDeaths += d;
                        totalAssists += a;
                        totalAcs += (s / Math.max(roundsPlayed, 1));
                        totalDmg += dmg;

                        String playerTeam = mainPlayer.get("team").asText();
                        boolean redWon = teams.get("red").get("has_won").asBoolean();
                        boolean won = (playerTeam.equalsIgnoreCase("Red") && redWon)
                                || (playerTeam.equalsIgnoreCase("Blue") && !redWon);
                        if (won) {
                            wins++;
                        } else {
                            losses++;
                        }

                        JsonNode rounds = match.get("rounds");
                        if (rounds != null) {
                            for (JsonNode round : rounds) {
                                JsonNode playerStats = round.get("player_stats");
                                if (playerStats == null) {
                                    continue;
                                }
                                for (JsonNode ps : playerStats) {
                                    if (!ps.get("player_puuid").asText()
                                            .equals(mainPlayer.get("puuid").asText())) {
                                        continue;
                                    }

                                    int roundKills = ps.get("kills").asInt(0);
                                    if (roundKills >= 5) {
                                        aces++;
                                    }

                                    if (roundKills >= 1) {
                                        JsonNode killEvents = ps.get("kill_events");
                                        if (killEvents != null) {
                                            for (JsonNode ke : killEvents) {
                                                JsonNode locs = ke.get("player_locations_on_kill");
                                                if (locs == null) {
                                                    continue;
                                                }
                                                long aliveTeammates = 0;
                                                for (JsonNode loc : locs) {
                                                    if (loc.get("player_team").asText()
                                                            .equalsIgnoreCase(playerTeam)) {
                                                        aliveTeammates++;
                                                    }
                                                }
                                                if (aliveTeammates == 1) {
                                                    clutches++;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                double avgKda = totalDeaths > 0
                        ? (totalKills + totalAssists) / (double) totalDeaths
                        : totalKills + totalAssists;
                double avgAcs = totalGames > 0 ? (double) totalAcs / totalGames : 0;
                double avgDmg = totalGames > 0 ? (double) totalDmg / totalGames : 0;
                double winRate = totalGames > 0 ? (wins * 100.0 / totalGames) : 0;

                String mostPlayedAgent = agentCount.entrySet().stream()
                        .max(java.util.Map.Entry.comparingByValue())
                        .map(java.util.Map.Entry::getKey)
                        .orElse("N/A");

                Color color = winRate >= 55 ? new Color(0x2ECC71)
                        : winRate >= 45 ? new Color(0xF39C12)
                        : new Color(0xE74C3C);

                String rankEmoji = getTierEmoji(currentTier);
                String mmrChangeStr = mmrChange >= 0 ? "+" + mmrChange : String.valueOf(mmrChange);

                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(color);
                embed.setThumbnail(rankIconUrl);

                if (agentIconUrl != null) {
                    embed.setAuthor(name + "#" + tag, null, agentIconUrl);
                }

                embed.setTitle("📊  Perfil Valorant");
                embed.setDescription(
                        rankEmoji + "  **" + currentRank + "**  ·  `" + rankingInTier + " RR`  "
                                + "(" + mmrChangeStr + " último partido)\n"
                                + "📈 ELO actual: **" + currentElo + "**  " + eloChangeStr + " vs historial\n"
                                + "🏆 Peak: **" + peakRank + "**  (" + peakElo + " ELO)\n\u200B"
                );

                embed.addField(
                        "🎮  Últimas " + totalGames + " partidas ranked",
                        String.format(
                                "```\n"
                                        + "Victorias    %d   (%.1f%%)\n"
                                        + "Derrotas     %d\n"
                                        + "```",
                                wins, winRate, losses
                        ),
                        false
                );

                embed.addField(
                        "📈  Rendimiento promedio",
                        String.format(
                                "```\n"
                                        + "KDA          %.2f\n"
                                        + "K / D / A    %d / %d / %d\n"
                                        + "ACS          %.0f\n"
                                        + "Daño/match   %.0f\n"
                                        + "```",
                                avgKda, totalKills, totalDeaths, totalAssists, avgAcs, avgDmg
                        ),
                        false
                );

                embed.addField(
                        "🌟  Highlights",
                        String.format(
                                "```\n"
                                        + "Aces         %d\n"
                                        + "Clutches     %d\n"
                                        + "Agente fav.  %s\n"
                                        + "```",
                                aces, clutches, mostPlayedAgent
                        ),
                        false
                );

                embed.setFooter("Basado en " + totalGames + " partidas ranked  •  ELO calculado de "
                        + mmrData.size() + " registros");

                loadingMsg.delete().queue();
                event.getChannel().sendMessageEmbeds(embed.build()).queue();

            } catch (Exception e) {
                loadingMsg.editMessage("❌ Error obteniendo el perfil. Verifica el nombre#tag.").queue();
                e.printStackTrace();
            }
        });
    }

    private String getTierEmoji(int tier) {
        if (tier == 0) {
            return "❓";
        }
        if (tier <= 5) {
            return "🔘";
        }
        if (tier <= 8) {
            return "🟤";
        }
        if (tier <= 11) {
            return "⚪";
        }
        if (tier <= 14) {
            return "🟡";
        }
        if (tier <= 17) {
            return "🔵";
        }
        if (tier <= 20) {
            return "💎️";
        }
        if (tier <= 23) {
            return "🟢";
        }
        if (tier <= 26) {
            return "🔴";
        }
        return "🏆";
    }
}