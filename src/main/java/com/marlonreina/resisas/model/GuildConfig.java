package com.marlonreina.resisas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
@Table(name = "guild_config")
public class GuildConfig {

    @Id
    @Column(name = "guild_id")
    private String guildId;

    @Column(name = "prefix")
    private String prefix;

}