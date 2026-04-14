package com.marlonreina.resisas.commands.riot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.service.RiotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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

                final String currentRank = latest.get("currenttierpatched").asText("Unranked");
                final int currentTier = latest.get("currenttier").asInt(0);
                final int rankingInTier = latest.get("ranking_in_tier").asInt(0);
                final int mmrChange = latest.get("mmr_change_to_last_game").asInt(0);
                final int currentElo = latest.get("elo").asInt(0);
                final String rankIconUrl = latest.get("images").get("small").asText();

                int peakElo = 0;
                String peakRank = currentRank;

                for (JsonNode entry : mmrData) {
                    int elo = entry.get("elo").asInt(0);
                    if (elo > peakElo) {
                        peakElo = elo;
                        peakRank = entry.get("currenttierpatched").asText("N/A");
                    }
                }

                final int oldestElo = mmrData.get(mmrData.size() - 1).get("elo").asInt(currentElo);
                final int eloChange = currentElo - oldestElo;
                final String eloChangeStr = eloChange >= 0 ? "▲ +"
                        + eloChange : "▼ "
                        + eloChange;

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
                Map<String, Integer> agentCount = new HashMap<>();

                if (matches != null && matches.isArray()) {
                    for (JsonNode match : matches) {

                        String mode = match.get("metadata").get("mode_id").asText();
                        if (!mode.equals("competitive")) {
                            continue;
                        }

                        final int roundsPlayed = match.get("metadata").get("rounds_played").asInt(1);
                        final JsonNode players = match.get("players").get("all_players");
                        final JsonNode teams = match.get("teams");

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

                        final int k = stats.get("kills").asInt();
                        final int d = stats.get("deaths").asInt();
                        final int a = stats.get("assists").asInt();
                        final int s = stats.get("score").asInt();
                        final int dmg = mainPlayer.get("damage_made").asInt();

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

                final double avgKda = totalDeaths > 0
                        ? (totalKills + totalAssists) / (double) totalDeaths
                        : totalKills + totalAssists;

                final double avgAcs = totalGames > 0 ? (double) totalAcs / totalGames : 0;
                final double avgDmg = totalGames > 0 ? (double) totalDmg / totalGames : 0;
                final double winRate = totalGames > 0 ? (wins * 100.0 / totalGames) : 0;

                final String mostPlayedAgent = agentCount.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("N/A");

                final Color color = winRate >= 55 ? new Color(0x2ECC71)
                        : winRate >= 45 ? new Color(0xF39C12)
                        : new Color(0xE74C3C);

                final String rankEmoji = getTierEmoji(currentTier);
                final String mmrChangeLastGame = mmrChange >= 0
                        ? "+" + mmrChange
                        : String.valueOf(mmrChange);

                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(color);
                embed.setThumbnail(rankIconUrl);

                if (agentIconUrl != null) {
                    embed.setAuthor(name + "#" + tag, null, agentIconUrl);
                }

                embed.setTitle("📊 Perfil Valorant");
                embed.setDescription(
                        rankEmoji + " **" + currentRank + "** · `" + rankingInTier + " RR` "
                                + "(" + mmrChangeLastGame + " último partido)\n"
                                + "📈 ELO: **" + currentElo + "** " + eloChangeStr + "\n"
                                + "🏆 Peak: **" + peakRank + "** (" + peakElo + " ELO)\n\u200B"
                );

                embed.addField("🎮 Partidas (" + totalGames + ")",
                        "Victorias: " + wins + " (" + String.format("%.1f", winRate) + "%)\n"
                                + "Derrotas: " + losses,
                        false);

                embed.addField("📈 Promedio",
                        "KDA: " + String.format("%.2f", avgKda) + "\n"
                                + "K/D/A: " + totalKills + "/" + totalDeaths + "/" + totalAssists + "\n"
                                + "ACS: " + String.format("%.0f", avgAcs) + "\n"
                                + "Daño: " + String.format("%.0f", avgDmg),
                        false);

                embed.addField("🌟 Highlights",
                        "Aces: " + aces + "\n"
                                + "Clutches: " + clutches + "\n"
                                + "Agente: " + mostPlayedAgent,
                        false);

                loadingMsg.delete().queue();
                event.getChannel().sendMessageEmbeds(embed.build()).queue();

            } catch (Exception e) {
                loadingMsg.editMessage("❌ Error obteniendo el perfil.").queue();
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
            return "💎";
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