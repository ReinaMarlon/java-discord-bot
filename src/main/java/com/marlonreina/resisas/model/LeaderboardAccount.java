package com.marlonreina.resisas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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

}