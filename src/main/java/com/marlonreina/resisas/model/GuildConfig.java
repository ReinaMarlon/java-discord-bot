package com.marlonreina.resisas.model;

public class GuildConfig {

    private String guildId;
    private String prefix;

    public GuildConfig(String guildId, String prefix) {
        this.guildId = guildId;
        this.prefix = prefix;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}