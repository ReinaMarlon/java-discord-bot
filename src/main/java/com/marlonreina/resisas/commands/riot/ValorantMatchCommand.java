package com.marlonreina.resisas.commands.riot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.service.RiotService;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class ValorantMatchCommand implements Command {

    // Emojis de rango aproximado por tier
    private static final String[] TIER_EMOJIS = {
        "⬜", "⬜", "⬜",
        "🟫", "🟫", "🟫",
        "⬛", "⬛", "⬛",
        "🟨", "🟨", "🟨",
        "🟦", "🟦", "🟦",
        "💠", "💠", "💠",
        "🔷", "🔷", "🔷",
        "💎", "💎", "💎",
        "🏆"
    };
    private final RiotService riotService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ValorantMatchCommand(RiotService riotService) {
        this.riotService = riotService;
    }

    @Override
    public void execute(CommandContext context) {
        MessageReceivedEvent event = context.getEvent();
        String[] args = context.getArgs();

        if (args.length < 1) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("vmatch nombre#tag")).build()
            ).queue();
            return;
        }

        try {
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

            String json = riotService.getMatches("latam", name, tag);
            JsonNode root = mapper.readTree(json);
            JsonNode match = root.get("data").get(0);

            // ── Metadata ─────────────────────────
            JsonNode meta = match.get("metadata");
            int roundsPlayed = meta.get("rounds_played").asInt();

            final String map = meta.get("map").asText();
            final String mode = meta.get("mode").asText();
            final String startTime = meta.get("game_start_patched").asText();

            // ── Resultado ────────────────────────
            JsonNode teams = match.get("teams");
            JsonNode redTeam = teams.get("red");
            JsonNode blueTeam = teams.get("blue");

            boolean redWon = redTeam.get("has_won").asBoolean();
            final int redWins = redTeam.get("rounds_won").asInt();
            final int blueWins = blueTeam.get("rounds_won").asInt();

            // ── Jugador ──────────────────────────
            JsonNode players = match.get("players").get("all_players");
            JsonNode mainPlayer = null;

            for (JsonNode p : players) {
                if (p.get("name").asText().equalsIgnoreCase(name)) {
                    mainPlayer = p;
                    break;
                }
            }

            if (mainPlayer == null) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtil.error("No se encontro al jugador en la partida.").build()
                ).queue();
                return;
            }

            String playerTeam = mainPlayer.get("team").asText();
            int tier = mainPlayer.get("currenttier").asInt();

            final String agent = mainPlayer.get("character").asText();
            final String rank = mainPlayer.get("currenttier_patched").asText();
            String agentIconUrl = mainPlayer.get("assets").get("agent").get("small").asText();

            JsonNode stats = mainPlayer.get("stats");

            int kills = stats.get("kills").asInt();
            int deaths = stats.get("deaths").asInt();
            int assists = stats.get("assists").asInt();
            int score = stats.get("score").asInt();

            int hs = stats.get("headshots").asInt();
            int bs = stats.get("bodyshots").asInt();
            int ls = stats.get("legshots").asInt();

            int totalShots = hs + bs + ls;

            final int dmgMade = mainPlayer.get("damage_made").asInt();
            final int dmgReceived = mainPlayer.get("damage_received").asInt();
            final int acs = score / Math.max(roundsPlayed, 1);
            final double kda = (kills + assists) / (double) Math.max(deaths, 1);
            final double hsPercent = totalShots > 0 ? (hs * 100.0 / totalShots) : 0;

            boolean mainWon = (playerTeam.equalsIgnoreCase("Red") && redWon)
                    || (playerTeam.equalsIgnoreCase("Blue") && !redWon);

            final String resultado = mainWon ? "✅ Victoria" : "❌ Derrota";
            Color embedColor = mainWon ? new Color(0x2ECC71) : new Color(0xE74C3C);

            // ── Equipos ──────────────────────────
            StringBuilder redBuilder = new StringBuilder();
            StringBuilder blueBuilder = new StringBuilder();

            for (JsonNode p : players) {

                String playerName = p.get("name").asText() + "#" + p.get("tag").asText();
                String playerAgent = p.get("character").asText();
                int playerTier = p.get("currenttier").asInt();

                JsonNode playerStats = p.get("stats");

                int pk = playerStats.get("kills").asInt();
                int pd = playerStats.get("deaths").asInt();
                int pa = playerStats.get("assists").asInt();
                int ps = playerStats.get("score").asInt();

                int playerAcs = ps / Math.max(roundsPlayed, 1);

                String tierEmoji = getTierEmoji(playerTier);

                boolean isMain = p.get("name").asText().equalsIgnoreCase(name);
                String prefix = isMain ? "**★ " : "";
                String suffix = isMain ? "**" : "";

                String line = String.format(
                        "%s%s %s | %s | %d/%d/%d | ACS %d%s%n",
                        prefix, tierEmoji, playerAgent, playerName,
                        pk, pd, pa, playerAcs, suffix
                );

                if (p.get("team").asText().equalsIgnoreCase("Red")) {
                    redBuilder.append(line);
                } else {
                    blueBuilder.append(line);
                }
            }

            String scoreLine = String.format(
                    "🔴 Red **%d** — **%d** Blue 🔵",
                    redWins, blueWins
            );

            String rankEmoji = getTierEmoji(tier);

            // ── Embed ────────────────────────────
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(embedColor);
            embed.setThumbnail(agentIconUrl);

            embed.setTitle(resultado + " | " + map + " · " + mode);

            embed.setDescription(
                    "🕐 " + startTime + "\n"
                            + "🗺️ Rondas: **" + roundsPlayed + "**\n"
                            + scoreLine
            );

            embed.addField(
                    "👤 " + name + "#" + tag + " — " + agent + " " + rankEmoji + " " + rank,
                    String.format(
                            "```%n"
                                    + "K / D / A     %d / %d / %d%n"
                                    + "KDA Ratio     %.2f%n"
                                    + "ACS           %d%n"
                                    + "Score         %d%n"
                                    + "HS%%           %.1f%%%n"
                                    + "Daño hecho    %d%n"
                                    + "Daño recibido %d%n"
                                    + "```",
                            kills, deaths, assists, kda, acs, score, hsPercent, dmgMade, dmgReceived
                    ),
                    false
            );

            embed.addField("🔴 Team Red", redBuilder.toString(), false);
            embed.addField("🔵 Team Blue", blueBuilder.toString(), false);

            embed.setFooter("Hexa Valorant Match - " + map);

            event.getChannel().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("Error obteniendo la partida.").build()
            ).queue();
            e.printStackTrace();
        }
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
