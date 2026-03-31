package com.marlonreina.resisas.utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class EmbedUtil {

    public static EmbedBuilder success(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.GREEN)
                .setFooter("Sistema de moderación ResisasBot. V 1.0");
    }

    public static EmbedBuilder error(String desc) {
        return new EmbedBuilder()
                .setTitle("Error")
                .setDescription(desc)
                .setColor(Color.RED)
                .setFooter("Sistema de moderación ResisasBot. V 1.0");
    }

    public static EmbedBuilder info(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.BLUE)
                .setFooter("Sistema de moderación ResisasBot. V 1.0");
    }

    public static EmbedBuilder simplyBuildMessage(String title, String desc, Color color){
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setColor(color)
                .setFooter("Sistema de moderación ResisasBot. V 1.0");
    }
}