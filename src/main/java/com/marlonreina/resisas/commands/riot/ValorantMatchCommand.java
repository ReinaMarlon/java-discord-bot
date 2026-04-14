package com.marlonreina.resisas.commands.riot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.service.RiotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class ValorantMatchCommand implements Command {

    // Emojis de rango aproximado por tier
    private static final String[] TIER_EMOJIS = {
            "⬜", "⬜", "⬜",           // Unranked / Iron
            "🟫", "🟫", "🟫",           // Bronze
            "⬛", "⬛", "⬛",           // Silver
            "🟨", "🟨", "🟨",           // Gold
            "🟦", "🟦", "🟦",           // Platinum
            "💠", "💠", "💠",           // Diamond
            "🔷", "🔷", "🔷",           // Ascendant
            "💎", "💎", "💎",           // Immortal
            "🏆"                       // Radiant
    };
    private final RiotService riotService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ValorantMatchCommand(RiotService riotService) {
        this.riotService = riotService;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        if (args.length < 1) {
            event.getChannel().sendMessage("Uso: `!vmatch nombre#tag`").queue();
            return;
        }

        try {
            String fullInput = String.join(" ", args); // "nombre#RS シ" aunque tenga espacios
            String[] riotId = fullInput.split("#");

            if (riotId.length < 2) {
                event.getChannel().sendMessage("Formato correcto: `nombre#tag`").queue();
                return;
            }

            String name = riotId[0].trim();
            String tag = riotId[1].trim();

            String json = riotService.getMatches("latam", name, tag);
            JsonNode root = mapper.readTree(json);
            JsonNode match = root.get("data").get(0);

            // ── Metadata ──────────────────────────────────────────────
            JsonNode meta = match.get("metadata");
            String map = meta.get("map").asText();
            String mode = meta.get("mode").asText();
            int roundsPlayed = meta.get("rounds_played").asInt();
            String startTime = meta.get("game_start_patched").asText();

            // ── Resultado del partido ──────────────────────────────────
            JsonNode teams = match.get("teams");
            JsonNode redTeam = teams.get("red");
            JsonNode blueTeam = teams.get("blue");
            int redWins = redTeam.get("rounds_won").asInt();
            int blueWins = blueTeam.get("rounds_won").asInt();
            boolean redWon = redTeam.get("has_won").asBoolean();

            // ── Jugador consultado ─────────────────────────────────────
            JsonNode players = match.get("players").get("all_players");
            JsonNode mainPlayer = null;

            for (JsonNode p : players) {
                if (p.get("name").asText().equalsIgnoreCase(name)) {
                    mainPlayer = p;
                    break;
                }
            }

            if (mainPlayer == null) {
                event.getChannel().sendMessage("❌ No se encontró al jugador en la partida.").queue();
                return;
            }

            String playerTeam = mainPlayer.get("team").asText();          // "Red" o "Blue"
            String agent = mainPlayer.get("character").asText();
            String rank = mainPlayer.get("currenttier_patched").asText();
            int tier = mainPlayer.get("currenttier").asInt();
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
            double hsPercent = totalShots > 0 ? (hs * 100.0 / totalShots) : 0;

            int dmgMade = mainPlayer.get("damage_made").asInt();
            int dmgReceived = mainPlayer.get("damage_received").asInt();
            int acs = score / Math.max(roundsPlayed, 1);
            double kda = (kills + assists) / (double) Math.max(deaths, 1);

            // Ganó o perdió?
            boolean mainWon = (playerTeam.equalsIgnoreCase("Red") && redWon)
                    || (playerTeam.equalsIgnoreCase("Blue") && !redWon);
            String resultado = mainWon ? "✅ Victoria" : "❌ Derrota";
            Color embedColor = mainWon ? new Color(0x2ECC71) : new Color(0xE74C3C);

            // ── Construcción de líneas por equipo ──────────────────────
            StringBuilder redBuilder = new StringBuilder();
            StringBuilder blueBuilder = new StringBuilder();

            for (JsonNode p : players) {
                String pName = p.get("name").asText() + "#" + p.get("tag").asText();
                String pAgent = p.get("character").asText();
                String pRank = p.get("currenttier_patched").asText();
                int pTier = p.get("currenttier").asInt();
                JsonNode pStats = p.get("stats");
                int pk = pStats.get("kills").asInt();
                int pd = pStats.get("deaths").asInt();
                int pa = pStats.get("assists").asInt();
                int ps = pStats.get("score").asInt();
                int pAcs = ps / Math.max(roundsPlayed, 1);
                String tierEmoji = getTierEmoji(pTier);

                // Marca al jugador principal con ★
                boolean isMain = p.get("name").asText().equalsIgnoreCase(name);
                String prefix = isMain ? "**★ " : "";
                String suffix = isMain ? "**" : "";

                String line = String.format("%s%s %s | %s | %d/%d/%d | ACS %d%s\n",
                        prefix, tierEmoji, pAgent, pName, pk, pd, pa, pAcs, suffix);

                if (p.get("team").asText().equalsIgnoreCase("Red")) {
                    redBuilder.append(line);
                } else {
                    blueBuilder.append(line);
                }
            }

            // ── Marcador ──────────────────────────────────────────────
            String scoreLine = String.format("🔴 Red **%d** — **%d** Blue 🔵", redWins, blueWins);

            // ── Rango emoji jugador principal ─────────────────────────
            String rankEmoji = getTierEmoji(tier);

            // ── Embed ─────────────────────────────────────────────────
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(embedColor);
            embed.setThumbnail(agentIconUrl);

            embed.setTitle(resultado + "  |  " + map + "  ·  " + mode);
            embed.setDescription(
                    "🕐 " + startTime + "\n" +
                            "🗺️ Rondas jugadas: **" + roundsPlayed + "**\n" +
                            scoreLine
            );

            // Jugador principal
            embed.addField(
                    "👤 " + name + "#" + tag + "  —  " + agent + "  " + rankEmoji + " " + rank,
                    String.format(
                            "```\n" +
                                    "K / D / A     %d / %d / %d\n" +
                                    "KDA Ratio     %.2f\n" +
                                    "ACS           %d\n" +
                                    "Score         %d\n" +
                                    "HS%%           %.1f%%\n" +
                                    "Daño hecho    %d\n" +
                                    "Daño recibido %d\n" +
                                    "```",
                            kills, deaths, assists, kda, acs, score, hsPercent, dmgMade, dmgReceived
                    ),
                    false
            );

            // Equipos
            embed.addField("🔴  Team Red", redBuilder.toString(), false);
            embed.addField("🔵  Team Blue", blueBuilder.toString(), false);

            embed.setFooter("Valorant Match · " + map);

            event.getChannel().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Error obteniendo la partida. Verifica el nombre#tag.").queue();
            e.printStackTrace();
        }
    }

    /**
     * Devuelve un emoji representativo según el tier numérico de Valorant.
     * Tiers: 0=Unranked, 3-5=Iron, 6-8=Bronze, 9-11=Silver, 12-14=Gold,
     * 15-17=Platinum, 18-20=Diamond, 21-23=Ascendant, 24-26=Immortal, 27=Radiant
     */
    private String getTierEmoji(int tier) {
        if (tier == 0) {
            return "❓";
        }// Unranked
        if (tier <= 5) {
            return "🔘";
        } // Iron      - gris
        if (tier <= 8) {
            return "🟤";
        } // Bronze    - café
        if (tier <= 11) {
            return "⚪";
        } // Silver    - blanco/gris claro
        if (tier <= 14) {
            return "🟡";
        } // Gold      - amarillo
        if (tier <= 17) {
            return "🔵";
        }// Platinum  - azul
        if (tier <= 20) {
            return "💎";
        } // Diamond   - azul claro
        if (tier <= 23) {
            return "🟢";
        } // Ascendant - verde
        if (tier <= 26) {
            return "🔴";
        } // Immortal  - rojo
        return "🏆";                  // Radiant
    }
}