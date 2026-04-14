package com.marlonreina.resisas.commands.riot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.service.RiotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class ValorantRankCommand implements Command {

    private final RiotService riotService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ValorantRankCommand(RiotService riotService) {
        this.riotService = riotService;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        if (args.length < 1) {
            event.getChannel().sendMessage("Uso: !vrank nombre#tag").queue();
            return;
        }

        try {
            String[] riotId = args[0].split("#");

            if (riotId.length < 2) {
                event.getChannel().sendMessage("Formato correcto: nombre#tag").queue();
                return;
            }

            String name = riotId[0];
            String tag = riotId[1];

            String json = riotService.getValorantRank("latam", name, tag);

            JsonNode root = mapper.readTree(json);

            if (root.get("data") == null) {
                event.getChannel().sendMessage("Jugador no encontrado").queue();
                return;
            }

            JsonNode data = root.get("data");

            String rank = data.get("currenttierpatched").asText();
            int rr = data.get("ranking_in_tier").asInt();
            int lastGame = data.get("mmr_change_to_last_game").asInt();

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle("Rango de Valorant");
            embed.setColor(Color.ORANGE);

            embed.addField("Jugador", data.get("name").asText() + "#" + data.get("tag").asText(), false);
            embed.addField("Rango", data.get("currenttierpatched").asText(), true);
            embed.addField("RR", String.valueOf(data.get("ranking_in_tier").asInt()), true);

            String lastGameStr = (lastGame >= 0 ? "+" : "") + lastGame;
            embed.addField("Última partida", lastGameStr, true);

            embed.addField("ELO", String.valueOf(data.get("elo").asInt()), true);

            String imageUrl = data.get("images").get("small").asText();
            embed.setThumbnail(imageUrl);

            embed.setFooter("Resisas Valorant Tracker");

            event.getChannel().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getChannel().sendMessage("Error al consultar el rango").queue();
            e.printStackTrace();
        }
    }
}