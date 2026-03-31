package com.marlonreina.resisas.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "leaderboard_accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"guild_id", "riot_name", "riot_tag"})
)
public class LeaderboardAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Column(name = "discord_id", nullable = false)
    private String discordId;

    @Column(name = "riot_name", nullable = false)
    private String riotName;

    @Column(name = "riot_tag", nullable = false)
    private String riotTag;

    public LeaderboardAccount() {}

    public LeaderboardAccount(String guildId, String discordId, String riotName, String riotTag) {
        this.guildId = guildId;
        this.discordId = discordId;
        this.riotName = riotName;
        this.riotTag = riotTag;
    }

    public Long getId() { return id; }
    public String getGuildId() { return guildId; }
    public String getDiscordId() { return discordId; }
    public String getRiotName() { return riotName; }
    public String getRiotTag() { return riotTag; }
}