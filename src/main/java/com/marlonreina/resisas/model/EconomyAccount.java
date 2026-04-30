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

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "economy_accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"guild_id", "discord_id"}))
public class EconomyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Column(name = "discord_id", nullable = false)
    private String userId;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Column(name = "last_daily_at")
    private OffsetDateTime lastDailyAt;
}
