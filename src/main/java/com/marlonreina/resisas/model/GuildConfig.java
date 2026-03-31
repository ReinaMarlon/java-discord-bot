package com.marlonreina.resisas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "guild_config")
public class GuildConfig {

    @Id
    @Column(name = "guild_id")
    private String guildId;

    @Column(name = "prefix")
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