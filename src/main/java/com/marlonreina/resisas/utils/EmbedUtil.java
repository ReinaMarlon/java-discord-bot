package com.marlonreina.resisas.utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;

public class EmbedUtil {

    public static final int HEXA_COLOR = 0x5865F2;
    public static final String FOOTER = "Hexa";

    public static EmbedBuilder success(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.GREEN)
                .setFooter(FOOTER);
    }

    public static EmbedBuilder error(String desc) {
        return new EmbedBuilder()
                .setTitle("Error")
                .setDescription(desc)
                .setColor(Color.RED)
                .setFooter(FOOTER);
    }

    public static EmbedBuilder info(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setColor(new Color(HEXA_COLOR))
                .setFooter(FOOTER);
    }

    public static EmbedBuilder simplyBuildMessage(String title, String desc, Color color) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setColor(color)
                .setFooter(FOOTER);
    }

    public static EmbedBuilder usage(String usage) {
        return info("Uso del comando")
                .setDescription("`" + usage + "`");
    }

    public static EmbedBuilder loading(String message) {
        return info("Procesando")
                .setDescription(message);
    }
}
