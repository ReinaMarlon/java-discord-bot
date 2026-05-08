package com.marlonreina.resisas.listener;

import com.marlonreina.resisas.model.WelcomeConfig;
import com.marlonreina.resisas.service.WelcomeConfigService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
public class WelcomeListener extends ListenerAdapter {

    private final WelcomeConfigService welcomeConfigService;

    public WelcomeListener(WelcomeConfigService welcomeConfigService) {
        this.welcomeConfigService = welcomeConfigService;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        WelcomeConfig config = welcomeConfigService.getOrCreate(event.getGuild().getId());
        if (!config.isEnabled() || config.getChannelId() == null) {
            return;
        }

        TextChannel channel = event.getGuild().getTextChannelById(config.getChannelId());
        if (channel == null) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(0x57F287));
        embed.setTitle("Bienvenido a " + event.getGuild().getName());
        embed.setDescription("Hola " + event.getMember().getAsMention()
                + ", nos alegra tenerte por aqui.\n"
                + "Toma asiento, revisa las reglas y disfruta la comunidad.");
        embed.addField("Servidor", event.getGuild().getName(), true);
        embed.addField("Miembro", "#" + event.getGuild().getMemberCount(), true);
        embed.addField("Cuenta creada", event.getUser().getTimeCreated().toLocalDate().toString(), true);
        embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
        embed.setFooter("Hexa - Welcome");

        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
